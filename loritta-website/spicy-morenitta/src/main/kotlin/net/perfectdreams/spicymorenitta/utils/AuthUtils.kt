package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window

object AuthUtils {
    fun handlePopup() {
        if (window.opener != undefined && window.opener !== window) {
            document.onDOMReady {
                // Popup, close popup and change navbar
                window.opener.asDynamic().authenticate(document.select<HTMLDivElement>("#hidden-auth-payload").innerHTML)
                window.close() // Close
            }
        } else {
            // Not popup, redirect!
            window.location.href = window.location.origin
        }
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @JsName("handlePostAuth")
    fun handlePostAuth(payload: String) {
        val userIdentification = JSON.nonstrict.parse<UserIdentification>(payload)
        SpicyMorenitta.INSTANCE.updateLoggedInUser(userIdentification)
    }
}