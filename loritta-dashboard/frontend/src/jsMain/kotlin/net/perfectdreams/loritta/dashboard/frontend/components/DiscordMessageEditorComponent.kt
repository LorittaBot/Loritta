package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButtonType
import net.perfectdreams.loritta.dashboard.frontend.compose.components.RawHtml
import net.perfectdreams.loritta.dashboard.frontend.compose.components.messages.DiscordMessageEditor
import net.perfectdreams.loritta.dashboard.frontend.compose.components.messages.JsonForDiscordMessages
import net.perfectdreams.loritta.dashboard.frontend.compose.components.messages.TargetChannelResult
import net.perfectdreams.loritta.dashboard.frontend.modals.Modal
import net.perfectdreams.loritta.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.renderer.discordMessageRenderer
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLInputElement
import web.html.HTMLSelectElement
import web.html.HTMLTextAreaElement
import web.input.INPUT
import web.input.InputEvent
import web.input.InputEventInit
import web.pointer.CLICK
import web.pointer.PointerEvent

class DiscordMessageEditorComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLTextAreaElement>() {
    override fun onMount() {
        mountedElement.style.display = "none"

        val rootNode = document.createElement("div")
        // This looks stupid, "why do we need this *here*?"
        // Well, this fixes a bug with the message preview overflowing the parent container for... some reason!?
        rootNode.style.width = "100%"
        mountedElement.before(rootNode)

        var rawMessage by mutableStateOf(mountedElement.value)
        val bootstrap = Json.decodeFromString<MessageEditorBootstrap>(BlissHex.decodeFromHexString(mountedElement.getAttribute("discord-message-editor-bootstrap")!!))

        val verifiedIcon = SVGIconManager.fromRawHtml(bootstrap.verifiedIconRawHtml)
        val eyeDropperIcon = SVGIconManager.fromRawHtml(bootstrap.eyeDropperIconRawHtml)
        val chevronDownIcon = SVGIconManager.fromRawHtml(bootstrap.chevronDownIconRawHtml)

        val buttonEditorTarget = document.querySelector("[discord-message-editor-button-for='${mountedElement.name}']")!!

        buttonEditorTarget.addEventHandler(PointerEvent.CLICK) {
            m.modalManager.openModalWithOnlyCloseButton(
                "Editor de Mensagem",
                Modal.Size.LARGE,
                {
                    DiscordMessageEditor(
                        m,
                        bootstrap.templates,
                        bootstrap.placeholders,
                        bootstrap.guild,
                        when (val target = bootstrap.testMessageTarget) {
                            is MessageEditorBootstrap.TestMessageTarget.QuerySelector -> {
                                val targetElement = document.querySelector(target.querySelector)

                                val channelId = when (targetElement) {
                                    is HTMLInputElement -> targetElement.value
                                    is HTMLTextAreaElement -> targetElement.value
                                    is HTMLSelectElement -> targetElement.value
                                    else -> error("Unsupported element ${target.querySelector}!")
                                }

                                TargetChannelResult.GuildMessageChannelTarget(channelId.toLong())
                            }
                            MessageEditorBootstrap.TestMessageTarget.SendDirectMessage -> TargetChannelResult.DirectMessageTarget
                            MessageEditorBootstrap.TestMessageTarget.Unavailable -> TargetChannelResult.ChannelNotSelected
                        },
                        bootstrap.selfUser,
                        verifiedIcon,
                        eyeDropperIcon,
                        chevronDownIcon,
                        rawMessage,
                        onMessageContentChange = {
                            rawMessage = it
                            mountedElement.value = it
                            mountedElement.dispatchEvent(
                                InputEvent(
                                    InputEvent.INPUT,
                                    InputEventInit(
                                        bubbles = true
                                    )
                                )
                            )
                        }
                    )
                }
            )
        }

        renderComposable(rootNode) {
            val parsedMessage = try {
                JsonForDiscordMessages.decodeFromString<DiscordMessage>(rawMessage)
            } catch (e: SerializationException) {
                null
            } catch (e: IllegalStateException) {
                null // This may be triggered when a message has invalid message components
            } catch (e: IllegalArgumentException) {
                null // This may be triggered when a message has invalid message components²
            } ?: DiscordMessage(
                content = rawMessage
            )

            Div(attrs = { classes("message-preview-section") }) {
                Div(attrs = { classes("message-preview-wrapper") }) {
                    Div(attrs = { classes("message-preview") }) {
                        RawHtml(createHTML(false).div {
                            discordMessageRenderer(
                                bootstrap.selfUser,
                                parsedMessage,
                                null,
                                bootstrap.verifiedIconRawHtml,
                                bootstrap.guild.channels,
                                bootstrap.guild.roles,
                                bootstrap.placeholders,
                            )
                        })
                    }
                }
            }
        }
    }

    override fun onUnmount() {}
}