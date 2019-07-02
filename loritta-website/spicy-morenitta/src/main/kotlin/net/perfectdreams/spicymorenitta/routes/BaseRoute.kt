package net.perfectdreams.spicymorenitta.routes

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.i
import kotlinx.html.js.onClickFunction
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.hasClass
import kotlin.dom.removeClass
import kotlin.math.max

abstract class BaseRoute(val path: String) : Logging {
    open val keepLoadingScreen = false

    // Implementação básica do sistema de paths do ktor
    fun matches(input: String): Boolean {
        val sourceSplit = path.removeSuffix("/").split("/")
        val inputSplit = input.removeSuffix("/").split("/")

        var inputSplitLength = 0

        for (index in 0 until max(sourceSplit.size, inputSplit.size)) {
            val sInput = sourceSplit.getOrNull(index)
            val iInput = inputSplit.getOrNull(index)

            // Check if it is a group match
            if (sInput != null && sInput.startsWith("{") && sInput.endsWith("}")) {
                if (iInput == null && sInput.endsWith("?}")) {
                    inputSplitLength++
                    continue
                }

                inputSplitLength++
                continue
            }

            if (iInput == null)
                return false

            if (iInput != sInput) // Input does not match
                return false

            inputSplitLength++
        }

        return true
    }

    fun getPathParameters(input: String): Map<String, String> {
        val parameters = mutableMapOf<String, String>()

        val sourceSplit = path.removeSuffix("/").split("/")
        val inputSplit = input.removeSuffix("/").split("/")

        var inputSplitLength = 0

        for (index in 0 until max(sourceSplit.size, inputSplit.size)) {
            val sInput = sourceSplit.getOrNull(index)
            val iInput = inputSplit.getOrNull(index)

            // Check if it is a group match
            if (sInput != null && sInput.startsWith("{") && sInput.endsWith("}")) {
                if (iInput == null && sInput.endsWith("?}")) {
                    inputSplitLength++
                    continue
                }

                parameters[sInput.removePrefix("{").removeSuffix("?}").removeSuffix("}")] = iInput ?: "?"

                inputSplitLength++
                continue
            }

            if (iInput == null)
                return parameters

            if (iInput != sInput) // Input does not match
                return parameters

            inputSplitLength++
        }

        return parameters
    }

    open fun onRender(call: ApplicationCall) {
        hideDummyNavbarHeight(call)
        switchContent(call)
    }

    open fun hideDummyNavbarHeight(call: ApplicationCall) {
        val dummyNavigationBar = document.select<HTMLDivElement?>("#dummy-navbar")
        dummyNavigationBar?.style?.height = "0px"
    }

    open fun switchContent(call: ApplicationCall) {
        if (call.content != null) {
            document.select<HTMLDivElement>("#content").remove()
            val scriptList = call.content.querySelectorAll("script")
            val toBeReinserted = mutableListOf<Node>()

            repeat(scriptList.length) {
                val scriptTag = scriptList[it]!! as HTMLScriptElement
                if (scriptTag.getAttribute("src") == null) {
                    toBeReinserted.add(scriptTag.cloneNode(true))
                    scriptTag.clear()
                }
            }

            // Cancelar todas as tasks
            SpicyMorenitta.INSTANCE.pageSpecificTasks.onEach { it.cancel() }

            document.body?.appendChild(call.content)
            val childNode = document.body?.childNodes?.get(0)

            if (childNode != null) {
                // Necessário para executar scripts inline
                toBeReinserted.forEach {
                    val inline = (it as HTMLScriptElement).innerHTML
                    debug("(Re-)Inlining script $inline")
                    val newScript = document.createElement("script")
                    val inlineScript = document.createTextNode(inline)
                    newScript.appendChild(inlineScript)
                    document.body?.appendChild(newScript)
                }
            }

            SpicyMorenitta.INSTANCE.setUpLinkPreloader()
            SpicyMorenitta.INSTANCE.setUpLazyLoad()
        }
    }

    fun twoColumnLayout(
            leftSidebar: DIV.() -> (Unit),
            rightSidebar: DIV.() -> (Unit)
    ) {
        val content = document.select<HTMLDivElement>("#content")

        content.append {
            div {
                id = "sidebar-wrapper"

                div {
                    id = "left-sidebar"

                    div(classes = "subnavbar-hamburger") {
                        i(classes = "subnavbar-hamburger-button fas fa-bars") {
                            onClickFunction = {
                                val leftSidebar = document.select<Element>("#left-sidebar")

                                if (leftSidebar.hasClass("expanded")) {
                                    leftSidebar.removeClass("expanded")
                                } else {
                                    leftSidebar.addClass("expanded")
                                }
                            }
                        }
                    }

                    div(classes = "contents") {
                        leftSidebar.invoke(this)
                    }
                }

                div {
                    id = "right-sidebar"
                    attributes["create-scroll-lazy-load-here"] = "true"

                    div(classes = "contents") {
                        rightSidebar.invoke(this)
                    }
                }
            }
        }
    }
}