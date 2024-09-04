package net.perfectdreams.loritta.morenitta.website.utils

import kotlinx.html.FlowOrMetaDataOrPhrasingContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.script
import kotlinx.html.unsafe

@HtmlTagMarker
fun FlowOrMetaDataOrPhrasingContent.tsukiScript(type : String? = null, src : String? = null, code: String) {
    script(
        type,
        src
    ) {
        unsafe {
            // language=JavaScript
            raw("""
                var self = me()
                {
                    $code
                }
            """.trimIndent())
        }
    }
}