package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.switchtwitch.data.TwitchUser

fun FlowContent.trackedYouTubeChannelEditorWithProfile(
    i18nContext: I18nContext,
    guild: Guild,
    channel: YouTubeChannel,
    channelId: Long?,
    message: String?
) {
    fieldWrappers {
        fieldWrapper {
            simpleImageWithTextHeader(channel.name, channel.avatarUrl, true)
        }

        fieldWrappers {
            sectionConfig {
                trackedYouTubeChannelEditor(
                    i18nContext,
                    guild,
                    channelId,
                    message
                )
            }
        }
    }
}