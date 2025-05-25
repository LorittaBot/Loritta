package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Section
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageLoveLetters
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.ShipCommand
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.ceil

class MarriageCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Marriage
        const val MARRIAGE_COST = 50_000
        const val MARRIAGE_RESTORE_COST = 250_000
        const val LOCALE_PREFIX = "commands.command.divorce"
        const val DIVORCE_REACTION_EMOJI = "\uD83D\uDC94"
        const val DIVORCE_EMBED_URI = "https://cdn.discordapp.com/emojis/556524143281963008.png?size=2048"
        const val DEFAULT_AFFINITY = 20
        const val MARRIAGE_RESTORE_MAX_TIME = 604_800_000L
        const val LOVE_LETTER_PRICE = 7_500L
        const val LOVE_LETTER_AFFINITY = 2
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("4aea2db6-4805-47dc-b138-f1bb5d15a9f0")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Marry.Label, I18N_PREFIX.Marry.Description, UUID.fromString("3b154846-ed9c-4147-81c8-4e5a98bff0db")) {
            this.enableLegacyMessageSupport = true

            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("casar")
                this.add("marry")
            }

            executor = MarriageMarryUserExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Divorce.Label, I18N_PREFIX.Divorce.Description, UUID.fromString("c82cd127-b74b-451d-b007-12bc539c0407")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("divorce")
                this.add("divorciar")
            }

            executor = MarriageDivorceExecutor(loritta)
        }

        subcommand(I18N_PREFIX.View.Label, I18N_PREFIX.View.Description, UUID.fromString("5fcf7ffd-fe5f-4384-9c81-6aeb698de331")) {
            executor = MarriageViewExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Rank.Label, I18N_PREFIX.Rank.Description, UUID.fromString("401e7c3f-c99b-47e3-9f0c-3b0626b2ba2d")) {
            executor = MarriageRankExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Configure.Label, I18N_PREFIX.Configure.Description, UUID.fromString("e9a97831-a633-4a2d-b1db-0ba55daec10c")) {
            executor = MarriageConfigureExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Restore.Label, I18N_PREFIX.Restore.Description, UUID.fromString("c1fd14b8-26f6-44dd-900c-f4d159f94187")) {
            executor = MarriageRestoreExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Letter.Label, I18N_PREFIX.Letter.Description, UUID.fromString("bbc337f9-d7f5-4bf3-9ad3-62036ab0fb9a")) {
            executor = MarriageLetterExecutor(loritta)
        }
    }

    class MarriageMarryUserExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18N_PREFIX.Marry.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val requestedBy = context.user
            val proposeTo = args[options.user].user

            val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.id)
            val marriage = loritta.pudding.marriages.getMarriageByUser(UserId(requestedBy.idLong))
            val proposeMarriage = loritta.pudding.marriages.getMarriageByUser(UserId(proposeTo.idLong))

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

            val button = loritta.interactivityManager.buttonForUser(
                if (loritta.config.loritta.environment == EnvironmentType.CANARY) requestedBy else proposeTo,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                context.i18nContext.get(I18N_PREFIX.Marry.AcceptMarryButton),
                {
                    emoji = Emoji.fromUnicode("\uD83D\uDC8D")
                }
            ) { context ->
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

                val result = loritta.transaction {
                    val now = Instant.now()

                    val requesterAlreadyHasMarriage = MarriageParticipants.selectAll()
                        .where {
                            MarriageParticipants.user eq requestedBy.idLong
                        }
                        .count() != 0L

                    if (requesterAlreadyHasMarriage)
                        return@transaction MarryResult.RequesterIsAlreadyMarried

                    val proposeToAlreadyHasMarriage = MarriageParticipants.selectAll()
                        .where {
                            MarriageParticipants.user eq proposeTo.idLong
                        }
                        .count() != 0L

                    if (proposeToAlreadyHasMarriage)
                        return@transaction MarryResult.ProposedToIsAlreadyMarried

                    val requestedByProfile = loritta.getOrCreateLorittaProfile(requestedBy.idLong)
                    val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.idLong)

                    if (splitCost > requestedByProfile.money) {
                        // Não tem dinheiro suficiente!
                        return@transaction MarryResult.RequesterDoesNotHaveEnoughSonhos(splitCost - requestedByProfile.money)
                    }

                    if (splitCost > proposeToProfile.money) {
                        // Não tem dinheiro suficiente!
                        return@transaction MarryResult.ProposedToDoesNotHaveEnoughSonhos(splitCost - proposeToProfile.money)
                    }

                    val marriage = UserMarriages.insert {
                        it[UserMarriages.createdAt] = now
                        it[UserMarriages.active] = true
                        it[UserMarriages.affinity] = DEFAULT_AFFINITY
                        it[UserMarriages.hugCount] = 0
                        it[UserMarriages.headPatCount] = 0
                        it[UserMarriages.highFiveCount] = 0
                        it[UserMarriages.slapCount] = 0
                        it[UserMarriages.attackCount] = 0
                        it[UserMarriages.danceCount] = 0
                        it[UserMarriages.kissCount] = 0
                        it[UserMarriages.coupleName] = null
                    }

                    MarriageParticipants.insert {
                        it[MarriageParticipants.marriage] = marriage[UserMarriages.id]
                        it[MarriageParticipants.user] = requestedBy.idLong
                        it[MarriageParticipants.joinedAt] = now
                        it[MarriageParticipants.primaryMarriage] = true
                    }

                    MarriageParticipants.insert {
                        it[MarriageParticipants.marriage] = marriage[UserMarriages.id]
                        it[MarriageParticipants.user] = proposeTo.idLong
                        it[MarriageParticipants.joinedAt] = now
                        it[MarriageParticipants.primaryMarriage] = true
                    }

                    val splitCostAsLong = splitCost.toLong()

                    requestedByProfile.takeSonhosAndAddToTransactionLogNested(
                        splitCostAsLong,
                        SonhosPaymentReason.MARRIAGE
                    )

                    proposeToProfile.takeSonhosAndAddToTransactionLogNested(
                        splitCostAsLong,
                        SonhosPaymentReason.MARRIAGE
                    )

                    // Cinnamon transactions log
                    SimpleSonhosTransactionsLogUtils.insert(
                        requestedByProfile.id.value,
                        Instant.now(),
                        TransactionType.MARRIAGE,
                        splitCostAsLong,
                        StoredMarriageMarryTransaction(
                            proposeTo.idLong
                        )
                    )

                    SimpleSonhosTransactionsLogUtils.insert(
                        proposeToProfile.id.value,
                        Instant.now(),
                        TransactionType.MARRIAGE,
                        splitCostAsLong,
                        StoredMarriageMarryTransaction(
                            requestedBy.idLong
                        )
                    )

                    return@transaction MarryResult.Success
                }

                when (result) {
                    is MarryResult.ProposedToDoesNotHaveEnoughSonhos -> {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.insufficientFundsOther", proposeTo.asMention, result.requiredSonhos],
                                Constants.ERROR
                            )
                        }
                    }
                    MarryResult.ProposedToIsAlreadyMarried -> {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.alreadyMarried"],
                                Constants.ERROR
                            )
                        }
                    }
                    is MarryResult.RequesterDoesNotHaveEnoughSonhos -> {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.insufficientFunds", result.requiredSonhos],
                                Constants.ERROR
                            )
                        }
                    }
                    MarryResult.RequesterIsAlreadyMarried -> {
                        context.reply(false) {
                            styled(
                                context.locale["commands.command.marry.alreadyMarriedOther"],
                                Constants.ERROR
                            )
                        }
                    }
                    MarryResult.Success -> {
                        context.reply(false) {
                            styled(
                                "Vocês se casaram! Felicidades para vocês dois!",
                                Emotes.LoriHeart
                            )
                        }
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
                    "Para aceitar, clique no \uD83D\uDC8D! Mas lembrando, o custo de um casamento é **$MARRIAGE_COST Sonhos** (${MARRIAGE_COST / 2} para cada usuário) e você perde **dois pontos de afinidade** todos os dias (cada casamento começa com 20 pontos), sendo possível ganhar pontos enviando cartinhas de amor e usando ações fofinhas nos comandos de roleplay. Se você chegar em zero pontos, o seu casamento acaba...",
                    "\uD83D\uDCB5"
                )
                styled(
                    "(O sistema de casamento da Loritta está sendo alterado, então os valores podem ser alteradas no futuro, fique de olho nas novidades!)",
                    Emotes.LoriLurk
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

        sealed class MarryResult {
            data object RequesterIsAlreadyMarried : MarryResult()
            data object ProposedToIsAlreadyMarried : MarryResult()
            data class RequesterDoesNotHaveEnoughSonhos(val requiredSonhos: Long) : MarryResult()
            data class ProposedToDoesNotHaveEnoughSonhos(val requiredSonhos: Long) : MarryResult()
            data object Success : MarryResult()
        }
    }

    class MarriageDivorceExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

            val button = loritta.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.DANGER,
                context.i18nContext.get(I18N_PREFIX.Divorce.DivorceButton),
                {
                    emoji = Emoji.fromUnicode(DIVORCE_REACTION_EMOJI)
                }
            ) { context ->
                loritta.newSuspendedTransaction {
                    MarriageParticipants.deleteWhere {
                        MarriageParticipants.marriage eq userMarriage.data.id
                    }

                    UserMarriages.update({ UserMarriages.id eq userMarriage.data.id }) {
                        it[UserMarriages.active] = false
                    }
                }

                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.divorced", net.perfectdreams.loritta.common.utils.Emotes.LORI_HEART]
                    )
                }

                try {
                    // We don't care if we can't find the user, just exit
                    val userPrivateChannel = loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(marriagePartner.id)
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

    class MarriageViewExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.View.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userLookup = args[options.user]?.user ?: context.user
            val lookingUpSelf = userLookup.idLong == context.user.idLong

            val marriage = context.loritta.pudding.marriages.getMarriageByUser(UserId(userLookup.idLong))

            if (marriage == null) {
                if (lookingUpSelf) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.View.YouAreNotMarried(loritta.commandMentions.marriageMarry)),
                            Emotes.LoriSob
                        )
                    }
                } else {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.View.UserIsNotMarried(userLookup.asMention)),
                            Emotes.LoriSob
                        )
                    }
                }
                return
            }

            val user1Info = loritta.lorittaShards.retrieveUserInfoById(marriage.user1)!!
            val user2Info = loritta.lorittaShards.retrieveUserInfoById(marriage.user2)!!

            val daysMarried = marriage.data.marriedSince.toJavaInstant().until(Instant.now(), ChronoUnit.DAYS)

            val fancyTags = mapOf(
                3640 to "Amor Atemporal",   // 520 semanas (~10 anos)
                1820 to "Chama Eterna",     // 260 semanas (~5 anos)
                728 to "Almas Gêmeas",    // 104 semanas (~2 anos)
                364 to "Parceiros de Jornada", // 52 semanas (~1 ano)
                182 to "Dupla Devotada",    // 26 semanas (~6 meses)
                91 to "Laços Fortes",        // 13 semanas (~3 meses)
                49 to "Pombinhos",          // 7 semanas
                21 to "Apaixonados",        // 3 semanas
                14 to "Queridinhos",        // 2 semanas
                7 to "Recém-Casados",       // 1 semana
                0 to "Início Promissor"     // Começo
            )

            val tagToBeDisplayed = fancyTags
                .entries
                .sortedByDescending { it.key }
                .first { daysMarried >= it.key }
                .value

            // Calculate next anniversary date
            val marriedSince = marriage.marriedSince.toJavaInstant()
            val now = Instant.now()
            val marriageDate = marriedSince.atZone(Constants.LORITTA_TIMEZONE)
            val marriageAnniversaryDate = findNextAnniversary(now.atZone(Constants.LORITTA_TIMEZONE), marriageDate)

            val sentByCountField = MarriageLoveLetters.sentBy.count()

            val (marriageRank, marriageAffinityRank, loveLetters) = loritta.transaction {
                val marriageRank = UserMarriages.selectAll()
                    .where { UserMarriages.active eq true and (UserMarriages.createdAt lessEq marriedSince) }
                    .orderBy(Pair(UserMarriages.createdAt, SortOrder.ASC), Pair(UserMarriages.id, SortOrder.ASC))
                    .count()

                val marriageAffinityRank = UserMarriages.selectAll()
                    .where { UserMarriages.active eq true and (UserMarriages.affinity greaterEq  marriage.data.affinity) }
                    .orderBy(Pair(UserMarriages.affinity, SortOrder.ASC), Pair(UserMarriages.id, SortOrder.ASC))
                    .count()

                val loveLetters = MarriageLoveLetters.select(MarriageLoveLetters.sentBy, sentByCountField)
                    .where { MarriageLoveLetters.marriage eq marriage.data.id }
                    .groupBy(MarriageLoveLetters.sentBy)
                    .toList()

                Triple(marriageRank, marriageAffinityRank, loveLetters)
            }

            val coupleName = if (marriage.data.coupleName != null)
                marriage.data.coupleName
            else {
                val participantsAsUserInfos = mutableListOf<CachedUserInfo?>()

                for (participantData in marriage.participants) {
                    val user = loritta.lorittaShards.retrieveUserInfoById(participantData)

                    participantsAsUserInfos.add(user)
                }

                val participantNamesAsUsers = mutableListOf<String>()

                for (userInfo in participantsAsUserInfos) {
                    participantNamesAsUsers.add(userInfo?.globalName ?: userInfo?.name ?: "???")
                }

                val sorted = participantNamesAsUsers.sorted()

                ShipCommand.createShipName(sorted[0], sorted[1])
            }

            val coupleBadgeId = marriage.data.coupleBadge?.let { UUID.fromString(it) }
            val equippedBadge = if (coupleBadgeId != null) {
                loritta.profileDesignManager.badges.filterIsInstance<Badge.LorittaBadge>().firstOrNull { it.id == coupleBadgeId }
            } else null

            context.reply(false) {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = LorittaColors.LorittaPink.rgb

                    + TextDisplay(
                        buildString {
                            appendLine("### ${Emotes.LoriHeartCombo1}${Emotes.LoriHeartCombo2} ${context.i18nContext.get(I18N_PREFIX.View.UserMarriage(userLookup.asMention))}")
                            if (equippedBadge != null) {
                                append(loritta.emojiManager.get(equippedBadge.emoji))
                                append(" ")
                            }
                            append("**$coupleName**")
                            appendLine()

                            appendLine("*${tagToBeDisplayed}*")
                            appendLine()

                            appendLine("${Emotes.SparklingHeart} <@${user1Info.id}> (`${user1Info.id}`)")
                            appendLine("${Emotes.SparklingHeart} <@${user2Info.id}> (`${user2Info.id}`)")
                            appendLine()

                            appendLine("**Afinidade:** ${marriage.data.affinity} pontos")
                            appendLine("Este casamento é o #${marriageAffinityRank} com mais afinidade em toda a Loritta!")
                            appendLine(context.i18nContext.get(I18N_PREFIX.View.YouLoseXPointsEveryDay(marriage.participants.size, loritta.commandMentions.roleplayHug, loritta.commandMentions.roleplayKiss, loritta.commandMentions.roleplayHeadPat, loritta.commandMentions.marriageLetter)))
                            appendLine()

                            appendLine("**Casados desde:** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(marriage.marriedSince.toJavaInstant())}")
                            appendLine("**Próximo aniversário do casamento:** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(marriageAnniversaryDate.toInstant())}")
                            appendLine("Este casamento é o #${marriageRank} mais duradouro em toda a Loritta!")
                            appendLine()

                            appendLine("**Abraços dados:** ${marriage.data.hugCount}")
                            appendLine("**Beijos dados:** ${marriage.data.kissCount}")
                            appendLine("**Cafunés dados:** ${marriage.data.headPatCount}")
                            appendLine()
                            for (loveLetter in loveLetters) {
                                appendLine("${Emotes.LoveLetter} ${context.i18nContext.get(I18N_PREFIX.View.SentXLoveLetters("<@${loveLetter[MarriageLoveLetters.sentBy]}>", loveLetter[sentByCountField]))}")
                            }
                        }
                    )
                }
            }
        }

        /**
         * Calculates the anniversary date within a specific target year,
         * handling Feb 29th in non-leap years by moving it to March 1st.
         *
         * @param originalDate The original date and time of the event.
         * @param targetYear The year for which to calculate the anniversary.
         * @return The ZonedDateTime of the anniversary in the target year.
         */
        private fun getAnniversaryInYear(originalDate: ZonedDateTime, targetYear: Int): ZonedDateTime {
            var month = originalDate.month
            var day = originalDate.dayOfMonth

            // Handle Feb 29th in non-leap years
            if (month == Month.FEBRUARY && day == 29 && !Year.isLeap(targetYear.toLong())) {
                month = Month.MARCH
                day = 1
            }

            return ZonedDateTime.of(
                targetYear,
                month.value,
                day,
                originalDate.hour,
                originalDate.minute,
                originalDate.second,
                originalDate.nano,
                Constants.LORITTA_TIMEZONE
            )
        }

        /**
         * Finds the next upcoming anniversary date based on the current date.
         * If the anniversary for the current year has already passed, it returns
         * the anniversary date for the next year. Handles leap years for Feb 29th.
         *
         * @param currentDateTime The current date time.
         * @param originalDate The original date and time of the event.
         * @return The LocalDateTime of the next upcoming anniversary.
         */
        private fun findNextAnniversary(currentDateTime: ZonedDateTime, originalDate: ZonedDateTime): ZonedDateTime {
            val currentYear = currentDateTime.year

            // Calculate potential anniversary for the current year
            val anniversaryThisYear = getAnniversaryInYear(originalDate, currentYear)

            // Check if the anniversary this year is still upcoming (or today)
            return if (!anniversaryThisYear.isBefore(currentDateTime)) { // isAfter or isEqual
                anniversaryThisYear
            } else {
                // Anniversary has passed this year, calculate for next year
                getAnniversaryInYear(originalDate, currentYear + 1)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }

    class MarriageRestoreExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val now7DaysAgo = Instant.now().minusMillis(MARRIAGE_RESTORE_MAX_TIME)

            val result = loritta.pudding.transaction {
                val marriage = MarriageParticipants
                    .innerJoin(UserMarriages)
                    .selectAll()
                    .where {
                        UserMarriages.active eq false and (UserMarriages.expiredAt greaterEq now7DaysAgo) and (MarriageParticipants.user eq context.user.idLong)
                    }
                    .orderBy(UserMarriages.expiredAt, SortOrder.DESC)
                    .limit(1)
                    .firstOrNull() ?: return@transaction RestorableMarriageQueryResult.NotFound

                val marriageParticipants = MarriageParticipants.selectAll()
                    .where {
                        MarriageParticipants.marriage eq marriage[UserMarriages.id]
                    }
                    .toList()

                return@transaction RestorableMarriageQueryResult.Success(marriage, marriageParticipants)
            }

            when (result) {
                RestorableMarriageQueryResult.NotFound -> {
                    context.reply(false) {
                        styled(
                            "Você não tem nenhum casamento que pode ser restaurado! Apenas casamentos que foram perdidos por falta de afinidade podem ser recuperados.",
                            Emotes.LoriSob
                        )
                    }
                }
                is RestorableMarriageQueryResult.Success -> {
                    val willExpireAt = result.marriage[UserMarriages.expiredAt]!!.plusMillis(MARRIAGE_RESTORE_MAX_TIME)

                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Restore.YouHaveAMarriageThatCanBeRestored),
                            Emotes.MarriageRing
                        )

                        for (participant in result.participants) {
                            styled(
                                "<@${participant[MarriageParticipants.user]}> (`${participant[MarriageParticipants.user]}`)",
                                Emotes.SparklingHeart
                            )
                        }

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Restore.RestoreInformation(SonhosUtils.getSonhosEmojiOfQuantity(MARRIAGE_RESTORE_COST.toLong()), MARRIAGE_RESTORE_COST, DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.marriage[UserMarriages.createdAt]))),
                            Emotes.LoriReading
                        )

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Restore.RestoreLimit(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(willExpireAt))),
                            Emotes.LoriDemon
                        )

                        actionRow(
                            loritta.interactivityManager.buttonForUser(
                                context.user,
                                context.alwaysEphemeral,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(I18N_PREFIX.Restore.RestoreMarriageButton),
                                {
                                    loriEmoji = Emotes.MarriageRing
                                }
                            ) { context ->
                                context.deferChannelMessage(false)

                                val result = loritta.transaction {
                                    val activeMarriages = MarriageParticipants
                                        .innerJoin(UserMarriages)
                                        .selectAll()
                                        .where {
                                            UserMarriages.active eq true and (MarriageParticipants.user inList result.participants.map { it[MarriageParticipants.user] })
                                        }
                                        .toList()

                                    for (activeMarriage in activeMarriages) {
                                        return@transaction RestoreMarriageResult.ParticipantIsAlreadyMarried(activeMarriage[MarriageParticipants.user])
                                    }

                                    UserMarriages.update({ UserMarriages.id eq result.marriage[UserMarriages.id] }) {
                                        it[UserMarriages.expiredAt] = null
                                        it[UserMarriages.active] = true
                                        it[UserMarriages.affinity] = DEFAULT_AFFINITY
                                    }

                                    val sonhosCheckResult = SonhosUtils.takeSonhosAndLogToTransactionLog(
                                        context.user.idLong,
                                        MARRIAGE_RESTORE_COST.toLong(),
                                        TransactionType.MARRIAGE,
                                        StoredMarriageRestoreTransaction
                                    )

                                    when (sonhosCheckResult) {
                                        is SonhosUtils.SonhosRemovalResult.NotEnoughSonhos -> {
                                            return@transaction RestoreMarriageResult.NotEnoughSonhos(sonhosCheckResult.balance)
                                        }
                                        SonhosUtils.SonhosRemovalResult.Success -> {}
                                    }

                                    return@transaction RestoreMarriageResult.Success
                                }

                                when (result) {
                                    is RestoreMarriageResult.ParticipantIsAlreadyMarried -> {
                                        context.reply(false) {
                                            styled(
                                                "Você não pode restaurar o casamento pois <@${result.userId}> (`${result.userId}`) já está casado!",
                                                Emotes.LoriSob
                                            )
                                        }
                                    }
                                    is RestoreMarriageResult.NotEnoughSonhos -> {
                                        context.reply(false) {
                                            styled(
                                                context.i18nContext.get(SonhosUtils.insufficientSonhos(MARRIAGE_RESTORE_COST.toLong(), result.userBalance)),
                                                Emotes.LoriSob
                                            )
                                        }
                                    }
                                    RestoreMarriageResult.Success -> {
                                        context.reply(false) {
                                            styled(
                                                "Casamento restaurado!"
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }

        sealed class RestorableMarriageQueryResult {
            data class Success(val marriage: ResultRow, val participants: List<ResultRow>) : RestorableMarriageQueryResult()
            data object NotFound : RestorableMarriageQueryResult()
        }

        sealed class RestoreMarriageResult {
            data object Success : RestoreMarriageResult()
            data class NotEnoughSonhos(val userBalance: Long) : RestoreMarriageResult()
            data class ParticipantIsAlreadyMarried(val userId: Long) : RestoreMarriageResult()
        }
    }

    class MarriageLetterExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        companion object {
            private val MESSENGERS = listOf(
                Pair("Loritta", "https://stuff.loritta.website/ship/loritta.png"),
                Pair("Pantufa", "https://stuff.loritta.website/ship/pantufa.png"),
                Pair("Gabriela", "https://stuff.loritta.website/ship/gabriela.png")
            )
        }

        inner class Options : ApplicationCommandOptions() {
            val message = string("message", I18N_PREFIX.Letter.Options.Message.Text, range = 5..500)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val message = args[options.message]

            val lettersSentToday = loritta.transaction {
                val todayAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .toInstant()

                val activeMarriage = MarriageParticipants
                    .innerJoin(UserMarriages)
                    .selectAll()
                    .where {
                        UserMarriages.active eq true and (MarriageParticipants.user eq context.user.idLong)
                    }
                    .firstOrNull()

                if (activeMarriage == null)
                    return@transaction 0

                MarriageLoveLetters.selectAll()
                    .where {
                        MarriageLoveLetters.marriage eq activeMarriage[UserMarriages.id] and (MarriageLoveLetters.sentBy eq context.user.idLong) and (MarriageLoveLetters.sentAt greaterEq todayAtMidnight)
                    }
                    .orderBy(MarriageLoveLetters.sentBy, SortOrder.DESC)
                    .count() + 1 // plus one if we are sending this one
            }

            val letterPrice = lettersSentToday * LOVE_LETTER_PRICE

            val button = loritta.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                context.i18nContext.get(I18N_PREFIX.Letter.SendLetter(letterPrice)),
                {
                    this.loriEmoji = Emotes.LoveLetter
                }
            ) { context ->
                context.deferChannelMessage(false)

                val result = loritta.transaction {
                    val todayAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .toInstant()

                    val activeMarriage = MarriageParticipants
                        .innerJoin(UserMarriages)
                        .selectAll()
                        .where {
                            UserMarriages.active eq true and (MarriageParticipants.user eq context.user.idLong)
                        }
                        .firstOrNull()

                    if (activeMarriage == null)
                        return@transaction MarriageLetterResult.YouAreNotMarried

                    val marriageParticipantsThatArentMe = MarriageParticipants.selectAll()
                        .where {
                            MarriageParticipants.marriage eq activeMarriage[MarriageParticipants.marriage] and (MarriageParticipants.user eq context.user.idLong)
                        }
                        .toList()

                    val lastLoveLetter = MarriageLoveLetters.selectAll()
                        .where {
                            MarriageLoveLetters.marriage eq activeMarriage[UserMarriages.id] and (MarriageLoveLetters.sentBy eq context.user.idLong)
                        }
                        .orderBy(MarriageLoveLetters.sentAt, SortOrder.DESC)
                        .firstOrNull()

                    if (lastLoveLetter != null && lastLoveLetter[MarriageLoveLetters.content].equals(message, true))
                        return@transaction MarriageLetterResult.MessageContentIdenticalToPreviousLetter

                    val lettersSentToday = MarriageLoveLetters.selectAll()
                        .where {
                            MarriageLoveLetters.marriage eq activeMarriage[UserMarriages.id] and (MarriageLoveLetters.sentBy eq context.user.idLong) and (MarriageLoveLetters.sentAt greaterEq todayAtMidnight)
                        }
                        .orderBy(MarriageLoveLetters.sentAt, SortOrder.DESC)
                        .count() + 1 // plus this one!

                    MarriageLoveLetters.insert {
                        it[MarriageLoveLetters.marriage] = activeMarriage[UserMarriages.id]
                        it[MarriageLoveLetters.content] = message
                        it[MarriageLoveLetters.sentAt] = Instant.now()
                        it[MarriageLoveLetters.sentBy] = context.user.idLong
                    }

                    UserMarriages.update({ UserMarriages.id eq activeMarriage[UserMarriages.id] }) {
                        it[UserMarriages.affinity] = UserMarriages.affinity + LOVE_LETTER_AFFINITY
                    }

                    val price = LOVE_LETTER_PRICE * lettersSentToday

                    val result = SonhosUtils.takeSonhosAndLogToTransactionLog(
                        context.user.idLong,
                        price,
                        TransactionType.MARRIAGE,
                        StoredMarriageLoveLetterTransaction
                    )

                    val newAffinity = activeMarriage[UserMarriages.affinity] + LOVE_LETTER_AFFINITY

                    val marriageAffinityRank = UserMarriages.selectAll()
                        .where { UserMarriages.active eq true and (UserMarriages.affinity greaterEq newAffinity) }
                        .orderBy(Pair(UserMarriages.affinity, SortOrder.ASC), Pair(UserMarriages.id, SortOrder.ASC))
                        .count()

                    when (result) {
                        is SonhosUtils.SonhosRemovalResult.NotEnoughSonhos -> {
                            return@transaction MarriageLetterResult.NotEnoughSonhos(result.balance)
                        }
                        SonhosUtils.SonhosRemovalResult.Success -> {
                            return@transaction MarriageLetterResult.Success(marriageParticipantsThatArentMe, newAffinity, marriageAffinityRank, price)
                        }
                    }
                }

                when (result) {
                    MarriageLetterResult.YouAreNotMarried -> {
                        context.reply(false) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.View.YouAreNotMarried(loritta.commandMentions.marriageMarry)),
                                Emotes.LoriSob
                            )
                        }
                    }
                    is MarriageLetterResult.NotEnoughSonhos -> {
                        context.reply(false) {
                            styled(
                                context.i18nContext.get(SonhosUtils.insufficientSonhos(LOVE_LETTER_PRICE, result.userBalance)),
                                Emotes.LoriSob
                            )
                        }
                    }
                    MarriageLetterResult.MessageContentIdenticalToPreviousLetter -> {
                        context.reply(false) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Letter.LetterContentIdenticalToPreviousLetter),
                                Emotes.LoriSob
                            )
                        }
                    }
                    is MarriageLetterResult.Success -> {
                        val messenger = MESSENGERS.random()

                        for (participant in result.marriageParticipantsThatArentMe) {
                            try {
                                val privateChannel = loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(participant[MarriageParticipants.user])
                                    ?: continue

                                privateChannel.sendMessage(
                                    MessageCreate {
                                        this.useComponentsV2 = true

                                        this.components += Container {
                                            +Section(
                                                Thumbnail(messenger.second)
                                            ) {
                                                +TextDisplay(
                                                    buildString {
                                                        appendLine("### ${Emotes.LoveLetter} ${context.i18nContext.get(I18N_PREFIX.Letter.PrivateMessage.ALoveLetter)}")
                                                        for (line in context.i18nContext.get(I18N_PREFIX.Letter.PrivateMessage.Intro(messenger.first))) {
                                                            if (line.isNotEmpty()) {
                                                                appendLine("*$line*")
                                                            } else {
                                                                appendLine()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        this.components += Container {
                                            this.accentColor = LorittaColors.LorittaPink.rgb

                                            +Section(
                                                Thumbnail(context.user.effectiveAvatarUrl)
                                            ) {
                                                +TextDisplay(
                                                    buildString {
                                                        for (line in message.lines()) {
                                                            appendLine("> $line")
                                                        }
                                                        appendLine("\\- ${context.user.asMention} ${Emotes.LoriHeartCombo1}${Emotes.LoriHeartCombo2}")
                                                    }
                                                )
                                            }
                                        }

                                        this.components += Container {
                                            +TextDisplay(
                                                buildString {
                                                    appendLine(context.i18nContext.get(I18N_PREFIX.Letter.PrivateMessage.AfterReadingTheLetterAffinity(LOVE_LETTER_AFFINITY, result.affinity, result.affinityRank)))
                                                    appendLine()
                                                    appendLine(context.i18nContext.get(I18N_PREFIX.Letter.PrivateMessage.CommandsUpsell( loritta.commandMentions.marriageRank, loritta.commandMentions.marriageLetter)))
                                                }
                                            )
                                        }
                                    }
                                ).queue()
                            } catch (e: Exception) {}
                        }

                        context.reply(false) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Letter.LetterSent(SonhosUtils.getSonhosEmojiOfQuantity(result.price), result.price, messenger.first, LOVE_LETTER_AFFINITY, result.affinity, result.affinityRank, loritta.commandMentions.marriageRank)),
                                Emotes.LoriHeart
                            )

                            styled(
                                context.i18nContext.get(I18N_PREFIX.Letter.LetterSentPrivateChannel),
                                Emotes.LoriLurk
                            )
                        }
                    }
                }
            }

            context.reply(false) {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = LorittaColors.LorittaPink.rgb

                    +Section(
                        Thumbnail("https://stuff.loritta.website/ship/loritta.png")
                    ) {
                        + TextDisplay(
                            buildString {
                                appendLine("### ${Emotes.LoveLetter} ${context.i18nContext.get(I18N_PREFIX.Letter.LoveLetter)}")
                                appendLine()
                                for (line in message.lines()) {
                                    appendLine("> $line")
                                }
                                appendLine()
                                for (line in context.i18nContext.get(I18N_PREFIX.Letter.Intro(LOVE_LETTER_AFFINITY, SonhosUtils.getSonhosEmojiOfQuantity(letterPrice), letterPrice, SonhosUtils.getSonhosEmojiOfQuantity(LOVE_LETTER_PRICE), LOVE_LETTER_PRICE))) {
                                    appendLine(line)
                                }
                            }
                        )
                    }
                }

                actionRow(button)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }

        private sealed class MarriageLetterResult {
            data class Success(val marriageParticipantsThatArentMe: List<ResultRow>, val affinity: Int, val affinityRank: Long, val price: Long) : MarriageLetterResult()
            data class NotEnoughSonhos(val userBalance: Long) : MarriageLetterResult()
            data object YouAreNotMarried : MarriageLetterResult()
            data object MessageContentIdenticalToPreviousLetter : MarriageLetterResult()
        }
    }

    class MarriageRankExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val rankType = string("rank_type", I18N_PREFIX.Rank.Options.RankType.Text) {
                choice(I18N_PREFIX.Rank.LongestMarriages, MarriageRankType.LONGEST.name)
                choice(I18N_PREFIX.Rank.AffinityMarriages, MarriageRankType.AFFINITY.name)
            }
            val page = optionalLong("page", I18N_PREFIX.Rank.Options.Page.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val type = MarriageRankType.valueOf(args[options.rankType])
            val page = (args[options.page]?.minus(1)?.coerceAtLeast(0)) ?: 0

            context.reply(false) {
                createRankMessage(context, type, page)()
            }
        }

        private suspend fun createRankMessage(
            context: UnleashedContext,
            rankType: MarriageRankType,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val result = loritta.transaction {
                val rm = mutableListOf<MarriageEntry>()

                val totalCount = UserMarriages.selectAll()
                    .where {
                        UserMarriages.active eq true
                    }
                    .count()

                val marriages = UserMarriages.selectAll()
                    .where {
                        UserMarriages.active eq true
                    }
                    .apply {
                        when (rankType) {
                            MarriageRankType.LONGEST -> orderBy(UserMarriages.createdAt, SortOrder.ASC)
                            MarriageRankType.AFFINITY -> orderBy(UserMarriages.affinity, SortOrder.DESC)
                        }
                    }
                    .limit(5)
                    .offset(page * 5)
                    .toList()

                for (marriage in marriages) {
                    val participants = MarriageParticipants.selectAll()
                        .where {
                            MarriageParticipants.marriage eq marriage[UserMarriages.id]
                        }
                        .toList()

                    rm.add(
                        MarriageEntry(
                            marriage,
                            participants
                        )
                    )
                }

                return@transaction MarriageRankResult(totalCount, rm)
            }

            val genericEntryGradient = BufferedImage(800, 106, BufferedImage.TYPE_INT_ARGB)
            RankingGenerator.drawEntryBackgroundGradient(genericEntryGradient)

            val now = Instant.now()

            val entries = mutableListOf<RankingGenerator.EntryRankInformation>()

            for (marriage in result.entries) {
                if (marriage.participants.size != 2)
                    error("Unsupported participants count! ${marriage.participants.size}")

                val participantsAsUserInfos = mutableListOf<CachedUserInfo?>()

                for (participantData in marriage.participants) {
                    val user = loritta.lorittaShards.retrieveUserInfoById(participantData[MarriageParticipants.user])

                    participantsAsUserInfos.add(user)
                }

                val participantNamesAsUsers = mutableListOf<String>()

                for (userInfo in participantsAsUserInfos) {
                    participantNamesAsUsers.add(userInfo?.globalName ?: userInfo?.name ?: "???")
                }

                val sorted = participantNamesAsUsers.sorted()

                val coupleNameByUserNames = ShipCommand.createShipName(sorted[0], sorted[1])
                val coupleNameByUserNamesSubtitle = participantNamesAsUsers.sorted().joinToString(" x ")

                // Creates a collage between the both marriage participants
                // When marriages support more than 2 participants, we will need to fix this somehow
                val avatar1 = participantsAsUserInfos[0]?.effectiveAvatarUrl?.let {
                    LorittaUtils.downloadImage(loritta, it)
                } ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR
                val avatar2 = participantsAsUserInfos[1]?.effectiveAvatarUrl?.let {
                    LorittaUtils.downloadImage(loritta, it)
                } ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR

                val avatarCollab = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
                val avatarCollabGraphics = avatarCollab.createGraphics()

                avatarCollabGraphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    InterpolationType.BILINEAR.graphics2DRenderingHint
                )
                avatarCollabGraphics.drawImage(
                    avatar1,
                    // destination
                    0,
                    0,
                    128,
                    256,
                    // source
                    0,
                    0,
                    avatar1.width / 2,
                    avatar1.height,
                    null
                )
                avatarCollabGraphics.drawImage(
                    avatar2,
                    // destination
                    128,
                    0,
                    256,
                    256,
                    // source
                    avatar2.width / 2,
                    0,
                    avatar2.width,
                    avatar2.height,
                    null
                )

                var subtitle = RankingGenerator.EntryRankInformation.EntryRankIconableSubtitle(
                    icon = null,
                    text = coupleNameByUserNamesSubtitle
                )

                val coupleBadgeId = marriage.data[UserMarriages.coupleBadge]
                if (coupleBadgeId != null) {
                    val equippedBadge = loritta.profileDesignManager.badges.firstOrNull { it.id == coupleBadgeId }

                    if (equippedBadge != null) {
                        var canUseBadge = false

                        for (participant in participantsAsUserInfos.filterNotNull()) {
                            val profile = loritta.getOrCreateLorittaProfile(participant.id)
                            val settings = loritta.transaction {
                                profile.settings
                            }

                            val deservesBadge = equippedBadge.checkIfUserDeservesBadge(
                                loritta.profileDesignManager.transformUserToProfileUserInfoData(
                                    participant,
                                    settings
                                ),
                                profile,
                                setOf()
                            )

                            if (deservesBadge) {
                                // We can get out after we find out that at least one of the participants can use the badge
                                canUseBadge = true
                                break
                            }
                        }

                        if (canUseBadge) {
                            subtitle = RankingGenerator.EntryRankInformation.EntryRankIconableSubtitle(
                                icon = equippedBadge.getImage(),
                                text = loritta.languageManager.defaultI18nContext.get(equippedBadge.titlePlural ?: equippedBadge.title) + " // " + coupleNameByUserNamesSubtitle
                            )
                        }
                    }
                }

                entries.add(
                    RankingGenerator.EntryRankInformation(
                        marriage.data[UserMarriages.coupleName] ?: coupleNameByUserNames,
                        subtitle,
                        when (rankType) {
                            MarriageRankType.LONGEST -> {
                                DateUtils.formatDateDiff(
                                    context.i18nContext,
                                    marriage.data[UserMarriages.createdAt],
                                    now
                                )
                            }
                            MarriageRankType.AFFINITY -> context.i18nContext.get(I18N_PREFIX.Rank.AffinityPoints(marriage.data[UserMarriages.affinity]))
                        },
                        avatarCollab,
                        genericEntryGradient
                    )
                )
            }

            // Calculates the max page
            val maxPage = ceil(result.totalCount / 5.0)

            val ranking = RankingGenerator.generateRanking(
                loritta,
                page * 5,
                when (rankType) {
                    MarriageRankType.LONGEST -> context.i18nContext.get(I18N_PREFIX.Rank.LongestMarriages)
                    MarriageRankType.AFFINITY -> context.i18nContext.get(I18N_PREFIX.Rank.AffinityMarriages)
                },
                null,
                readImageFromResources("/marriages/marriage_rank_background.png"),
                entries
            )

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                ranking
            ) {
                createRankMessage(context, rankType, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }

        private data class MarriageRankResult(val totalCount: Long, val entries: List<MarriageEntry>)

        private data class MarriageEntry(
            val data: ResultRow,
            val participants: List<ResultRow>
        )

        enum class MarriageRankType {
            LONGEST,
            AFFINITY
        }
    }

    class MarriageConfigureExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val marriage = context.loritta.pudding.marriages.getMarriageByUser(UserId(context.user.idLong))

            if (marriage == null) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.View.YouAreNotMarried(loritta.commandMentions.marriageMarry)),
                        Emotes.LoriSob
                    )
                }
                return
            }

            val screen = MarriageConfigureScreen.General(loritta, marriage.id)

            context.reply(false, screen.render(context))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }
}