package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.bluesky

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.BlueskyPostMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor.lorittaDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildConfigureBlueskyProfileView(
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
    private val trackId: Long?,
    private val blueskyProfile: BlueskyProfile,
    private val trackSettings: BlueskyTrackSettings,
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
    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = WebsiteUtils.convertJDAGuildToSerializable(guild)
        val serializableSelfLorittaUser = WebsiteUtils.convertJDAUserToSerializable(guild.selfMember.user)

        div {
            a(classes = "discord-button no-background-theme-dependent-dark-text", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky") {
                htmxGetAsHref()
                attributes["hx-push-url"] = "true"
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"

                htmxDiscordLikeLoadingButtonSetup(
                    i18nContext
                ) {
                    this.text("Voltar para a lista de contas do Bluesky")
                }
            }

            hr {}

            div(classes = "hero-wrapper") {
                div(classes = "hero-image") {
                    img(src = blueskyProfile.avatar) {
                        style = "border-radius: 100%; width: 300px; height: 300px;"
                    }
                }

                div(classes = "hero-text") {
                    h1 {
                        text(blueskyProfile.effectiveName)
                    }
                }
            }

            hr {}

            div {
                id = "module-config-wrapper"
                form {
                    id = "module-config"
                    // If the trackId is null, then the save bar should ALWAYS be dirty
                    if (trackId != null) {
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"
                    }

                    // TODO: We technically don't need this here if the trackId is != null
                    hiddenInput {
                        name = "did"
                        value = blueskyProfile.did
                    }

                    div(classes = "field-wrappers") {
                        div(classes = "field-wrapper") {
                            div(classes = "field-title") {
                                label {
                                    text("Canal onde será enviado as mensagens")
                                }
                            }

                            div {
                                style = "width: 100%;"

                                discordChannelSelectMenu(
                                    lorittaWebsite,
                                    i18nContext,
                                    "channelId",
                                    guild.channels.filterIsInstance<GuildMessageChannel>(),
                                    trackSettings.channelId,
                                    null
                                )
                            }
                        }

                        lorittaDiscordMessageEditor(
                            i18nContext,
                            "message",
                            listOf(),
                            PlaceholderSectionType.BLUESKY_POST_MESSAGE,
                            BlueskyPostMessagePlaceholders.placeholders.flatMap { placeholder ->
                                when (placeholder) {
                                    BlueskyPostMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.iconUrl ?: "???"
                                    ) // TODO: Provide a proper fallback
                                    BlueskyPostMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.name
                                    )

                                    BlueskyPostMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.memberCount.toString()
                                    )

                                    BlueskyPostMessagePlaceholders.PostUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "https://bsky.app/profile/loritta.website/post/3l34ux7btja24"
                                    )
                                }
                            },
                            serializableGuild,
                            serializableSelfLorittaUser,
                            TestMessageTargetChannelQuery.QuerySelector("[name='channelId']"),
                            trackSettings.message
                        )
                    }
                }
            }

            hr {}

            lorittaSaveBar(
                i18nContext,
                trackId == null,
                {}
            ) {
                if (trackId != null) {
                    attributes["hx-patch"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks/$trackId"
                } else {
                    attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks"
                }
            }
        }
    }

    data class BlueskyTrackSettings(
        val channelId: Long?,
        val message: String
    )
}