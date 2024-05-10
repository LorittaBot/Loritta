// Yes, this is intentionally kept in the root package
// Access this via window['spicy-morenitta']
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.Element
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("closeModal")
fun closeModal() {
    SpicyMorenitta.INSTANCE.modalManager.closeModal()
}

@OptIn(ExperimentalJsExport::class, ExperimentalEncodingApi::class)
@JsExport
@JsName("openEmbeddedModal")
fun openEmbeddedModal(element: Element) {
    val embeddedModalData = element.getAttribute("loritta-embedded-spicy-modal") ?: error("There isn't any embedded modal on ${element}!")
    val embeddedSpicyModal = Json.decodeFromString<EmbeddedSpicyModal>(decodeURIComponent(embeddedModalData))
    SpicyMorenitta.INSTANCE.modalManager.openModal(embeddedSpicyModal)
}

external fun decodeURIComponent(encodedURI: String): String = definedExternally