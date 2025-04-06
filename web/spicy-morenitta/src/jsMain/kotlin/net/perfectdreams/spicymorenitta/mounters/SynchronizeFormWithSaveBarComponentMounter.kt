package net.perfectdreams.spicymorenitta.mounters

import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.htmx
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import web.html.HTMLElement

class SynchronizeFormWithSaveBarComponentMounter : ComponentMounter(), Logging {
    override fun mount(element: HTMLElement) {
        if (element is HTMLFormElement && element.getAttribute("loritta-synchronize-with-save-bar") != null) {
            if (element.getAttribute("loritta-powered-up") != null)
                return

            element.setAttribute("loritta-powered-up", "")
            val lorittaSaveBarAttribute = element.getAttribute("loritta-synchronize-with-save-bar")!!

            val saveBarElement = htmx.find(lorittaSaveBarAttribute) as HTMLDivElement
            val initialState = JSON.stringify(htmx.values(element))

            println("Form save bar setup, initial state is: $initialState")

            element.addEventListener(
                "input",
                { event ->
                    // If the input does not have a name attribute, let's ignore the input...
                    if ((event.target as Element?)?.getAttribute("name") == null)
                        return@addEventListener

                    // Save the current state
                    val newState = JSON.stringify(htmx.values(element))

                    println("INITIAL STATE: $initialState")
                    println("NEW STATE: $newState")

                    if (newState != initialState) {
                        // This is a HACK to avoid the save bar showing up when it is inserted into the DOM
                        // We can't keep this in a hyperscript when the animation ends because that breaks if we are deferring the script
                        saveBarElement.removeClass("initial-state")

                        saveBarElement.addClass("has-changes")
                        saveBarElement.removeClass("no-changes")
                    } else {
                        // This is a HACK to avoid the save bar showing up when it is inserted into the DOM
                        // We can't keep this in a hyperscript when the animation ends because that breaks if we are deferring the script
                        saveBarElement.removeClass("initial-state")

                        saveBarElement.addClass("no-changes")
                        saveBarElement.removeClass("has-changes")
                    }
                }
            )
        }
    }
}