// Yes, this is intentionally kept in the root package
// Access this via window['spicy-morenitta']
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.Element

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("closeModal")
fun closeModal() {
    println("Closing modal...")
    SpicyMorenitta.INSTANCE.modalManager.closeModal()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("openEmbeddedModal")
fun openEmbeddedModal(element: Element, attributeName: String = "loritta-embedded-spicy-modal") {
    val embeddedModalData = element.getAttribute(attributeName) ?: error("There isn't any embedded modal on ${element}!")
    val embeddedSpicyModal = Json.decodeFromString<EmbeddedSpicyModal>(decodeURIComponent(embeddedModalData))
    SpicyMorenitta.INSTANCE.modalManager.openModal(embeddedSpicyModal)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("playSoundEffect")
fun playSoundEffect(soundEffect: String, onEnd: () -> (Unit) = {}) {
    SpicyMorenitta.INSTANCE.playSoundEffect(soundEffect, onEnd = onEnd)
}

external fun decodeURIComponent(encodedURI: String): String = definedExternally