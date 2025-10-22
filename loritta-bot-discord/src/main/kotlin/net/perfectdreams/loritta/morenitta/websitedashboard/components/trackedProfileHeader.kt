package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img

fun FlowContent.trackedProfileHeader(
    username: String,
    avatarUrl: String
) {
    div(classes = "tracked-profile-header") {
        img(src = avatarUrl)

        h1 {
            text(username)
        }
    }
}