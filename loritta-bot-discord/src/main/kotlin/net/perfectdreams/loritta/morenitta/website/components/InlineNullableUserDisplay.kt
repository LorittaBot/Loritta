package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.loritta.serializable.CachedUserInfo

object InlineNullableUserDisplay {
    fun FlowContent.inlineNullableUserDisplay(userId: Long, cachedUserInfo: CachedUserInfo?) {
        div(classes = "inline-user-display") {
            if (cachedUserInfo != null) {
                // TODO - htmx-adventures: Move this somewhere else
                val userAvatarId = cachedUserInfo.avatarId
                val avatarUrl = if (userAvatarId != null) {
                    val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                        "gif"
                    } else { "png" }

                    "https://cdn.discordapp.com/avatars/$userId/${userAvatarId}.${extension}?size=256"
                } else {
                    val avatarId = (userId shr 22) % 6

                    "https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
                }

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
}