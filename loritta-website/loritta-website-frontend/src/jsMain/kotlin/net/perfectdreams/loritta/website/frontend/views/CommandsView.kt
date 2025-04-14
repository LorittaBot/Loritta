package net.perfectdreams.loritta.website.frontend.views

import net.perfectdreams.harmony.web.addClass
import net.perfectdreams.harmony.web.removeClass
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.utils.extensions.onClick
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import web.dom.Element
import web.dom.document
import web.events.addEventListener
import web.html.*
import web.uievents.InputEvent
import web.window.window
import kotlin.js.Date

class CommandsView(val showtime: LorittaWebsiteFrontend) : DokyoView() {
    override suspend fun onLoad() {
        val commandCategories = document.selectAll<HTMLDivElement>("lori-command-category")
        val commandEntries = document.selectAll<HTMLDivElement>("lori-command-entry")
        val commandCategoryInfo = document.selectAll<HTMLDivElement>("[data-category-info]")
        val searchBar = document.select<HTMLInputElement>(".search-bar")

        // Auto close all command entries when you click on a new command entry
        commandEntries.forEach { entry ->
            entry.onClick {
                commandEntries.filterNot { it == entry }.forEach {
                    it.children[0]?.removeAttribute("open")
                }
            }
        }

        commandCategories.forEach { categoryTag ->
            val categoryEnumName = categoryTag.getAttribute("data-command-category")

            val parentAElement = categoryTag.parentElement as HTMLAnchorElement

            // We are going to handle the events on the anchor element
            parentAElement.onClick {
                // Accessibility stuff
                if (it.asDynamic().ctrlKey as Boolean || it.asDynamic().metaKey as Boolean || it.asDynamic().shiftKey as Boolean)
                    return@onClick

                it.preventDefault()

                if (categoryEnumName == "ALL") {
                    // If we trying to see all commands, just reset the display of everything
                    commandEntries.forEach { it.style.display = "" }
                } else {
                    // Hide & Unhide command entries
                    commandEntries.filter { it.getAttribute("data-command-category") == categoryEnumName }
                        .forEach { it.style.display = "" }
                    commandEntries.filterNot { it.getAttribute("data-command-category") == categoryEnumName }
                        .forEach { it.style.display = "none" }
                }

                // Hide & Unhide category info
                commandCategoryInfo.filter { it.getAttribute("data-category-info") == categoryEnumName }
                    .forEach { it.style.display = "" }
                commandCategoryInfo.filterNot { it.getAttribute("data-category-info") == categoryEnumName }
                    .forEach { it.style.display = "none" }

                // Reset the search bar
                searchBar.value = ""

                // Toggle selection
                commandCategories.filter { it.getAttribute("data-command-category") == categoryEnumName }
                    .forEach { it.parentElement?.addClass("selected") }
                commandCategories.filterNot { it.getAttribute("data-command-category") == categoryEnumName }
                    .forEach { it.parentElement?.removeClass("selected") }

                // Reset scroll to the top of the page
                window.scrollTo(0.0, 0.0)

                // And push the URL to the history!
                // This is actually not needed, but useful if someone wants to share the URL
                showtime.pushState(parentAElement.href)
            }
        }

        // Search is a pain to handle, but not impossible!
        // We need to do filtering based on the currently visible elements AND the current active category!
        searchBar.addEventListener(InputEvent.INPUT, {
            val start = Date.now()
            // Cache the RegEx to avoid object creation!
            // Also, we escpae the search bar value to avoid users writing RegEx...
            // (not that is a huuuuge issue because, well, they will only wreck havoc in their own PC, but still...)
            val regex = Regex(Regex.escape(searchBar.value), RegexOption.IGNORE_CASE)

            // Remove all yellow backgrounds
            // We can't use .remove() because that would remove the entire tag, but we want to keep the inner text!
            // Behold, a workaround! https://stackoverflow.com/a/60332957/7271796
            document.selectAll<HTMLSpanElement>(".yellow").forEach {
                it.outerHTML = it.innerHTML
            }

            // We need to get the currently active category to not unhide stuff that shouldn't be unhidden
            // Also, it should be impossible that there isn't any children (unless if the HTML is malformed)
            val activeCategory = document.select<HTMLAnchorElement>(".entry.selected")
                .children[0]
                ?.getAttribute("data-command-category") ?: return@addEventListener

            commandEntries.forEach {
                if (searchBar.value.isBlank()) {
                    // Search Bar is empty, just unhide everything
                    if (activeCategory == "ALL") {
                        it.style.display = ""
                    } else {
                        val shouldBeVisible = it.getAttribute("data-command-category") == activeCategory
                        if (shouldBeVisible)
                            it.style.display = ""
                        else
                            it.style.display = "none"
                    }
                } else {
                    val shouldBeVisible = activeCategory == "ALL" || it.getAttribute("data-command-category") == activeCategory
                    // If it shouldn't be visible anyways, do not even *try* to check stuff, just hide them and carry on
                    if (!shouldBeVisible) {
                        it.style.display = "none"
                        return@forEach
                    }

                    // Because we want to add a few nice details to the search results (a yellow background on results)
                    // we need to get all the indexes in the command label and description
                    val commandLabelElement = it.select<HTMLElement>("lori-command-label")
                    val commandDescriptionElement = it.select<HTMLElement>("lori-command-description")

                    fun findMatchesWithinAndHighlight(element: Element): Boolean {
                        // We need to use innerHTML because we also care about the positions of HTML tags within the text
                        // If we used something like innerText or textContent, any HTML tag would break the search
                        val innerHTML = element.innerHTML

                        val results = regex.findAll(innerHTML)
                            .toList()

                        if (results.isNotEmpty()) {
                            val changedInnerHTMLBuilder = StringBuilder(innerHTML)

                            // Inserting stuff somewhere would've been too easy... But then you remember that, if you insert
                            // something, this will cause EVERYTHING to shift, so you also need to add a offset based on the
                            // size of the tag that you've inserted...
                            var offset = 0

                            for (result in results) {
                                // ew, raw HTML!
                                // But because we aren't injecting any user input... it is fine ;3
                                val startingTag = "<span class='yellow' style='background-color: #f7ff00b5;'>"
                                val endTag = "</span>"

                                changedInnerHTMLBuilder.insert(
                                    result.range.first + offset,
                                    startingTag
                                )

                                offset += startingTag.length

                                // Even tho "last" is inclusive... it is not working as expected, so we need to +1
                                changedInnerHTMLBuilder.insert(result.range.last + offset + 1, endTag)

                                offset += endTag.length
                            }

                            element.innerHTML = changedInnerHTMLBuilder.toString()
                            return true
                        }
                        return false
                    }

                    // Yes, we need to do both in separate lines, we can't shove them in the same line because, if there is the word in the label, the description
                    // won't be highlighted! And we want both :3
                    val labelResult = findMatchesWithinAndHighlight(commandLabelElement)
                    val descriptionResult = findMatchesWithinAndHighlight(commandDescriptionElement)
                    val hasAnyMatch = labelResult || descriptionResult

                    if (hasAnyMatch) {
                        it.style.display = ""
                    } else {
                        it.style.display = "none"
                    }
                }
            }

            println("End: ${Date.now() - start}")
        })
    }
}