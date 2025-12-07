package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.ins
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.calculateGuildIconShortName

fun FlowContent.guildDashLeftSidebarEntries(
    i18nContext: I18nContext,
    guild: Guild,
    userPremiumPlans: UserPremiumPlans,
    selectedGuildSection: GuildDashboardSection
) {
    div(classes = "guild-icon-wrapper") {
        style = "text-align: center;"

        div(classes = "discord-server-icon") {
            if (guild.iconUrl != null) {
                img(src = guild.iconUrl) {}
            } else {
                classes += "use-discord-background"
                text(calculateGuildIconShortName(guild.name))
            }
        }
    }

    div(classes = "entry guild-name") {
        text(guild.name)
    }

    a(classes = "discord-button ${ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT.className} text-with-icon bounce-icon-to-the-left-on-hover", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/") {
        style = "width: 100%;"
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "#right-sidebar-wrapper"
        attributes["bliss-component"] = "close-left-sidebar-on-click"

        svgIcon(SVGIcons.CaretLeft)

        text("Voltar ao Painel de Usuário")
    }

    leftSidebarHr()

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/overview", i18nContext.get(DashboardI18nKeysData.Overview.Title), SVGIcons.SquaresFour, selectedGuildSection == GuildDashboardSection.OVERVIEW, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Comandos")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/commands", "Comandos da Loritta", SVGIcons.SlashCommand, selectedGuildSection == GuildDashboardSection.COMMANDS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/prefixed-commands", i18nContext.get(DashboardI18nKeysData.PrefixedCommands.Title), SVGIcons.PrefixCommand, selectedGuildSection == GuildDashboardSection.PREFIXED_COMMANDS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/command-channels", "Canais de Comandos", SVGIcons.TextChannel, selectedGuildSection == GuildDashboardSection.COMMAND_CHANNELS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/custom-commands", i18nContext.get(DashboardI18nKeysData.CustomCommands.Title), SVGIcons.Code, selectedGuildSection == GuildDashboardSection.CUSTOM_COMMANDS, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Comunidade")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/welcomer", i18nContext.get(DashboardI18nKeysData.Welcomer.Title), SVGIcons.HandWaving, selectedGuildSection == GuildDashboardSection.WELCOMER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/autorole", i18nContext.get(DashboardI18nKeysData.Autorole.Title), SVGIcons.RoleShield, selectedGuildSection == GuildDashboardSection.AUTOROLE,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/member-counter", i18nContext.get(DashboardI18nKeysData.MemberCounter.Title), SVGIcons.ArrowFatLinesUp, selectedGuildSection == GuildDashboardSection.MEMBER_COUNTER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/permissions", i18nContext.get(DashboardI18nKeysData.Permissions.Title), SVGIcons.IdentificationCard, selectedGuildSection == GuildDashboardSection.PERMISSIONS,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Moderação")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/invite-blocker", i18nContext.get(DashboardI18nKeysData.InviteBlocker.Title), SVGIcons.Ban, selectedGuildSection == GuildDashboardSection.INVITE_BLOCKER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/punishment-log", i18nContext.get(DashboardI18nKeysData.PunishmentLog.Title), SVGIcons.Scroll, selectedGuildSection == GuildDashboardSection.PUNISHMENT_LOG,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/warn-actions", i18nContext.get(DashboardI18nKeysData.WarnActions.Title), SVGIcons.Gavel, selectedGuildSection == GuildDashboardSection.WARN_ACTIONS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/event-log", i18nContext.get(DashboardI18nKeysData.EventLog.Title), SVGIcons.Eye, selectedGuildSection == GuildDashboardSection.EVENT_LOG,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/gamersafer-verify", i18nContext.get(DashboardI18nKeysData.GamerSafer.Title), SVGIcons.GamerSafer, selectedGuildSection == GuildDashboardSection.GAMERSAFER,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Experiência")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/xp-rewards", i18nContext.get(DashboardI18nKeysData.XpRewards.Title), SVGIcons.Ranking, selectedGuildSection == GuildDashboardSection.XP_REWARDS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/xp-rates", i18nContext.get(DashboardI18nKeysData.XpRates.Title), SVGIcons.ChartDonut, selectedGuildSection == GuildDashboardSection.XP_RATES,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/xp-blockers", i18nContext.get(DashboardI18nKeysData.XpBlockers.Title), SVGIcons.LockSimple, selectedGuildSection == GuildDashboardSection.XP_BLOCKERS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/xp-notifications", i18nContext.get(DashboardI18nKeysData.XpNotifications.Title), SVGIcons.SortDescending, selectedGuildSection == GuildDashboardSection.XP_NOTIFICATIONS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/reset-xp", i18nContext.get(DashboardI18nKeysData.ResetXp.Title), SVGIcons.Knife, selectedGuildSection == GuildDashboardSection.RESET_XP,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Alertas Sociais")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/youtube", i18nContext.get(DashboardI18nKeysData.Youtube.Title), SVGIcons.YouTube, selectedGuildSection == GuildDashboardSection.YOUTUBE, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/twitch", i18nContext.get(DashboardI18nKeysData.Twitch.Title), SVGIcons.Twitch, selectedGuildSection == GuildDashboardSection.TWITCH, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/bluesky", i18nContext.get(DashboardI18nKeysData.Bluesky.Title), SVGIcons.Bluesky, selectedGuildSection == GuildDashboardSection.BLUESKY, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Diversão")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/starboard", "Starboard", SVGIcons.Star, selectedGuildSection == GuildDashboardSection.STARBOARD,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/bom-dia-e-cia", i18nContext.get(DashboardI18nKeysData.BomDiaECia.Title), SVGIcons.PlayStation, selectedGuildSection == GuildDashboardSection.BOM_DIA_E_CIA,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/quirky-mode", i18nContext.get(DashboardI18nKeysData.QuirkyMode.Title), SVGIcons.Coy, selectedGuildSection == GuildDashboardSection.QUIRKY_MODE,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Loritta")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/reaction-events", "Evento de Reações da Loritta", SVGIcons.Sparkles, selectedGuildSection == GuildDashboardSection.LORITTA_REACTION_EVENTS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/daily-shop-trinkets", i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.Title), SVGIcons.ShoppingBag, selectedGuildSection == GuildDashboardSection.LORITTA_TRINKETS_SHOP, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Premium")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/premium-keys", i18nContext.get(DashboardI18nKeysData.PremiumKeys.Title), SVGIcons.Key, selectedGuildSection == GuildDashboardSection.PREMIUM_KEYS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/badge", i18nContext.get(DashboardI18nKeysData.Badge.Title), SVGIcons.Seal, selectedGuildSection == GuildDashboardSection.CUSTOM_BADGE,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/daily-multiplier", i18nContext.get(DashboardI18nKeysData.DailyMultiplier.Title), SVGIcons.ShootingStar, selectedGuildSection == GuildDashboardSection.DAILY_MULTIPLIER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/drops", i18nContext.get(DashboardI18nKeysData.Drops.Title), SVGIcons.Fire, selectedGuildSection == GuildDashboardSection.LORITTA_DROPS,  true)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/tax-free-days", i18nContext.get(DashboardI18nKeysData.TaxFreeDays.Title), SVGIcons.CalendarStar, selectedGuildSection == GuildDashboardSection.LORITTA_TAX_FREE_DAYS,  true)
}