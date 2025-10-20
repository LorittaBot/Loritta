package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard

import dev.minn.jda.ktx.generics.getChannel
import io.ktor.server.application.ApplicationCall
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.numberInput
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.starboardStorytime
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class StarboardGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/starboard") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val starboardConfig = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            serverConfig.starboardConfig
        }

        val starboardChannelId = starboardConfig?.starboardChannelId

        val starboardChannel = if (starboardChannelId != null) { guild.getChannel(starboardChannelId) } else null

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Starboard.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.STARBOARD)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    div(classes = "hero-wrapper") {
                                        etherealGambiImg(
                                            "https://stuff.loritta.website/loritta-star-hug-yafyr.png",
                                            classes = "hero-image",
                                            sizes = "(max-width: 900px) 100vw, 360px"
                                        ) {
                                            classes += "starboard-web-animation"
                                        }

                                        div(classes = "hero-text") {
                                            h1 {
                                                text(i18nContext.get(DashboardI18nKeysData.Starboard.Title))
                                            }

                                            for (line in i18nContext.get(DashboardI18nKeysData.Starboard.Description)) {
                                                p {
                                                    text(line)
                                                }
                                            }
                                        }
                                    }

                                    hr {}

                                    div {
                                        id = "section-config"

                                        toggleableSection(
                                            {
                                                text("Ativar Starboard")
                                            },
                                            description = null,
                                            starboardConfig?.enabled ?: false,
                                            "enabled",
                                            true,
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Starboard.StarboardChannel))
                                                    }

                                                    select {
                                                        attributes["bliss-component"] = "fancy-select-menu"
                                                        attributes["save-bar-track"] = "true"
                                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/starboard/storytime"
                                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #starboard-storytime (innerHTML)"
                                                        attributes["bliss-trigger"] = "input"
                                                        attributes["bliss-include-json"] = "[loritta-config]"
                                                        attributes["loritta-config"] = "starboardChannelId"
                                                        name = "starboardChannelId"

                                                        for (channel in guild.channels) {
                                                            if (channel is GuildMessageChannel) {
                                                                option {
                                                                    this.label = channel.name
                                                                    this.value = channel.id
                                                                    this.selected = starboardChannelId == channel.idLong
                                                                    this.disabled = false
                                                                    /* this.attributes["fancy-select-menu-open-modal-if-disabled"] = BlissHex.encodeToHexString(
                                                                        Json.encodeToString(
                                                                            createEmbeddedModal(
                                                                                "Sem Permissão!",
                                                                                true,
                                                                                {
                                                                                    text("Sem permissão :(")
                                                                                },
                                                                                listOf(
                                                                                    {
                                                                                        defaultModalCloseButton(i18nContext)
                                                                                    }
                                                                                )
                                                                            )
                                                                        )
                                                                    ) */
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                fieldWrapper {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Starboard.MinimumReactCount))
                                                    }

                                                    numberInput {
                                                        min = "1"
                                                        max = "1000"
                                                        step = "1"
                                                        value = (starboardConfig?.requiredStars ?: 1).toString()

                                                        attributes["save-bar-track"] = "true"
                                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/starboard/storytime"
                                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #starboard-storytime (innerHTML)"
                                                        attributes["bliss-trigger"] = "input"
                                                        attributes["bliss-include-json"] = "[loritta-config]"
                                                        attributes["loritta-config"] = "requiredStars"
                                                        name = "requiredStars"
                                                    }
                                                }
                                            }

                                            hr {}

                                            div {
                                                id = "starboard-storytime"

                                                starboardStorytime(i18nContext, starboardChannel, starboardConfig?.requiredStars ?: 1, website.loritta.lorittaShards.shardManager.shards.first().selfUser)
                                            }
                                        }
                                    }
                                },
                                {
                                    genericSaveBar(
                                        i18nContext,
                                        false,
                                        guild,
                                        "/starboard"
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}