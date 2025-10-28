package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.script
import kotlinx.html.unsafe

fun FlowContent.pushAdSenseAdScript() {
    script {
        unsafe {
            raw("""(adsbygoogle = window.adsbygoogle || []).push({})""")
        }
    }
}