package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.switchtwitch.data.TwitchUser

fun FlowContent.trackedTwitchChannelEditorWithProfile(
    i18nContext: I18nContext,
    guild: Guild,
    twitchUser: TwitchUser,
    state: TwitchAccountTrackState,
    channelId: Long?,
    message: String?
) {
    fieldWrappers {
        fieldWrapper {
            trackedProfileHeader(twitchUser.displayName, twitchUser.profileImageUrl)

            when (state) {
                TwitchAccountTrackState.AUTHORIZED -> {
                    div(classes = "alert alert-success") {
                        text("O canal foi autorizado pelo dono, então você receberá notificações quando o canal entrar ao vivo!")
                    }
                }
                TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                    div(classes = "alert alert-success") {
                        text("O canal não está autorizado, mas ela está na minha lista especial de \"pessoas tão incríveis que não preciso pedir autorização\". Você receberá notificações quando o canal entrar ao vivo.")
                    }
                }
                TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                    div(classes = "alert alert-success") {
                        text("O canal não está autorizado, mas você colocou ele na lista de acompanhamentos premium! Você receberá notificações quando o canal entrar ao vivo.")
                    }
                }
                TwitchAccountTrackState.UNAUTHORIZED -> {
                    div(classes = "alert alert-danger") {
                        text("O canal não está autorizado! Você só receberá notificações quando o canal for autorizado na Loritta.")
                    }
                }
            }
        }

        fieldWrappers {
            sectionConfig {
                trackedTwitchChannelEditor(
                    i18nContext,
                    guild,
                    twitchUser,
                    channelId,
                    message
                )
            }
        }
    }
}