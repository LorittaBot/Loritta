package net.perfectdreams.spicymorenitta.routes

import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get

class DonateRoute(val m: SpicyMorenitta) : BaseRoute("/donate") {
    override fun onRender(call: ApplicationCall) {
        (document.getElementById("donate-button-plan1") as HTMLDivElement?)?.onclick = {
            showPaymentSelectionModal(19.99)
        }
        (document.getElementById("donate-button-plan2") as HTMLDivElement?)?.onclick = {
            showPaymentSelectionModal(39.99)
        }
        (document.getElementById("donate-button-plan3") as HTMLDivElement?)?.onclick = {
            showPaymentSelectionModal(99.99)
        }

        (document.getElementById("renew-button") as HTMLDivElement?)?.onclick = {
            val donationKeysJson = document.getElementById("donation-keys-json")?.innerHTML!!

            val donationKeys = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(ListSerializer(ServerConfig.DonationKey.serializer()), donationKeysJson)

            if (donationKeys.isNotEmpty()) {
                val modal = TingleModal(
                    jsObject<TingleOptions> {
                        footer = true
                        cssClass = arrayOf("tingle-modal--overflow")
                        closeMethods = arrayOf()
                    }
                )

                modal.setContent(
                    document.create.div {
                        div(classes = "category-name") {
                            + "Suas keys atuais"
                        }

                        p {
                            + "Parece que você já possui algumas keys, você deseja renovar elas?"
                        }

                        for (key in donationKeys) {
                            h2 {
                                + "Key ${key.id} (R$ ${key.value})"
                            }
                            h3 {
                                + "Você pode renovar ela por apenas R$ ${key.value * 0.8}!"
                            }

                            div(classes = "button-discord button-discord-info pure-button") {
                                style = "font-size: 1.25em; margin: 5px;"
                                + "Renovar"

                                onClickFunction = {
                                    modal.close()

                                    PaymentUtils.requestAndRedirectToPaymentUrl(
                                        buildJsonObject {
                                            put("money", key.value)
                                            put("keyId", key.id.toString())
                                        }
                                    )
                                }
                            }
                        }
                    }
                )

                /* modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Eu quero comprar uma nova key", "button-discord button-discord-info pure-button button-discord-modal") {
                    modal.close()
                    showDonateModal(19.99)
                } */

                modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
                    modal.close()
                }

                modal.open()
            } else {
                showDonateModal(19.99)
            }
        }
    }

    fun showDonateModal(inputValue: Double) {
        val modal = TingleModal(
            jsObject<TingleOptions> {
                footer = true
                cssClass = arrayOf("tingle-modal--overflow")
                closeMethods = arrayOf()
            }
        )

        modal.setContent(
            document.create.div {
                div(classes = "category-name") {
                    + locale["website.donate.areYouGoingToDonate"]
                }
                div {
                    style = "text-align: center;"
                    img {
                        src = "https://cdn.discordapp.com/attachments/510601125221761054/535199384535826442/FreshLori.gif"
                    }
                    p {
                        + "Obrigada por querer doar para mim! Você não faz ideia de como cada compra me ajuda a sobreviver."
                    }
                    p {
                        + "Antes de doar, veja todas as recompensas que você pode ganhar doando a quantidade que você deseja!"
                    }
                    p {
                        + "Mas então... Quanto você vai querer doar?"
                    }

                    input(InputType.number, classes = "how-much-money") {
                        min = "1"
                        max = "1000"
                        value = inputValue.toString()
                        step = "0.10"
                    }

                    + " reais"

                    p {
                        + "Não se esqueça de entrar no meu servidor de suporte caso você tenha dúvidas sobre as vantagens, formas de pagamento e, na pior das hipóteses, se der algum problema. (vai se dá algum problema, né?)"
                    }
                    /* div {
                        div(classes = "button-discord button-discord-info pure-button") {
                            style = "font-size: 1.25em; margin: 5px;"
                            + "PayPal (Cartão de Crédito e Saldo do PayPal)"
                        }
                    } */
                }
            }
        )

        modal.addFooterBtn("<i class=\"fas fa-cash-register\"></i> Escolher Forma de Pagamento", "button-discord button-discord-info pure-button button-discord-modal") {
            modal.close()

            showPaymentSelectionModal((visibleModal.getElementsByClassName("how-much-money")[0] as HTMLInputElement).value.toDouble())
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()
        modal.trackOverflowChanges(m)
    }

    fun showPaymentSelectionModal(price: Double) {
        PaymentUtils.requestAndRedirectToPaymentUrl(
            buildJsonObject {
                put("money", price)
            }
        )
    }
}