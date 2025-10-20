package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.textInput
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

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
                                attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), #left-sidebar (innerHTML) -> #left-sidebar (innerHTML)"
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