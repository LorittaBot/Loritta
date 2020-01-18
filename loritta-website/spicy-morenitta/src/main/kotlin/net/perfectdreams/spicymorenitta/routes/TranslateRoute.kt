package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import utils.ShowdownConverter
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

class TranslateRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/translate") {
    override val keepLoadingScreen: Boolean
        get() = true

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        val navbarHeight = document.select<HTMLDivElement>("#navigation-bar").clientHeight.toString()

        val editLocaleId = window.prompt("Locale ID for output (default, en-us, etc)")

        m.launch {
            val localeToBeTranslatedPayload = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/locale/$editLocaleId")
            }

            val localeToBeTranslated = kotlinx.serialization.json.JSON.nonstrict.parse<BaseLocale>(localeToBeTranslatedPayload)

            val originalLocalePayload = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/locale/${localeToBeTranslated["loritta.inheritsFromLanguageId"]}")
            }

            val originalLocale = kotlinx.serialization.json.JSON.nonstrict.parse<BaseLocale>(originalLocalePayload)

            val content = document.select<HTMLDivElement>("#content")

            val downloadJson = document.select<HTMLDivElement>("#download-json")

            downloadJson.onClick {
                // Workaround, já que não é possível salvar listas pelo Kotlin Serialization
                // Iremos colocar as listas "after the fact", verificando se começa por "list::" e aplicando as mudanças
                val json = object{}.asDynamic()

                localeToBeTranslated.localeEntries.forEach { (key, value) ->
                    if (value != originalLocale[key]) { // Não é necessário salvar keys que não foram traduzidas
                        if (value.startsWith("list::")) {
                            json[key] = localeToBeTranslated.getList(key)
                        } else {
                            json[key] = value
                        }
                    }
                }

                console.log(json)

                val asJson = JSON.stringify(json, space = 4)

                println(asJson)
                val a = window.document.createElement("a") as HTMLAnchorElement

                a.href = URL.createObjectURL(Blob(arrayOf(asJson), BlobPropertyBag("text/json")))
                a.download = localeToBeTranslated.id + ".json"

                // Append anchor to body.
                document.body!!.appendChild(a)
                a.click()

                // Remove anchor from body
                document.body!!.removeChild(a)
            }

            content.append {
                div {
                    style = "display: flex; flex-direction: row; height: calc(100vh - ${navbarHeight}px);"

                    div {
                        id = "entries-sidebar"
                    }
                    div {
                        id = "edit-sidebar"

                        div {
                            style = "padding: 7px;"

                            div {
                                id = "original-locale-key"
                            }
                            textArea {
                                id = "edited-locale"
                            }
                            hr {}
                            p {
                                b {
                                    + "Preview"
                                }
                            }
                            div {
                                id = "markdown-locale"
                            }
                            hr {}
                            p {
                                b {
                                    + "Original"
                                }
                            }
                            div {
                                id = "original-locale"
                            }
                        }
                    }
                }
            }

            redrawLocaleEntriesSidebar(originalLocale, localeToBeTranslated)

            m.hideLoadingScreen()
        }
    }

    fun redrawLocaleEntriesSidebar(originalLocale: BaseLocale, localeToBeTranslated: BaseLocale) {
        val entriesSidebar = document.select<HTMLDivElement>("#entries-sidebar")

        entriesSidebar.clear()
        entriesSidebar.append {
            for ((key, value) in originalLocale.localeEntries) {
                val translatedEntry = localeToBeTranslated[key]

                var classes = "locale-entry"
                if (translatedEntry == value) {
                    classes += " not-edited"
                }

                div(classes) {
                    +value

                    onClickFunction = {
                        val originalLocaleKeyDiv = document.select<HTMLDivElement>("#original-locale-key")
                        val editedLocaleTextArea = document.select<HTMLTextAreaElement>("#edited-locale")
                        val markdownLocaleDiv = document.select<HTMLDivElement>("#markdown-locale")
                        val originalLocaleDiv = document.select<HTMLDivElement>("#original-locale")

                        originalLocaleKeyDiv.innerText = key

                        var toBeTranslated = localeToBeTranslated.localeEntries[key] as String

                        // workaround, ew
                        val isList = toBeTranslated.startsWith("list::")
                        if (isList) {
                            toBeTranslated = toBeTranslated.removePrefix("list::").split("\n").joinToString("\n-----[list]-----\n")
                        }

                        editedLocaleTextArea.value = toBeTranslated
                        originalLocaleDiv.innerText = value
                        val converter = ShowdownConverter()
                        markdownLocaleDiv.innerHTML = converter.makeHtml(toBeTranslated.replace("-----[list]-----", "\n\n"))

                        editedLocaleTextArea.oninput = {
                            var localeValue = document.select<HTMLTextAreaElement>("#edited-locale").value
                            markdownLocaleDiv.innerHTML = converter.makeHtml(localeValue.replace("-----[list]-----", "\n\n"))

                            if (isList)
                                localeValue = "list::" + localeValue.replace("\n-----[list]-----\n", "\n")

                            localeToBeTranslated.localeEntries[key] = localeValue

                            redrawLocaleEntriesSidebar(originalLocale, localeToBeTranslated)
                            asDynamic()
                        }
                    }
                }
            }
        }
    }
}