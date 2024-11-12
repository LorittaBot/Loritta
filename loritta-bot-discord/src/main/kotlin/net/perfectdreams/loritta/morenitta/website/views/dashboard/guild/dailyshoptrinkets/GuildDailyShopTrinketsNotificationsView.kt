package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.dailyshoptrinkets

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.DailyShopTrinketsNotificationMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor.lorittaDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.toggleableSection
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildDailyShopTrinketsNotificationsConfig
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.time.Instant

class  GuildDailyShopTrinketsNotificationsView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    selectedType: String,
    private val config: GuildDailyShopTrinketsNotificationsConfig
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    selectedType
) {
    companion object {
        val defaultShopTrinketsTemplate = DashboardDiscordMessageEditor.createMessageTemplate(
            "Padrão",
            "# ${Emotes.ShoppingBags.asMention} Loja Diária da Loritta ({daily-shop.date-short})"
        )

        val defaultNewTrinketsTemplate = DashboardDiscordMessageEditor.createMessageTemplate(
            "Padrão",
            "# ${Emotes.ShoppingBags.asMention} Novas Bugigangas na Loja Diária da Loritta ({daily-shop.date-short})"
        )

        private val notifyShopTrinketsTemplates = listOf(
            defaultShopTrinketsTemplate
        )

        private val notifyNewTrinketsTemplates = listOf(
            defaultNewTrinketsTemplate
        )
    }

    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = WebsiteUtils.convertJDAGuildToSerializable(guild)
        val serializableSelfLorittaUser = WebsiteUtils.convertJDAUserToSerializable(guild.selfMember.user)

        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    img(src = "https://stuff.loritta.website/loritta-daily-shop-nicholas.png", classes = "hero-image") {}

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.Title))
                        }

                        for (line in i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.Description)) {
                            p {
                                text(line)
                            }
                        }
                    }
                }

                hr {}

                div {
                    id = "module-config-wrapper"
                    form {
                        id = "module-config"
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"

                        div(classes = "toggleable-sections") {
                            toggleableSection(
                                "notifyShopTrinkets",
                                i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.ShopRefresh.NotifyWhenRefresh),
                                i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.ShopRefresh.NotifyWhenRefreshDescription),
                                config.notifyShopTrinkets
                            ) {
                                div(classes = "field-wrappers") {
                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.ShopRefresh.ChannelWhereTheMessagesWillBeSent))
                                        }

                                        discordChannelSelectMenu(
                                            lorittaWebsite,
                                            i18nContext,
                                            "shopTrinketsChannelId",
                                            guild.channels.filterIsInstance<GuildMessageChannel>(),
                                            config.shopTrinketsChannelId,
                                            null
                                        )
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.ShopRefresh.MessageWhenTheShopUpdates))
                                        }

                                        lorittaDiscordMessageEditor(
                                            i18nContext,
                                            "shopTrinketsMessage",
                                            notifyShopTrinketsTemplates,
                                            PlaceholderSectionType.DAILY_SHOP_TRINKETS_NOTIFICATION_MESSAGE,
                                            DailyShopTrinketsNotificationMessagePlaceholders.placeholders.flatMap {
                                                when (it) {
                                                    DailyShopTrinketsNotificationMessagePlaceholders.DailyShopDateShortPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        TimeFormat.DATE_SHORT.format(Instant.now())
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.name
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.memberCount.toString()
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.getIconUrl(512, ImageFormat.PNG) ?: ""
                                                    ) // TODO: Fix this!
                                                }
                                            },
                                            serializableGuild,
                                            serializableSelfLorittaUser,
                                            TestMessageTargetChannelQuery.QuerySelector("[name='shopTrinketsChannelId']"),
                                            config.shopTrinketsMessage ?: ""
                                        )
                                    }
                                }
                            }

                            toggleableSection(
                                "notifyNewTrinkets",
                                i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.NewTrinkets.NotifyWhenNew),
                                i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.NewTrinkets.NotifyWhenNewDescription),
                                config.notifyNewTrinkets
                            ) {
                                div(classes = "field-wrappers") {
                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.NewTrinkets.ChannelWhereTheMessagesWillBeSent))
                                        }

                                        discordChannelSelectMenu(
                                            lorittaWebsite,
                                            i18nContext,
                                            "newTrinketsChannelId",
                                            guild.channels.filterIsInstance<GuildMessageChannel>(),
                                            config.newTrinketsChannelId,
                                            null
                                        )
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShopTrinkets.NewTrinkets.MessageWhenNewTrinkets))
                                        }

                                        lorittaDiscordMessageEditor(
                                            i18nContext,
                                            "newTrinketsMessage",
                                            notifyNewTrinketsTemplates,
                                            PlaceholderSectionType.DAILY_SHOP_TRINKETS_NOTIFICATION_MESSAGE,
                                            DailyShopTrinketsNotificationMessagePlaceholders.placeholders.flatMap {
                                                when (it) {
                                                    DailyShopTrinketsNotificationMessagePlaceholders.DailyShopDateShortPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        TimeFormat.DATE_SHORT.format(Instant.now())
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.name
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.memberCount.toString()
                                                    )

                                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.getIconUrl(512, ImageFormat.PNG) ?: ""
                                                    ) // TODO: Fix this!
                                                }
                                            },
                                            serializableGuild,
                                            serializableSelfLorittaUser,
                                            TestMessageTargetChannelQuery.QuerySelector("[name='newTrinketsChannelId']"),
                                            config.newTrinketsMessage ?: ""
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                hr {}

                lorittaSaveBar(
                    i18nContext,
                    false,
                    {}
                ) {
                    attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/daily-shop-trinkets"
                }
            }
        }
    }
}