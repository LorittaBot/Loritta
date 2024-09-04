package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.YouTubePostMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor.lorittaDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildConfigureYouTubeChannelView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    private val trackId: Long?,
    private val youtubeChannel: YouTubeChannel,
    private val trackSettings: YouTubeTrackSettings
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
    "youtube"
) {
    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = WebsiteUtils.convertJDAGuildToSerializable(guild)
        val serializableSelfLorittaUser = WebsiteUtils.convertJDAUserToSerializable(guild.selfMember.user)

        div {
            a(classes = "discord-button no-background-theme-dependent-dark-text", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube") {
                htmxGetAsHref()
                attributes["hx-push-url"] = "true"
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"

                htmxDiscordLikeLoadingButtonSetup(
                    i18nContext
                ) {
                    this.text("Voltar para a lista de canais do YouTube")
                }
            }

            hr {}

            div(classes = "hero-wrapper") {
                div(classes = "hero-image") {
                    img(src = youtubeChannel.avatarUrl) {
                        style = "border-radius: 100%; width: 300px; height: 300px;"
                    }
                }

                div(classes = "hero-text") {
                    h1 {
                        text(youtubeChannel.name)
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
                        name = "youtubeChannelId"
                        value = youtubeChannel.channelId
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
                            PlaceholderSectionType.YOUTUBE_POST_MESSAGE,
                            YouTubePostMessagePlaceholders.placeholders.flatMap { placeholder ->
                                when (placeholder) {
                                    YouTubePostMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.iconUrl ?: "???"
                                    ) // TODO: Provide a proper fallback

                                    YouTubePostMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.name
                                    )

                                    YouTubePostMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.memberCount.toString()
                                    )

                                    YouTubePostMessagePlaceholders.VideoTitlePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "Loritta The Dog"
                                    )

                                    YouTubePostMessagePlaceholders.VideoIdPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "fVjCBxPqWlU"
                                    )

                                    YouTubePostMessagePlaceholders.VideoUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "https://youtu.be/fVjCBxPqWlU"
                                    )

                                    YouTubePostMessagePlaceholders.VideoThumbnailPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "https://img.youtube.com/vi/fVjCBxPqWlU/maxresdefault.jpg"
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
                    attributes["hx-patch"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube/tracks/$trackId"
                } else {
                    attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube/tracks"
                }
            }
        }
    }

    data class YouTubeTrackSettings(
        val channelId: Long?,
        val message: String
    )
}