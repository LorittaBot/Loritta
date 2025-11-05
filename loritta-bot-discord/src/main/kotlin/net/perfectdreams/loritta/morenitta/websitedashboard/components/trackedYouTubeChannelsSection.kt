package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.id
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick

fun FlowContent.trackedYouTubeChannelsSection(
    i18nContext: I18nContext,
    guild: Guild,
    trackedProfiles: List<TrackedProfile>
) {
    trackedProfilesSection(
        i18nContext,
        guild,
        "Canais que você está seguindo",
        i18nContext.get(DashboardI18nKeysData.Youtube.Channels(trackedProfiles.size)),
        {
            openModalOnClick(
                createEmbeddedModal(
                    "Adicionar Canal",
                    EmbeddedModal.Size.MEDIUM,
                    true,
                    {
                        textInput {
                            name = "channelLink"
                            placeholder = "https://www.youtube.com/@Loritta"
                        }
                    },
                    listOf(
                        {
                            defaultModalCloseButton(i18nContext)
                        },
                        {
                            discordButton(ButtonStyle.PRIMARY) {
                                id = "add-profile"
                                attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube/add"
                                attributes["bliss-include-query"] = ".modal [name='channelLink']"
                                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                                attributes["bliss-push-url:200"] = "true"

                                text("Continuar")
                            }
                        }
                    )
                )
            )

            text("Adicionar Canal")
        },
        "youtube",
        trackedProfiles
    )
}