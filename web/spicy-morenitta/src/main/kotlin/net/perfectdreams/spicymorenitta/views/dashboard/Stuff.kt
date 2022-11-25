package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.html.*
import kotlinx.html.dom.create
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import kotlinx.browser.document
import kotlinx.browser.window
import net.perfectdreams.spicymorenitta.utils.jsObject

object Stuff {
    fun showPremiumFeatureModal(description: (DIV.() -> (Unit))? = null) {
        val modal = TingleModal(
            jsObject<TingleOptions> {
                footer = true
                cssClass = arrayOf("tingle-modal--overflow")
            }
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

                        if (description != null) {
                            description.invoke(this)
                        } else {
                            p {
                                +"Você encontrou uma função premium minha! Legal, né?"
                            }
                            p {
                                +"Para ter esta função e muito mais, veja a minha lista de vantagens que você pode ganhar doando!"
                            }
                        }
                    }
                }
        )

        modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Vamos lá!", "button-discord button-discord-info pure-button button-discord-modal") {
            window.open("${loriUrl}donate", "_blank")
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()
    }
}