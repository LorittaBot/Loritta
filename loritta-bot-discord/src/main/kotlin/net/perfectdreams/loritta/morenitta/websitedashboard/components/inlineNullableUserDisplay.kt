package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.DiscordCDNUtils

fun FlowContent.inlineNullableUserDisplay(userId: Long, cachedUserInfo: CachedUserInfo?) {
    div(classes = "inline-user-display") {
        if (cachedUserInfo != null) {
            val avatarUrl = DiscordCDNUtils.getEffectiveAvatarUrl(userId, cachedUserInfo.avatarId, null, 24)

            img(src = avatarUrl) {
                width = "24"
                height = "24"
            }

            div {
                text("${cachedUserInfo.globalName ?: cachedUserInfo.name} (@${cachedUserInfo.name} / ")
                code {
                    text(userId.toString())
                }
                text(")")
            }
        } else {
            // Because the hash is null, the avatar will be rendered as Discord's default avatar
            img(src = "https://cdn.discordapp.com/embed/avatars/0.png?size=256") {
                width = "24"
                height = "24"
            }

            div {
                text("Usuário desconhecido (")
                code {
                    text(userId.toString())
                }
                text(")")
            }
        }
    }
}