package net.perfectdreams.spicymorenitta.mounters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.messageeditor.LorittaDiscordMessageEditorSetupConfig
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.messages.DiscordMessageEditor
import net.perfectdreams.spicymorenitta.components.messages.TargetChannelResult
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.querySelector
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.events.EventInit
import web.events.addEventListener
import web.html.HTMLElement
import web.html.HTMLTextAreaElement
import web.uievents.InputEvent

class LorittaDiscordMessageEditorComponentMounter(val m: SpicyMorenitta) : SimpleComponentMounter("loritta-discord-message-editor"), Logging {
    override fun simpleMount(element: HTMLElement) {
        val originalSelectMenuElement = element
        if (originalSelectMenuElement.getAttribute("loritta-powered-up") != null)
            return

        if (originalSelectMenuElement !is HTMLTextAreaElement)
            throw RuntimeException("Discord Message Editor Component is not a HTMLTextAreaElement!")

        originalSelectMenuElement.setAttribute("loritta-powered-up", "")
        val setupJson = Json.decodeFromString<LorittaDiscordMessageEditorSetupConfig>(originalSelectMenuElement.getAttribute("loritta-discord-message-editor-config")!!)

        // Hide the original text area
        originalSelectMenuElement.style.display = "none"

        val selectMenuWrapperElement = document.createElement("div")

        originalSelectMenuElement.parentElement!!.insertBefore(selectMenuWrapperElement, originalSelectMenuElement)

        var rawMessage by mutableStateOf(originalSelectMenuElement.value)
        var targetChannelId by mutableStateOf<String?>(null)

        when (val query = setupJson.testMessageTargetChannelQuery) {
            is TestMessageTargetChannelQuery.QuerySelector -> {
                val targetQuery = document.querySelector<HTMLElement>(query.querySelector)

                fun updateSelect() {
                    val value = targetQuery.asDynamic().value
                    targetChannelId = value as String?
                }

                targetQuery.addEventListener(
                    InputEvent.INPUT,
                    {
                        updateSelect()
                    }
                )

                updateSelect()
            }

            TestMessageTargetChannelQuery.SendDirectMessage -> targetChannelId = "dm" // wow this is a hack
        }

        renderComposable(selectMenuWrapperElement.unsafeCast<web.html.HTMLElement>()) {
            val _targetChannelId = targetChannelId

            DiscordMessageEditor(
                m,
                setupJson.templates,
                setupJson.placeholderSectionType,
                setupJson.placeholders,
                setupJson.guild,
                setupJson.testMessageEndpointUrl,
                if (_targetChannelId != null) {
                    if (_targetChannelId == "dm") {
                        TargetChannelResult.DirectMessageTarget
                    } else {
                        TargetChannelResult.GuildMessageChannelTarget(_targetChannelId.toLong())
                    }
                } else {
                    TargetChannelResult.ChannelNotSelected
                },
                setupJson.selfLorittaUser,
                listOf(),
                listOf(),
                rawMessage
            ) {
                // Update our variable
                rawMessage = it

                // And update the backing textarea
                originalSelectMenuElement.value = rawMessage

                // And dispatch an input event for anyone that's listening to it
                originalSelectMenuElement.dispatchEvent(web.events.Event(InputEvent.INPUT, EventInit(bubbles = true, cancelable = true)))
            }
        }
    }
}