package net.perfectdreams.loritta.morenitta.website.utils

import kotlinx.html.BUTTON
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.encodeURIComponent
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast

class SpicyMorenittaTriggers(
    var closeSpicyModal: Boolean = false,
    var spicyModal: EmbeddedSpicyModal? = null,
    var spicyToast: EmbeddedSpicyToast? = null,
    var playSoundEffect: String? = null,
    var additionalJson: JsonObjectBuilder.() -> (Unit) = {}
) {
    fun showSpicyToast(
        type: EmbeddedSpicyToast.Type,
        title: String,
        description: String,
    ) {
        this.spicyToast = EmbeddedSpicyModalUtils.createSpicyToast(type, title) { text(description) }
    }

    fun showSpicyToast(
        type: EmbeddedSpicyToast.Type,
        title: String,
        description: DIV.() -> (Unit) = {},
    ) {
        this.spicyToast = EmbeddedSpicyModalUtils.createSpicyToast(type, title, description)
    }

    fun showSpicyModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: TagConsumer<String>.() -> (Unit),
        buttons: List<BUTTON.() -> (Unit)>
    ) {
        this.spicyModal = EmbeddedSpicyModalUtils.createSpicyModal(
            title,
            canBeClosedByClickingOutsideTheWindow,
            body,
            buttons
        )
    }

    fun build(): JsonObject {
        return buildJsonObject {
            if (closeSpicyModal)
                put("closeSpicyModal", true)

            if (spicyModal != null)
                put("showSpicyModal", encodeURIComponent(Json.encodeToString(spicyModal)))

            if (spicyToast != null)
                put("showSpicyToast", encodeURIComponent(Json.encodeToString(spicyToast)))

            if (playSoundEffect != null)
                put("playSoundEffect", playSoundEffect)

            additionalJson.invoke(this)
        }
    }
}