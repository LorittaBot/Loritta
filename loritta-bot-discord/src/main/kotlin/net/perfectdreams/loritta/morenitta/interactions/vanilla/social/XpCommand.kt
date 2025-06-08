package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*
import kotlin.math.ceil

class XpCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val XP_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Xpview
        val XP_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Xprank
        val XP_EDIT_I18N_PREFIX = I18nKeysData.Commands.Command.Xpedit
        val XP_TRANSFER_I18N_PREFIX = I18nKeysData.Commands.Command.Xptransfer
        val XP_NOTIFICATIONS_I18N_PREFIX = I18nKeysData.Commands.Command.Xpnotifications
        val I18N_PREFIX = I18nKeysData.Commands.Command.Xp
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("1d3edb96-4485-4249-a365-4a772bb02f0c")) {
        isGuildOnly = true
        enableLegacyMessageSupport = true

        val viewXpExecutor = ViewXpExecutor()
        executor = viewXpExecutor

        subcommand(XP_VIEW_I18N_PREFIX.Label, XP_VIEW_I18N_PREFIX.Description, UUID.fromString("3e0b3704-3c10-42c7-8b90-54a02db05751")) {
            executor = viewXpExecutor
        }

        subcommand(XP_NOTIFICATIONS_I18N_PREFIX.Label, XP_NOTIFICATIONS_I18N_PREFIX.Description,UUID.fromString("7c4d1ef0-6c2a-4ac6-8b4f-9dbba03005f2")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("xpnotifications")
                add("xpnotificações")
            }

            executor = XpNotificationsExecutor()
        }

        subcommand(XP_RANK_I18N_PREFIX.Label, XP_RANK_I18N_PREFIX.Description, UUID.fromString("5bc39edc-2dc1-462b-9b96-15ea27b168f9")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("rank")
                add("top")
                add("leaderboard")
                add("ranking")
            }

            executor = XpRankExecutor()
        }

        subcommand(XP_EDIT_I18N_PREFIX.Label, XP_EDIT_I18N_PREFIX.Description, UUID.fromString("2aa975a7-23b1-430c-a1fb-4c0991af4e3b")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("editarxp")
                add("setxp")
                add("editxp")
            }

            executor = EditXpExecutor()
        }

        subcommand(XP_TRANSFER_I18N_PREFIX.Label, XP_TRANSFER_I18N_PREFIX.Description, UUID.fromString("0ec48ce1-967e-422d-8147-eded127d5913")) {
            executor = TransferXpExecutor()
        }
    }

    inner class XpNotificationsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val newValue = loritta.newSuspendedTransaction {
                context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm =
                    !context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm

                context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm
            }

            if (newValue) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(XP_NOTIFICATIONS_I18N_PREFIX.DisabledNotifications),
                        Emotes.LoriSmile
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(XP_NOTIFICATIONS_I18N_PREFIX.EnabledNotifications),
                        Emotes.LoriSmile
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    inner class ViewXpExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", XP_VIEW_I18N_PREFIX.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            // TODO: Check if the user is banned
            val userAndMember = args[options.user]
            val userToBeViewed: User
            val memberToBeViewed: Member?

            if (userAndMember != null) {
                userToBeViewed = userAndMember.user
                memberToBeViewed = userAndMember.member
            } else {
                userToBeViewed = context.user
                memberToBeViewed = context.member
            }

            context.deferChannelMessage(false)

            val guildId = context.guild.idLong

            context.reply(false) {
                embed {
                    author(userToBeViewed.asTag, null, userToBeViewed.effectiveAvatar.url)

                    title = "${Emotes.LoriIdentificationCard} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.ServerProfileCard)}"

                    val localProfile = loritta.pudding.transaction {
                        GuildProfiles.selectFirstOrNull { (GuildProfiles.guildId eq guildId.toLong()) and (GuildProfiles.userId eq userToBeViewed.id.toLong()) }
                    }

                    val xp = localProfile?.get(GuildProfiles.xp) ?: 0

                    val level = ExperienceUtils.getCurrentLevelForXp(xp)

                    val currentLevelTotalXp = ExperienceUtils.getLevelExperience(level)

                    val nextLevel = level + 1
                    val nextLevelTotalXp = ExperienceUtils.getLevelExperience(nextLevel)
                    val nextLevelRequiredXp = ExperienceUtils.getHowMuchExperienceIsLeftToLevelUp(xp, nextLevel)

                    val ranking = loritta.pudding.transaction {
                        GuildProfiles.selectAll().where {
                            GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.xp greaterEq xp) and (GuildProfiles.isInGuild eq true)
                        }.count()
                    }

                    val nextRoleReward = loritta.pudding.transaction {
                        RolesByExperience.selectAll().where {
                            RolesByExperience.guildId eq guildId.toLong() and (RolesByExperience.requiredExperience greater xp)
                        }.orderBy(RolesByExperience.requiredExperience).firstOrNull()
                    }

                    val activeRoleRate = memberToBeViewed?.let {
                        loritta.pudding.transaction {
                            ExperienceRoleRates.selectAll().where {
                                ExperienceRoleRates.guildId eq guildId.toLong() and (ExperienceRoleRates.role inList it.roles.map { it.idLong })
                            }.orderBy(ExperienceRoleRates.rate, SortOrder.DESC).firstOrNull()
                        }
                    }

                    field("${Emotes.LoriSunglasses} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.CurrentLevel)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.Level(level)), true)
                    field("${Emotes.LoriStonks} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.CurrentXp)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.Xp(xp)), true)
                    field("${Emotes.LoriReading} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.Placement)}", "#${ranking}", true)
                    field("${Emotes.LoriZap} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.XpNeededForTheNextLevel(nextLevel, nextLevelTotalXp))}", nextLevelRequiredXp.toString(), true)

                    if (nextRoleReward != null) {
                        val rolesMentions = nextRoleReward[RolesByExperience.roles].joinToString(", ") { "<@&${it}>" }
                        val diff = nextRoleReward[RolesByExperience.requiredExperience] - xp

                        field("${Emotes.LoriCard} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.NextReward)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.GetXPToEarnRoles(diff, rolesMentions)), true)
                    }

                    if (activeRoleRate != null) {
                        field("${Emotes.LoriHappy} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.BonusXPForRoles)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.BecauseYouHaveRole("<@&${activeRoleRate[ExperienceRoleRates.role]}>", activeRoleRate[ExperienceRoleRates.rate])), true)
                    }

                    field("${Emotes.LoriHi} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.LoriTipsAndTricks)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.KeepTalkingToEarnXp), false)

                    color = LorittaColors.LorittaAqua.rgb
                }

                // TODO: "View profile button"
                actionRow(
                    Button.link(
                        "${loritta.config.loritta.website.url}extras/faq-loritta/experience",
                        context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.LearnAboutXpButton)
                    ).withEmoji(Emoji.fromFormatted(Emotes.LoriReading.asMention))
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.user to context.getUserAndMember(0)
            )
        }
    }

    inner class EditXpExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val mode = string("mode", XP_EDIT_I18N_PREFIX.Options.Mode.Text) {
                for (mode in XpModificationMode.values()) {
                    choice(mode.shortName, mode.name)
                }
            }

            val user = user("user", XP_EDIT_I18N_PREFIX.Options.User.Text)

            val value = long("value", XP_EDIT_I18N_PREFIX.Options.Value.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.hasPermission(Permission.MANAGE_SERVER)) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.UserDoesntHavePermissionDiscord(
                                context.i18nContext.get(I18nKeysData.Permissions.ManageGuild)
                            )
                        ),
                        Emotes.LoriZap
                    )
                }
            }

            val userToBeEdited = args[options.user]
            val xpValue = args[options.value]

            context.deferChannelMessage(false)

            val guildId = context.guild.idLong

            val userAndMember = args[options.user]
            val userIsMember = userAndMember.member != null
            val localConfig = loritta.getOrCreateServerConfig(guildId)

            val localProfile = localConfig.getUserData(loritta, userToBeEdited.user.idLong, userIsMember)
            val oldUserXp = localProfile.xp

            var newXpValue = when (XpModificationMode.valueOf(args[options.mode])) {
                XpModificationMode.SET -> { xpValue }
                XpModificationMode.ADD -> { oldUserXp+xpValue }
                XpModificationMode.REMOVE -> { oldUserXp-xpValue }
            }

            if (newXpValue < 0) { newXpValue = 0 }

            loritta.pudding.transaction {
                GuildProfiles.update({ GuildProfiles.guildId eq guildId and (GuildProfiles.userId eq userToBeEdited.user.idLong) }) {
                    it[xp] = newXpValue
                }
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        XpCommand.XP_EDIT_I18N_PREFIX.EditedXp(userToBeEdited.user.asMention, oldUserXp, newXpValue)
                    ),
                    Emotes.LoriShining
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val declarationPath = loritta.interactionsListener.manager.findDeclarationPath(context.commandDeclaration)

            val fullLabel = buildString {
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(context.i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(context.i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim()

            val modeAsString = args.getOrNull(0)
            val mode = modeAsString?.let {
                XpModificationMode.values().firstOrNull { context.i18nContext.get(it.shortName).normalize().lowercase() == modeAsString.normalize().lowercase() }
            }

            if (mode == null) {
                context.reply(true) {
                    /* styled(
                        context.i18nContext.get(I18N_PREFIX.Status.YouNeedToSelectWhatRaffleTypeYouWant),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSleeping
                    ) */

                    for (availableModificationMode in XpModificationMode.values()) {
                        styled("**${context.i18nContext.get(availableModificationMode.shortName)}:** `${context.config.commandPrefix}$fullLabel ${context.i18nContext.get(availableModificationMode.shortName).lowercase()}`")
                    }
                }
                return null
            }

            val userAndMember = context.getUserAndMember(1)
            val quantity = args.getOrNull(2)?.toLongOrNull()

            if (userAndMember == null || quantity == null) {
                context.explain()
                return null
            }

            context.mentions.injectUser(userAndMember.user)

            return mapOf(
                options.mode to mode.name,
                options.user to userAndMember,
                options.value to quantity
            )
        }
    }

    inner class TransferXpExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val mode = string("mode", XP_TRANSFER_I18N_PREFIX.Options.Mode.Text) {
                for (mode in XpTransferMode.values()) {
                    choice(mode.shortName, mode.name)
                }
            }

            val user = user("user", XP_TRANSFER_I18N_PREFIX.Options.User.Text)

            val target = user("target", XP_TRANSFER_I18N_PREFIX.Options.Target.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.hasPermission(Permission.MANAGE_SERVER)) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.UserDoesntHavePermissionDiscord(
                                context.i18nContext.get(I18nKeysData.Permissions.ManageGuild)
                            )
                        ),
                        Emotes.LoriZap
                    )
                }
            }

            val userToBeEdited = args[options.user]
            val targetToBeEdited = args[options.target]

            context.deferChannelMessage(false)

            if (userToBeEdited.user.id == targetToBeEdited.user.id) {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(XpCommand.XP_TRANSFER_I18N_PREFIX.UserIsTheSameAsTarget),
                        Emotes.LoriFire
                    )
                }
            }

            val guildId = context.guild.idLong
            val filter = GuildProfiles.guildId eq guildId

            val userProfile = loritta.pudding.transaction { GuildProfiles.selectFirstOrNull { (filter and (GuildProfiles.userId eq userToBeEdited.user.idLong)) } }
            val targetProfile = loritta.pudding.transaction { GuildProfiles.selectFirstOrNull { (filter and (GuildProfiles.userId eq targetToBeEdited.user.idLong)) } }

            if (userProfile?.get(GuildProfiles.xp)!! <= 0) {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(XpCommand.XP_TRANSFER_I18N_PREFIX.UserHas0Xp),
                        Emotes.LoriHm
                    )
                }
            }

            val xpTransferMode = XpTransferMode.valueOf(args[options.mode])
            val targetXp = when (xpTransferMode) {
                XpTransferMode.ADD -> { (userProfile.get(GuildProfiles.xp)) + (targetProfile?.get(GuildProfiles.xp)?: 0) }
                XpTransferMode.SET -> { (userProfile.get(GuildProfiles.xp)) }
            }

            loritta.pudding.transaction {
                GuildProfiles.update({ filter and (GuildProfiles.userId eq userToBeEdited.user.id.toLong()) }) { it[xp] = 0  }
                GuildProfiles.update({ filter and (GuildProfiles.userId eq targetToBeEdited.user.id.toLong() )}) { it[xp] = targetXp }
            }

            if (xpTransferMode == XpTransferMode.SET) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            XpCommand.XP_TRANSFER_I18N_PREFIX.TranferedXp(
                                userToBeEdited.user.asMention,
                                userProfile.get(GuildProfiles.xp), targetToBeEdited.user.asMention, (targetProfile?.get(GuildProfiles.xp) ?: 0)
                            )
                        ),
                        Emotes.LoriShining
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            XpCommand.XP_TRANSFER_I18N_PREFIX.TransferedXpWithAddition(
                                userToBeEdited.user.asMention, userProfile.get(GuildProfiles.xp), targetToBeEdited.user.asMention
                            )
                        ),
                        Emotes.LoriShining
                    )
                    styled(
                        context.i18nContext.get(
                            XpCommand.XP_TRANSFER_I18N_PREFIX.AdditionInfo(
                                targetToBeEdited.user.asMention, (targetProfile?.get(GuildProfiles.xp)?: 0), targetXp
                            )
                        ),
                        Emotes.LoriSunglasses
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val declarationPath = loritta.interactionsListener.manager.findDeclarationPath(context.commandDeclaration)

            val fullLabel = buildString {
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(context.i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(context.i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim()

            val modeAsString = args.getOrNull(0)
            val mode = modeAsString?.let {
                XpTransferMode.values().firstOrNull { context.i18nContext.get(it.shortName).normalize().lowercase() == modeAsString.normalize().lowercase() }
            }

            if (mode == null) {
                context.reply(true) {
                    /* styled(
                        context.i18nContext.get(I18N_PREFIX.Status.YouNeedToSelectWhatRaffleTypeYouWant),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSleeping
                    ) */

                    for (availableModificationMode in XpTransferMode.values()) {
                        styled("**${context.i18nContext.get(availableModificationMode.shortName)}:** `${context.config.commandPrefix}$fullLabel ${context.i18nContext.get(availableModificationMode.shortName).lowercase()}`")
                    }
                }
                return null
            }

            val userAndMember = context.getUserAndMember(1)
            val userAndMemberTarget = context.getUserAndMember(2)

            if (userAndMember == null || userAndMemberTarget == null) {
                context.explain()
                return null
            }

            context.mentions.injectUser(userAndMember.user)
            context.mentions.injectUser(userAndMemberTarget.user)

            return mapOf(
                options.mode to mode.name,
                options.user to userAndMember,
                options.target to userAndMemberTarget
            )
        }
    }

    inner class XpRankExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val guild = context.guild

            val userPage = args[options.page] ?: 1L
            val page = userPage - 1

            val message = createMessage(
                loritta,
                context,
                guild,
                page
            )

            context.reply(false) {
                message()
            }
        }

        suspend fun createMessage(
            loritta: LorittaBot,
            context: UnleashedContext,
            guild: Guild,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val (totalCount, profiles) = loritta.pudding.transaction {
                val totalCount = GuildProfiles.selectAll().where {
                    (GuildProfiles.guildId eq guild.id.toLong()) and
                            (GuildProfiles.isInGuild eq true)
                }.count()

                val profilesInTheQuery = GuildProfiles.selectAll().where {
                    (GuildProfiles.guildId eq guild.id.toLong()) and
                            (GuildProfiles.isInGuild eq true)
                }
                    .orderBy(GuildProfiles.xp to SortOrder.DESC)
                    .limit(5)
                    .offset(page * 5)
                    .toList()

                Pair(totalCount, profilesInTheQuery)
            }

            // Calculates the max page
            val maxPage = ceil(totalCount / 5.0)

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    guild.name,
                    context.guild.getIconUrl(256, ImageFormat.PNG),
                    profiles.map {
                        val xp = it[GuildProfiles.xp]
                        val level = ExperienceUtils.getCurrentLevelForXp(xp)

                        RankingGenerator.UserRankInformation(
                            it[GuildProfiles.userId],
                            context.i18nContext.get(XpCommand.XP_RANK_I18N_PREFIX.TotalXpAndLevel(xp, level))
                        )
                    }
                ) {
                    loritta.pudding.transaction {
                        GuildProfiles.update({ GuildProfiles.id eq it.toLong() and (GuildProfiles.guildId eq guild.id.toLong()) }) {
                            it[isInGuild] = false
                        }
                    }
                    null
                }
            ) {
                createMessage(loritta, context, guild, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val page = context.args.getOrNull(0)?.toLongOrNull()

            if (page != null && !RankingGenerator.isValidRankingPage(page)) {
                context.reply(false) {
                    styled(
                        context.locale["commands.invalidRankingPage"],
                        Constants.ERROR
                    )
                }
                return null
            }

            return mapOf(
                options.page to page
            )
        }
    }

    enum class XpModificationMode(val shortName: StringI18nData) {
        ADD(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Add),
        REMOVE(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Remove),
        SET(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Set)
    }

    enum class XpTransferMode(val shortName: StringI18nData) {
        ADD(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Add),
        SET(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Set)
    }
}