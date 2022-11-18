package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import dev.kord.core.entity.Member
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ExperienceUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.LorittaColors
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class ViewXpExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", XpCommand.XP_VIEW_I18N_PREFIX.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        // TODO: Check if the user is banned
        val userToBeViewed = args[options.user] ?: context.member
        val memberToBeViewed = userToBeViewed as? Member

        context.deferChannelMessage()

        val guildId = context.guildId

        context.sendMessage {
            embed {
                author(userToBeViewed.tag, null, userToBeViewed.effectiveAvatar.url)

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
                            ExperienceRoleRates.guildId eq guildId.toLong() and (ExperienceRoleRates.role inList it.roleIds.map { it.toLong() })
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

                color = LorittaColors.LorittaAqua.toKordColor()
            }

            actionRow {
                linkButton("${loritta.config.loritta.website.url}/extras/faq-loritta/experience") {
                    label = context.i18nContext.get(XpCommand.XP_VIEW_I18N_PREFIX.LearnAboutXpButton)
                    loriEmoji = Emotes.LoriReading
                }

                // TODO: "View profile button"
            }
        }
    }
}
