@file:JsExport
package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.HTMLDivElement
import kotlinx.browser.document
import kotlinx.browser.window

object AuthUtils {
    fun handlePopup() {
        if (window.opener != undefined && window.opener !== window) {
            document.onDOMContentLoaded {
                // Popup, close popup and change navbar
                window.opener.asDynamic().authenticate(document.select<HTMLDivElement>("#hidden-auth-payload").innerHTML)
                window.close() // Close
            }
        } else {
            // Not popup, redirect!
            window.location.href = window.location.origin
        }
    }

    @JsName("handlePostAuth")
    fun handlePostAuth(payload: String) {
        val userIdentification = JSON.nonstrict.decodeFromString(UserIdentification.serializer(), payload)
        SpicyMorenitta.INSTANCE.updateLoggedInUser(userIdentification)
    }
}