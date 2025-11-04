package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.switchtwitch.data.TwitchUser

fun FlowContent.trackedBlueskyChannelEditorWithProfile(
    i18nContext: I18nContext,
    guild: Guild,
    profile: BlueskyProfile,
    channelId: Long?,
    message: String?
) {
    fieldWrappers {
        fieldWrapper {
            simpleImageWithTextHeader(profile.effectiveName, profile.avatar, true)
        }

        fieldWrappers {
            sectionConfig {
                trackedBlueskyProfileEditor(
                    i18nContext,
                    guild,
                    channelId,
                    message
                )
            }
        }
    }
}