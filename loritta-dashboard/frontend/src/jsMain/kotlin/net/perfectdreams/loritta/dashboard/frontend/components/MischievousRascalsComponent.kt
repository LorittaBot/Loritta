package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButtonType
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import org.jetbrains.compose.web.dom.Text
import web.cssom.ClassName
import web.dom.document
import web.events.CHANGE
import web.events.CLICK
import web.events.Event
import web.events.addEventHandler
import web.html.HTMLAnchorElement
import web.html.HTMLDivElement
import web.html.HTMLInputElement

class MischievousRascalsComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLAnchorElement>() {
    override fun onMount() {
        this.registeredEvents += this.mountedElement.addEventHandler(
            Event.CLICK
        ) {
            val gameState = (document.querySelector("[bliss-component='loritta-shimeji']").asDynamic().blissComponent as LorittaShimejiComponent).gameState

            m.modalManager.openModalWithCloseButton(
                "Pestinhas Travessas",
                {
                    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                        onClick {
                            gameState.spawnPlayer(LorittaPlayer.PlayerType.LORITTA)
                        }
                    }) {
                        Text("Invocar Loritta")
                    }

                    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                        onClick {
                            gameState.spawnPlayer(LorittaPlayer.PlayerType.PANTUFA)
                        }
                    }) {
                        Text("Invocar Pantufa")
                    }

                    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                        onClick {
                            gameState.spawnPlayer(LorittaPlayer.PlayerType.GABRIELA)
                        }
                    }) {
                        Text("Invocar Gabriela")
                    }

                    DiscordButton(DiscordButtonType.DANGER, attrs = {
                        onClick {
                            while (gameState.entities.isNotEmpty()) {
                                gameState.entities.removeFirst().remove()
                            }
                        }
                    }) {
                        Text("Limpar Pestinhas")
                    }
                }
            )
        }
    }

    override fun onUnmount() {}
}