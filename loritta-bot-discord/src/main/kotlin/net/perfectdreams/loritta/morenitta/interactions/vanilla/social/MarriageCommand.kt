package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Marriage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredMarriageMarryTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class MarriageCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Marriage
        const val MARRIAGE_COST = 15_000
        const val LOCALE_PREFIX = "commands.command.divorce"
        const val DIVORCE_REACTION_EMOJI = "\uD83D\uDC94"
        const val DIVORCE_EMBED_URI = "https://cdn.discordapp.com/emojis/556524143281963008.png?size=2048"
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("4aea2db6-4805-47dc-b138-f1bb5d15a9f0")) {
        this.enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Marry.Label, I18N_PREFIX.Marry.Description, UUID.fromString("3b154846-ed9c-4147-81c8-4e5a98bff0db")) {
            this.enableLegacyMessageSupport = true

            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("casar")
                this.add("marry")
            }

            executor = MarryUserExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Divorce.Label, I18N_PREFIX.Divorce.Description, UUID.fromString("c82cd127-b74b-451d-b007-12bc539c0407")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("divorce")
                this.add("divorciar")
            }

            executor = MarryDivorceExecutor(loritta)
        }
    }

    class MarryUserExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val requestedBy = context.user
            val proposeTo = args[options.user].user

            val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.id)
            val marriage = loritta.newSuspendedTransaction { context.lorittaUser.profile.marriage }
            val proposeMarriage = loritta.newSuspendedTransaction { proposeToProfile.marriage }

            val splitCost = MARRIAGE_COST / 2

            if (proposeTo.idLong == requestedBy.idLong) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.cantMarryYourself"],
                        Constants.ERROR
                    )
                }
                return
            }

            if (proposeTo.id == loritta.config.loritta.discord.applicationId.toString()) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.marryLoritta"],
                        "<:smol_lori_putassa:395010059157110785>"
                    )
                }
                return
            }

            if (marriage != null) {
                // Já está casado!
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.alreadyMarried", context.config.commandPrefix],
                        Constants.ERROR
                    )
                }
                return
            }

            if (proposeMarriage != null) {
                // Já está casado!
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.alreadyMarriedOther", proposeTo.asMention],
                        Constants.ERROR
                    )
                }
                return
            }

            if (splitCost > context.lorittaUser.profile.money) {
                // Não tem dinheiro suficiente!
                val diff = splitCost - context.lorittaUser.profile.money
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.insufficientFunds", diff],
                        Constants.ERROR
                    )
                }
                return
            }

            if (splitCost > proposeToProfile.money) {
                // Não tem dinheiro suficiente!
                val diff = splitCost - proposeToProfile.money
                context.reply(false) {
                    styled(
                        context.locale["commands.command.marry.insufficientFundsOther", proposeTo.asMention, diff],
                        Constants.ERROR
                    )
                }
                return
            }

            // Temporary until we refactor the marriage code
            val mutex = Mutex()

            val button = loritta.interactivityManager.buttonForUser(
                proposeTo,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                context.i18nContext.get(I18N_PREFIX.Marry.AcceptMarryButton),
                {
                    emoji = Emoji.fromUnicode("\uD83D\uDC8D")
                }
            ) { context ->
                mutex.withLock {
                    val profile = loritta.getOrCreateLorittaProfile(requestedBy.idLong)
                    val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.idLong)
                    val marriage = loritta.newSuspendedTransaction { profile.marriage }
                    val proposeMarriage = loritta.newSuspendedTransaction { proposeToProfile.marriage }

                    if (proposeTo.id == requestedBy.id) {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.cantMarryYourself"],
                                Constants.ERROR
                            )
                        }
                        return@buttonForUser
                    }

                    if (proposeTo.id == loritta.config.loritta.discord.applicationId.toString()) {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.loritta"],
                                "<:smol_lori_putassa:395010059157110785>"
                            )
                        }
                        return@buttonForUser
                    }

                    if (marriage != null) {
                        // Não tem dinheiro suficiente!
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.alreadyMarried"],
                                Constants.ERROR
                            )
                        }
                        return@buttonForUser
                    }

                    if (proposeMarriage != null) {
                        // Já está casado!
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.alreadyMarriedOther"],
                                Constants.ERROR
                            )
                        }
                        return@buttonForUser
                    }

                    if (splitCost > profile.money) {
                        // Não tem dinheiro suficiente!
                        val diff = splitCost - profile.money
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.insufficientFunds", diff],
                                Constants.ERROR
                            )
                        }
                        return@buttonForUser
                    }

                    if (splitCost > proposeToProfile.money) {
                        // Não tem dinheiro suficiente!
                        val diff = splitCost - proposeToProfile.money
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.insufficientFundsOther", proposeTo.asMention, diff],
                                Constants.ERROR
                            )
                        }
                        return@buttonForUser
                    }

                    // Okay, tudo certo, vamos lá!
                    loritta.newSuspendedTransaction {
                        val newMarriage = Marriage.new {
                            user1 = requestedBy.idLong
                            user2 = proposeTo.idLong
                            marriedSince = System.currentTimeMillis()
                        }
                        profile.marriage = newMarriage
                        proposeToProfile.marriage = newMarriage

                        val splitCostAsLong = splitCost.toLong()

                        profile.takeSonhosAndAddToTransactionLogNested(
                            splitCostAsLong,
                            SonhosPaymentReason.MARRIAGE
                        )

                        proposeToProfile.takeSonhosAndAddToTransactionLogNested(
                            splitCostAsLong,
                            SonhosPaymentReason.MARRIAGE
                        )

                        // Cinnamon transactions log
                        SimpleSonhosTransactionsLogUtils.insert(
                            profile.id.value,
                            Instant.now(),
                            TransactionType.MARRIAGE,
                            splitCostAsLong,
                            StoredMarriageMarryTransaction(
                                proposeToProfile.id.value
                            )
                        )

                        SimpleSonhosTransactionsLogUtils.insert(
                            proposeToProfile.id.value,
                            Instant.now(),
                            TransactionType.MARRIAGE,
                            splitCostAsLong,
                            StoredMarriageMarryTransaction(
                                profile.id.value
                            )
                        )
                    }

                    context.reply(false) {
                        styled(
                            "Vocês se casaram! Felicidades para vocês dois!",
                            "❤"
                        )
                    }
                }
            }

            // Pedido enviado!
            context.reply(false) {
                styled(
                    proposeTo.asMention + " Você recebeu uma proposta de casamento de " + requestedBy.asMention + "!",
                    "\uD83D\uDC8D"
                )
                styled(
                    "Para aceitar, clique no \uD83D\uDC8D! Mas lembrando, o custo de um casamento é **15000 Sonhos** (7500 para cada usuário), e **250 Sonhos** todos os dias!",
                    "\uD83D\uDCB5"
                )

                actionRow(
                    button
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val user = context.getUserAndMember(0)
            if (user == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.user to user
            )
        }
    }

    class MarryDivorceExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userProfile = context.lorittaUser._profile ?: run {
                // If the user doesn't have any profile, then he won't have any marriage anyway
                context.reply(false) {
                    styled(
                        context.locale["commands.category.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", net.perfectdreams.loritta.common.utils.Emotes.LORI_HEART],
                        Constants.ERROR
                    )
                }
                return
            }

            val marriage = ProfileUtils.getMarriageInfo(loritta, userProfile) ?: run {
                // Now that's for when the marriage doesn't exist
                context.reply(false) {
                    styled(
                        context.locale["commands.category.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", net.perfectdreams.loritta.common.utils.Emotes.LORI_HEART],
                        Constants.ERROR
                    )
                }
                return
            }

            val marriagePartner = marriage.partner
            val userMarriage = marriage.marriage

            // Temporary until we refactor the marriage code
            val mutex = Mutex()

            val button = loritta.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.DANGER,
                context.i18nContext.get(I18N_PREFIX.Divorce.DivorceButton),
                {
                    emoji = Emoji.fromUnicode(DIVORCE_REACTION_EMOJI)
                }
            ) { context ->
                mutex.withLock {
                    loritta.newSuspendedTransaction {
                        Profiles.update({ Profiles.marriage eq userMarriage.id }) {
                            it[Profiles.marriage] = null
                        }
                        userMarriage.delete()
                    }

                    context.reply(false) {
                        styled(
                            context.locale["$LOCALE_PREFIX.divorced", net.perfectdreams.loritta.common.utils.Emotes.LORI_HEART]
                        )
                    }

                    try {
                        // We don't care if we can't find the user, just exit
                        val userPrivateChannel =
                            loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(marriagePartner.id)
                                ?: return@buttonForUser

                        userPrivateChannel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setTitle(context.locale["$LOCALE_PREFIX.divorcedTitle"])
                                .setDescription(context.locale["$LOCALE_PREFIX.divorcedDescription", context.user.name])
                                .setThumbnail(DIVORCE_EMBED_URI)
                                .setColor(Constants.LORITTA_AQUA)
                                .build()
                        ).queue()
                    } catch (e: Exception) {
                    }
                }
            }

            context.reply(false) {
                styled(
                    context.locale["$LOCALE_PREFIX.prepareToDivorce", net.perfectdreams.loritta.common.utils.Emotes.LORI_CRYING],
                    "\uD83D\uDDA4"
                )

                styled(
                    context.locale["$LOCALE_PREFIX.pleaseConfirm", DIVORCE_REACTION_EMOJI]
                )

                actionRow(button)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }
}