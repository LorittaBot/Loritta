package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection

fun FlowContent.guildDashLeftSidebarEntries(
    i18nContext: I18nContext,
    guild: Guild,
    selectedGuildSection: GuildDashboardSection
) {
    div(classes = "guild-icon-wrapper") {
        style = "text-align: center;"

        img(src = guild.iconUrl) {

        }
    }

    div(classes = "entry guild-name") {
        text(guild.name)
    }

    aDashboardSidebarEntryButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, i18nContext, "/", "Voltar ao Painel de Usuário", false)

    leftSidebarHr()

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/overview", i18nContext.get(DashboardI18nKeysData.Overview.Title), selectedGuildSection == GuildDashboardSection.OVERVIEW, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Comandos")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/commands", "Comandos da Loritta", selectedGuildSection == GuildDashboardSection.COMMANDS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/prefixed-commands", i18nContext.get(DashboardI18nKeysData.PrefixedCommands.Title), selectedGuildSection == GuildDashboardSection.PREFIXED_COMMANDS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/command-channels", "Canais de Comandos", selectedGuildSection == GuildDashboardSection.COMMAND_CHANNELS, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/custom-commands", i18nContext.get(DashboardI18nKeysData.CustomCommands.Title), selectedGuildSection == GuildDashboardSection.CUSTOM_COMMANDS, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Moderação")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/invite-blocker", i18nContext.get(DashboardI18nKeysData.InviteBlocker.Title), selectedGuildSection == GuildDashboardSection.INVITE_BLOCKER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/punishment-log", i18nContext.get(DashboardI18nKeysData.PunishmentLog.Title), selectedGuildSection == GuildDashboardSection.PUNISHMENT_LOG,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/event-log", i18nContext.get(DashboardI18nKeysData.EventLog.Title), selectedGuildSection == GuildDashboardSection.EVENT_LOG,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/welcomer", i18nContext.get(DashboardI18nKeysData.Welcomer.Title), selectedGuildSection == GuildDashboardSection.WELCOMER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/autorole", i18nContext.get(DashboardI18nKeysData.Autorole.Title), selectedGuildSection == GuildDashboardSection.AUTOROLE,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/permissions", i18nContext.get(DashboardI18nKeysData.Permissions.Title), selectedGuildSection == GuildDashboardSection.PERMISSIONS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/member-counter", i18nContext.get(DashboardI18nKeysData.MemberCounter.Title), selectedGuildSection == GuildDashboardSection.MEMBER_COUNTER,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/gamersafer-verify", i18nContext.get(DashboardI18nKeysData.GamerSafer.Title), selectedGuildSection == GuildDashboardSection.GAMERSAFER,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Alertas Sociais")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/youtube", i18nContext.get(DashboardI18nKeysData.Youtube.Title), selectedGuildSection == GuildDashboardSection.YOUTUBE, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/twitch", i18nContext.get(DashboardI18nKeysData.Twitch.Title), selectedGuildSection == GuildDashboardSection.TWITCH, false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/bluesky", i18nContext.get(DashboardI18nKeysData.Bluesky.Title), selectedGuildSection == GuildDashboardSection.BLUESKY, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Diversão")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/starboard", "Starboard", selectedGuildSection == GuildDashboardSection.STARBOARD,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/bom-dia-e-cia", i18nContext.get(DashboardI18nKeysData.BomDiaECia.Title), selectedGuildSection == GuildDashboardSection.BOM_DIA_E_CIA,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/quirky-mode", i18nContext.get(DashboardI18nKeysData.QuirkyMode.Title), selectedGuildSection == GuildDashboardSection.QUIRKY_MODE,  false)

    leftSidebarHr()

    div(classes = "category") {
        text("Loritta")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/reaction-events", "Evento de Reações da Loritta", selectedGuildSection == GuildDashboardSection.LORITTA_REACTION_EVENTS,  true)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/daily-shop-trinkets", i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.Title), selectedGuildSection == GuildDashboardSection.LORITTA_TRINKETS_SHOP, true)

    leftSidebarHr()

    div(classes = "category") {
        text("Premium")
    }

    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/premium-keys", i18nContext.get(DashboardI18nKeysData.PremiumKeys.Title), selectedGuildSection == GuildDashboardSection.PREMIUM_KEYS,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/badge", i18nContext.get(DashboardI18nKeysData.Badge.Title), selectedGuildSection == GuildDashboardSection.CUSTOM_BADGE,  false)
    aDashboardSidebarEntry(i18nContext, "/guilds/${guild.idLong}/daily-multiplier", i18nContext.get(DashboardI18nKeysData.DailyMultiplier.Title), selectedGuildSection == GuildDashboardSection.DAILY_MULTIPLIER,  false)
}