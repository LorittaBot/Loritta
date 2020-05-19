package net.perfectdreams.spicymorenitta.routes

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.parseList
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import kotlin.browser.document
import kotlin.collections.set

class DonateRoute(val m: SpicyMorenitta) : BaseRoute("/donate") {
    override fun onRender(call: ApplicationCall) {
        val table = page.getElementById("donate-features") as HTMLDivElement
        val plansTable = page.getElementById("plans-features") as HTMLDivElement

        val rewards = listOf(
                DonationReward("ignore_me", 0.0, false),
                DonationReward("ignore_me", 99.99, false),

                // ===[  ESSENTIAL  ]===
                DonationReward("Lori irá parar de perturbar você e os membros do seu servidor com pedidos de doação", 19.99, false) ,

                // ===[ RECOMMENDED ]===
                DonationReward("Badge EXCLUSIVA no \"+perfil\" para os membros do seu servidor", 39.99, false),
                DonationReward("Faça seu PRÓPRIO background para o \"+perfil\"", 39.99, false),

                // DonationReward("Personalizar nome/avatar da Loritta nas notificações do YouTube/Twitch/Twitter", 39.99, false),
                DonationReward("Tempo reduzido entre comandos", 39.99, false),

                // ===[  COMPLETE  ]===

                // ===[   NUMBERS  ]===
                DonationReward("Sonhos ganhos a cada minuto", 39.99, false, callback = { column ->
                    when {
                        column >= 99.99 -> +"10"
                        column >= 39.99 -> +"4"
                        column >= 19.99 -> +"2"
                        else -> +"0"
                    }
                }),
                DonationReward("Multiplicador de dailies de sonhos para membros do seu servidor", 19.99, false, callback = { column ->
                    + (ServerPremiumPlans.getPlanFromValue(column).dailyMultiplier.toString() + "x")
                }),
                DonationReward("Máximo de cargos de Level Up", 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).maxLevelUpRoles.toString()
                }),
                DonationReward("Número de Contadores de Membros", 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).memberCounterCount.toString()
                }),
                DonationReward("Máximo de contas de notificações do YouTube/Twitch/Twitter", 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).maxYouTubeChannels.toString()
                }),
                DonationReward("Limite máximo de sonhos no +daily", 39.99, false, callback = { column ->
                    + UserPremiumPlans.getPlanFromValue(column).maxDreamsInDaily.toString()
                }),
                DonationReward("Chance da Lori te dar uma reputação", 39.99, false, callback = { column ->
                    + (UserPremiumPlans.getPlanFromValue(column).loriReputationRetribution.toString() + "%")
                }),
                DonationReward("Multiplicador de XP Global", 119.99, false, callback = { column ->
                    + (ServerPremiumPlans.getPlanFromValue(column).globalXpMultiplier.toString() + "x")
                })
        )

        plansTable.appendBuilder(
                StringBuilder().appendHTML(true).table {
                    style = "margin: 0 auto;"

                    val rewardColumn = mutableListOf<Double>()
                    tr {
                        th { +"" }
                        rewards.asSequence()
                                .map { it.minimumDonation }
                                .distinct()
                                .filter { it == 0.0 || it == 19.99 || it == 39.99 || it == 99.99 }
                                .sortedBy { it }.toList().forEach {
                                    th {
                                        val titlePrefix = when (it) {
                                            0.0 -> "Grátis"
                                            19.99 -> "Essencial"
                                            39.99 -> "Recomendado"
                                            99.99 -> "Completo"
                                            else -> "???"
                                        }

                                        if (it == 0.0) {
                                            style = "opacity: 0.7; font-size: 0.9em;"
                                        }

                                        if (it == 39.99) {
                                            style = "background-color: #83ff836b; font-size: 1.3em;"
                                        }

                                        +("$titlePrefix (R$" + it.toString().replace(".", ",") + ")")
                                    }
                                    rewardColumn.add(it)
                                }
                    }

                    for (reward in rewards.filterNot { it.doNotDisplayInPlans }.filter { it.name != "ignore_me" }) {
                        tr {
                            td {
                                attributes["style"] = "font-weight: 800;"
                                +reward.name
                            }
                            for (column in rewardColumn) {
                                td {
                                    if (column == 0.0) {
                                        style = "opacity: 0.7; font-size: 0.8em;"
                                    }

                                    if (column == 39.99) {
                                        style = "background-color: #83ff836b;"
                                    }
                                    reward.callback.invoke(this, column)
                                }
                            }
                        }
                    }

                    tr {
                        td {
                            + ""
                        }

                        val loginButton = document.getElementById("login-for-donate-url")
                        val needsToLogin = loginButton != null
                        val url = loginButton?.getAttribute("href")

                        td {
                        }

                        td {
                            if (needsToLogin) {
                                a(href = url) {
                                    div(classes = "button-discord button-discord-info pure-button") {
                                        // id = "donate-button-plan1"

                                        i(classes = "fas fa-gift") {}
                                        +" Comprar Plano"
                                    }
                                }
                            } else {
                                div(classes = "button-discord button-discord-info pure-button") {
                                    id = "donate-button-plan1"

                                    i(classes = "fas fa-gift") {}
                                    +" Comprar Plano"
                                }
                            }
                        }

                        td {
                            if (needsToLogin) {
                                style = "background-color: #83ff836b;"
                                a(href = url) {
                                    div(classes = "button-discord button-discord-info pure-button") {
                                        // id = "donate-button-plan2"
                                        style = "font-size: 1.2em;"

                                        i(classes = "fas fa-gift") {}
                                        +" Comprar Plano"
                                    }
                                }
                            } else {
                                style = "background-color: #83ff836b;"
                                div(classes = "button-discord button-discord-info pure-button") {
                                    id = "donate-button-plan2"
                                    style = "font-size: 1.2em;"

                                    i(classes = "fas fa-gift") {}
                                    +" Comprar Plano"
                                }
                            }
                        }

                        td {
                            if (needsToLogin) {
                                a(href = url) {
                                    div(classes = "button-discord button-discord-info pure-button") {
                                        // id = "donate-button-plan3"

                                        i(classes = "fas fa-gift") {}
                                        +" Comprar Plano"
                                    }
                                }
                            } else {
                                div(classes = "button-discord button-discord-info pure-button") {
                                    id = "donate-button-plan3"

                                    i(classes = "fas fa-gift") {}
                                    +" Comprar Plano"
                                }
                            }
                        }
                    }
                }
        )

        // Criar coisas
        table.appendBuilder(
                StringBuilder().appendHTML(true).table {
                    style = "margin: 0 auto;"

                    val rewardColumn = mutableListOf<Double>(0.0)
                    tr {
                        th { +"" }
                        th { +"Nenhuma Doação" }
                        rewards.map { it.minimumDonation }.distinct().sortedBy { it }.forEach {
                            th { +("R$" + it.toString().replace(".", ",") + "+") }
                            rewardColumn.add(it)
                        }
                    }

                    for (reward in rewards.filter { it.name != "ignore_me" }) {
                        tr {
                            td {
                                attributes["style"] = "font-weight: 800;"
                                +reward.name
                            }
                            for (column in rewardColumn) {
                                td {
                                    reward.callback.invoke(this, column)
                                }
                            }
                        }
                    }
                }
        )

        (document.getElementById("donate-button-plan1") as HTMLDivElement?)?.onclick = {
            showDonateModal(19.99)
        }
        (document.getElementById("donate-button-plan2") as HTMLDivElement?)?.onclick = {
            showDonateModal(39.99)
        }
        (document.getElementById("donate-button-plan3") as HTMLDivElement?)?.onclick = {
            showDonateModal(119.99)
        }

        (document.getElementById("donate-button") as HTMLDivElement?)?.onclick = {
            val donationKeysJson = document.getElementById("donation-keys-json")?.innerHTML!!

            val donationKeys = kotlinx.serialization.json.JSON.nonstrict.parseList<ServerConfig.DonationKey>(donationKeysJson)

            if (donationKeys.isNotEmpty()) {
                val modal = TingleModal(
                        TingleOptions(
                                footer = true,
                                cssClass = arrayOf("tingle-modal--overflow")
                        )
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
                                        val o = object {
                                            val money = key.value // unused
                                            val keyId = key.id.toString()
                                        }

                                        println(JSON.stringify(o))

                                        modal.close()

                                        PaymentUtils.openPaymentSelectionModal(o)
                                    }
                                }
                            }
                        }
                )

                modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Eu quero comprar uma nova key", "button-discord button-discord-info pure-button button-discord-modal") {
                    modal.close()
                    showDonateModal(19.99)
                }

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
                TingleOptions(
                        footer = true,
                        cssClass = arrayOf("tingle-modal--overflow")
                )
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
                            + "Obrigada por querer doar para mim! Você não faz ideia de como cada doação me ajuda a sobreviver."
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
            val o = object {
                val money = (visibleModal.getElementsByClassName("how-much-money")[0] as HTMLInputElement).value
            }

            modal.close()

            PaymentUtils.openPaymentSelectionModal(o)
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()
    }

    data class DonationReward(val name: String, val minimumDonation: Double, val doNotDisplayInPlans: Boolean, val callback: TD.(Double) -> Unit = { column ->
        if (column >= minimumDonation) {
            i("fas fa-check") {}
        } else {
            i("fas fa-times") {}
        }
    })
}