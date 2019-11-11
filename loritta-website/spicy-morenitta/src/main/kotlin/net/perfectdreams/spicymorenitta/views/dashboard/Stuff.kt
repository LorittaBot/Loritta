package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.spicymorenitta.utils.loriUrl
import utils.TingleModal
import utils.TingleOptions
import kotlin.browser.document
import kotlin.browser.window

object Stuff {
    fun showPremiumFeatureModal() {
        val modal = TingleModal(
                TingleOptions(
                        footer = true,
                        cssClass = arrayOf("tingle-modal--overflow")
                )
        )

        modal.setContent(
                document.create.div {
                    div(classes = "category-name") {
                        + "Você encontrou uma função premium!"
                    }

                    div {
                        style = "text-align: center;"

                        img(src = "https://i.imgur.com/wEUDTZG.png") {
                            width = "250"
                        }

                        p {
                            + "Você encontrou uma função premium minha! Legal, né?"
                        }
                        p {
                            + "Para ter esta função e muito mais, veja a minha lista de vantagens que você pode ganhar doando!"
                        }
                    }
                }
        )

        modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Vamos lá!", "button-discord button-discord-info pure-button button-discord-modal") {
            window.location.href = "${loriUrl}donate"
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()
    }
}