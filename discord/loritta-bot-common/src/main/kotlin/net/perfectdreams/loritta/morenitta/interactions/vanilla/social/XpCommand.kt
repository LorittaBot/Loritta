package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ExperienceUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.RaffleCommand
import net.perfectdreams.loritta.morenitta.utils.normalize
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class XpCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val XP_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Xpview
        val XP_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Xprank
        val XP_EDIT_I18N_PREFIX = I18nKeysData.Commands.Command.Xpedit
        val XP_TRANSFER_I18N_PREFIX = I18nKeysData.Commands.Command.Xptransfer
        val I18N_PREFIX = I18nKeysData.Commands.Command.Xp
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.SOCIAL) {
        isGuildOnly = true
        enableLegacyMessageSupport = true

        val viewXpExecutor = ViewXpExecutor()
        executor = viewXpExecutor

        subcommand(XP_VIEW_I18N_PREFIX.Label, XP_VIEW_I18N_PREFIX.Description) {
            executor = viewXpExecutor
        }

        /* subcommand(XP_RANK_I18N_PREFIX.Label, XP_RANK_I18N_PREFIX.Description) {
            executor = { XpRankExecutor(it) }
        }

        */
        subcommand(XP_EDIT_I18N_PREFIX.Label, XP_EDIT_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("editarxp")
                add("setxp")
                add("editxp")
            }

            executor = EditXpExecutor()
        }

        /* subcommand(XP_TRANSFER_I18N_PREFIX.Label, XP_TRANSFER_I18N_PREFIX.Description) {
            executor = { TransferXpExecutor(it) }
        } */
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
                        GuildProfiles.select {
                            GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.xp greaterEq xp) and (GuildProfiles.isInGuild eq true)
                        }.count()
                    }

                    val nextRoleReward = loritta.pudding.transaction {
                        RolesByExperience.select {
                            RolesByExperience.guildId eq guildId.toLong() and (RolesByExperience.requiredExperience greater xp)
                        }.orderBy(RolesByExperience.requiredExperience).firstOrNull()
                    }

                    val activeRoleRate = memberToBeViewed?.let {
                        loritta.pudding.transaction {
                            ExperienceRoleRates.select {
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
                        // We do "Rate - 1.0" because, if we have a Rate of 1.1x, then it means that the user has a 10% XP boost, not a 110% XP boost.
                        field("${Emotes.LoriHappy} ${context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.BonusXPForRoles)}", context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.BecauseYouHaveRole("<@&${activeRoleRate[ExperienceRoleRates.role]}>", activeRoleRate[ExperienceRoleRates.rate] - 1.0)), true)
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

            var newXpValue = when (args[options.mode]) {
                "SET" -> { xpValue }
                "ADD" -> { oldUserXp+xpValue }
                "REMOVE" -> { oldUserXp-xpValue }
                else -> { xpValue }
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
                options.mode to mode,
                options.user to userAndMember,
                options.value to quantity
            )
        }
    }

    enum class XpModificationMode(val shortName: StringI18nData) {
        ADD(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Add),
        REMOVE(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Remove),
        SET(XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Set)
    }
}