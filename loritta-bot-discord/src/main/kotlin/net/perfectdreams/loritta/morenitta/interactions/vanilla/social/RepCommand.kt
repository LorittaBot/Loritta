package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.stripLinks
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded
import net.perfectdreams.loritta.serializable.Reputation
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class RepCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Rep
        private const val ENTRIES_PER_PAGE = 10
        private const val LOCALE_PREFIX = "commands.command.replist"
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("912b9e16-bb52-47eb-afba-87ffabe9b6c9")) {
        this.enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Give.Label, I18N_PREFIX.Give.Description, UUID.fromString("fb361757-9bf8-443c-83bb-733f24f482ae")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("reputation")
                this.add("reputação")
                this.add("reputacao")
                this.add("rep")
            }

            executor = GiveRepExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Delete.Label, I18N_PREFIX.Delete.Description, UUID.fromString("76c36a28-7eb1-4dac-86df-36b4da6cf13c")) {
            executor = DeleteRepExecutor(loritta)
        }

        subcommand(I18N_PREFIX.On.Label, I18N_PREFIX.On.Description, UUID.fromString("eaf66820-f596-4bda-9d98-589d9b20abee")) {
            executor = RepOnExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Off.Label, I18N_PREFIX.Off.Description, UUID.fromString("8ba3c346-7c58-447b-ad69-69ed9e80042e")) {
            executor = RepOffExecutor(loritta)
        }

        subcommand(I18N_PREFIX.List.Label, I18N_PREFIX.List.Description, UUID.fromString("e1c548c1-5ac5-494d-b9a0-8c9987bc800b")) {
            this.alternativeLegacyLabels.apply {
                add("list")
                add("lista")
            }

            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("reps")
                add("reputations")
                add("reputações")
                add("reputacoes")
            }

            executor = RepListExecutor(loritta)
        }
    }

    class GiveRepExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18N_PREFIX.Give.Options.User.Description)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val user = args[options.user].user

            if (context.user.id == user.id) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(I18N_PREFIX.Give.RepSelf),
                        Emotes.Error
                    )
                }
            }

            if (AccountUtils.getUserTodayDailyReward(loritta, context.user.idLong) == null) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.YouNeedToGetDailyRewardBeforeDoingThisAction(
                                context.loritta.commandMentions.daily
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }

            val lastReputationGiven = context.loritta.pudding.reputations.getGivenReputationsByUser(context.user.id.toLong())
                .maxByOrNull { it.receivedAt }

            if (lastReputationGiven != null) {
                val diff = Clock.System.now().toEpochMilliseconds() - lastReputationGiven.receivedAt

                if (3_600_000 > diff) {
                    context.fail(true) {
                        this.styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Give.Wait(
                                    "<t:${(lastReputationGiven.receivedAt + 3.6e+6.toLong()) / 1000}:R>"
                                )
                            ),
                            Emotes.Error
                        )
                    }
                }
            }

            val reputationsEnabled = loritta.transaction {
                Profile.findById(user.idLong)?.settings?.reputationsEnabled ?: true
            }

            if (!reputationsEnabled) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Give.UserHasDisabledReputations(user.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            var url = "${context.loritta.config.loritta.website.url}user/${user.id}/rep"

            if (context.guildId != null)
                url += "?guild=${context.guildId}&channel=${context.channel.idLong}"

            context.reply(true) {
                this.styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Give.ReputationLink(url)
                    ),
                    Emotes.LoriHeart
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val userAndMember = context.getUserAndMember(0)
            if (userAndMember == null) {
                context.explain()
                return null
            }

            return mapOf(options.user to userAndMember)
        }
    }

    class DeleteRepExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val rep = string("rep", I18N_PREFIX.Delete.Options.Rep.Description) {
                autocomplete { context ->
                    val reputations = loritta.transaction {
                        Reputations.selectAll()
                            .where {
                                Reputations.receivedById eq context.event.user.idLong and (Reputations.content.like("%${context.event.focusedOption.value.replace("%", "")}%"))
                            }
                            .orderBy(Reputations.receivedAt, SortOrder.DESC)
                            .limit(DiscordResourceLimits.Command.Options.ChoicesCount)
                            .map {
                                // TODO: This is from Pudding's Service class, we moved it here because it is a extension method there, can't we refactor this?
                                PuddingReputation(
                                    loritta.pudding,
                                    Reputation(
                                        it[Reputations.id].value,
                                        it[Reputations.givenById],
                                        it[Reputations.givenByIp],
                                        it[Reputations.givenByEmail],
                                        it[Reputations.receivedById],
                                        it[Reputations.receivedAt],
                                        it[Reputations.content],
                                    )
                                )
                            }
                    }

                    return@autocomplete reputations
                        .associate { formatReputation(it, context) to it.id.toString() }
                        .entries
                        .associate { it.key to it.value }
                }
            }

            private suspend fun formatReputation(reputation: PuddingReputation, context: AutocompleteContext): String {
                return buildString {
                    val givenAtTime = Instant.fromEpochMilliseconds(reputation.receivedAt)
                        .toLocalDateTime(Constants.LORITTA_TIMEZONE.toKotlinTimeZone())

                    val year = givenAtTime.year
                    val month = givenAtTime.month.value.toString().padStart(2, '0')
                    val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
                    val hour = givenAtTime.hour.toString().padStart(2, '0')
                    val minute = givenAtTime.minute.toString().padStart(2, '0')

                    append("[$day/$month/$year $hour:$minute] ")

                    HarmonyLoggerFactory.logger {}.value.info { "RepCommand#retrieveUserInfoById - UserId: ${reputation.givenById}" }
                    val user = context.loritta.lorittaShards.retrieveUserInfoById(reputation.givenById)
                    if (user == null)
                        append("Unknown: ")
                    else
                        append("${user.name}: ")

                    val reputationContent = reputation.content
                    if (reputationContent != null) {
                        append(
                            reputationContent.stripCodeMarks()
                                // Strip new lines and replace them with " "
                                .stripLinks()
                                .replace(Regex("[\\r\\n]"), " ")
                                .substringIfNeeded(0..250)
                        )
                    } else {
                        append("*vazio*")
                    }
                }.shortenAndStripCodeBackticks(DiscordResourceLimits.Command.Options.Description.Length)
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val reputation = context.loritta.pudding.reputations.getReputation(args[options.rep].toLong())

            if (reputation == null || reputation.receivedById != context.user.id.toLong()) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(I18N_PREFIX.Delete.ReputationNotFound),
                        Emotes.Error
                    )
                }
            }

            reputation.delete()

            context.reply(true) {
                this.styled(
                    context.i18nContext.get(I18N_PREFIX.Delete.SuccessfullyDeleted),
                    Emotes.LoriHappy
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            TODO("Not yet implemented")
        }
    }

    class RepOnExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            loritta.transaction {
                context.lorittaUser.profile.settings.reputationsEnabled = true
            }

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.On.ReputationsEnabled)
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }

    class RepOffExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            loritta.transaction {
                context.lorittaUser.profile.settings.reputationsEnabled = false
            }

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Off.ReputationsDisabled)
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }

    class RepListExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", TodoFixThisData)
            val page = optionalLong("page", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val customPageOneIndex = (args[options.page] ?: 1)

            if (!RankingGenerator.isValidRankingPage(customPageOneIndex)) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.transactions.pageDoesNotExist"],
                        Constants.ERROR
                    )
                }
                return
            }

            val customPage = customPageOneIndex - 1

            val user = args[options.user]?.user ?: context.user

            context.deferChannelMessage(false)

            val totalReputationReceived = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.receivedById eq user.idLong
                }.count()
            }

            val totalReputationGiven = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.givenById eq user.idLong
                }.count()
            }

            if ((totalReputationGiven + totalReputationReceived) == 0L) {
                if (context.user == user) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.List.YouHaveNoReps),
                            Emotes.LoriSob
                        )
                    }
                } else {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.List.UserHasNoReps),
                            Emotes.LoriSob
                        )
                    }
                }
                return
            }

            val builder = createRepListMessage(
                context,
                user,
                customPage
            )

            context.reply(false, builder)
        }

        suspend fun createRepListMessage(
            context: UnleashedContext,
            user: User,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) {
            val locale = context.locale

            val reputations = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
                }.orderBy(Reputations.receivedAt, SortOrder.DESC)
                    .limit(ENTRIES_PER_PAGE)
                    .offset(page * ENTRIES_PER_PAGE)
                    .toList()
            }

            val totalReputationReceived = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.receivedById eq user.idLong
                }.count()
            }

            val totalReputationGiven = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.givenById eq user.idLong
                }.count()
            }

            val description = buildString {
                if (reputations.size == 0) {
                    this.append(locale["$LOCALE_PREFIX.noReps"])
                } else {
                    this.append(locale["$LOCALE_PREFIX.reputationsTotalDescription", totalReputationReceived, totalReputationGiven])
                    this.append("\n")
                    this.append("\n")

                    for (reputation in reputations) {
                        // Needed for checking if the string don't bypass 2048 chars limit
                        val str = StringBuilder()

                        val receivedReputation = reputation[Reputations.receivedById] == user.idLong

                        val givenAtTime = java.time.Instant.ofEpochMilli(reputation[Reputations.receivedAt]).atZone(Constants.LORITTA_TIMEZONE)
                        val year = givenAtTime.year
                        val month = givenAtTime.monthValue.toString().padStart(2, '0')
                        val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
                        val hour = givenAtTime.hour.toString().padStart(2, '0')
                        val minute = givenAtTime.minute.toString().padStart(2, '0')
                        str.append("`[$day/$month/$year $hour:$minute]` ")

                        val emoji = if (receivedReputation)
                            "\uD83D\uDCE5"
                        else
                            "\uD83D\uDCE4"
                        str.append(emoji)
                        str.append(" ")

                        val receivedByUserId = if (receivedReputation) {
                            reputation[Reputations.givenById]
                        } else {
                            reputation[Reputations.receivedById]
                        }

                        HarmonyLoggerFactory.logger {}.value.info { "RepListCommand#retrieveUserInfoById - UserId: $receivedByUserId" }
                        val receivedByUser = loritta.lorittaShards.retrieveUserInfoById(receivedByUserId)

                        val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")
                        val content = reputation[Reputations.content]?.stripCodeMarks()
                            // Strip new lines and replace them with " "
                            ?.stripLinks()
                            ?.replace(Regex("[\\r\\n]"), " ")
                            ?.substringIfNeeded(0..250)

                        val receivedByLoritta = reputation[Reputations.givenById] == loritta.config.loritta.discord.applicationId.toString().toLong()
                        if (receivedByLoritta) {
                            str.append(locale["$LOCALE_PREFIX.receivedReputationByLoritta", "`${user.name + "#" + user.discriminator}`"])
                        } else {
                            if (receivedReputation) {
                                if (content.isNullOrBlank()) {
                                    str.append(locale["$LOCALE_PREFIX.receivedReputation", "`${name}`"])
                                } else {
                                    str.append(locale["$LOCALE_PREFIX.receivedReputationWithContent", "`${name}`", "`$content`"])
                                }
                            } else {
                                if (content.isNullOrBlank()) {
                                    str.append(locale["$LOCALE_PREFIX.sentReputation", "`${name}`"])
                                } else {
                                    str.append(locale["$LOCALE_PREFIX.sentReputationWithContent", "`${name}`", "`$content`"])
                                }
                            }
                        }
                        str.append("\n")

                        // If it's not bypassing the 2048 chars limit, then append
                        if (this.length + str.length <= 2048) {
                            this.append(str)
                        } else {
                            // If else, stop appending strings
                            return@buildString
                        }
                    }
                }
            }

            val totalReps = totalReputationGiven + totalReputationReceived

            // We don't want the user to see more than 100 pages of reputation
            val allowForward = totalReps >= (page + 1) * ENTRIES_PER_PAGE && (100 > page)
            val allowBack = page != 0L

            return {
                embed {
                    this.title = buildString {
                        append("${net.perfectdreams.loritta.common.utils.Emotes.LORI_RICH} ")
                        if (context.user != user)
                            append("${locale["$LOCALE_PREFIX.otherUserRepList", user.asTag]} — ${locale["commands.command.transactions.page"]} ${page + 1}")
                        else
                            append("${locale["$LOCALE_PREFIX.title"]} — ${locale["commands.command.transactions.page"]} ${page + 1}")
                    }
                    this.description = description

                    color = LorittaColors.LorittaAqua.rgb
                }

                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        false, // TODO: This should be context.alwaysEphemeral when this is migrated to InteraKTions Unleashed
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronLeft
                            disabled = !allowBack
                        }
                    ) {
                        val hook = it.updateMessageSetLoadingState(updateMessageContent = false)

                        val builtMessage = createRepListMessage(
                            context,
                            user,
                            page - 1
                        )

                        hook.editOriginal(
                            MessageEdit {
                                builtMessage()
                            }
                        ).await()
                    },
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        false, // TODO: This should be context.alwaysEphemeral when this is migrated to InteraKTions Unleashed
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronRight
                            disabled = !allowForward
                        }
                    ) {
                        val hook = it.updateMessageSetLoadingState(updateMessageContent = false)

                        val builtMessage = createRepListMessage(
                            context,
                            user,
                            page + 1
                        )

                        hook.editOriginal(
                            MessageEdit {
                                builtMessage()
                            }
                        ).await()
                    }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val userAndMember = context.getUserAndMember(0)

            var page: Long?
            if (userAndMember != null)
                page = args.getOrNull(1)?.toLongOrNull()
            else
                page = args.getOrNull(0)?.toLongOrNull()

            return mapOf(
                options.user to userAndMember,
                options.page to page
            )
        }
    }
}