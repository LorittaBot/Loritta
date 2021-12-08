package net.perfectdreams.spicymorenitta.routes

import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import kotlinx.serialization.builtins.ListSerializer
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.PaymentUtils
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import net.perfectdreams.spicymorenitta.utils.appendBuilder
import net.perfectdreams.spicymorenitta.utils.page
import net.perfectdreams.spicymorenitta.utils.trackOverflowChanges
import net.perfectdreams.spicymorenitta.utils.visibleModal
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import kotlin.collections.set

class DonateRoute(val m: SpicyMorenitta) : BaseRoute("/donate") {
    companion object {
        const val LOCALE_PREFIX = "website.donate"
    }

    override fun onRender(call: ApplicationCall) {
        val plansTable = page.getElementById("plans-features") as HTMLDivElement

        val rewards = listOf(
                DonationReward("ignore_me", 0.0, false),
                DonationReward("ignore_me", 99.99, false),

                // ===[ RECOMMENDED ]===
                DonationReward(locale["${LOCALE_PREFIX}.rewards.exclusiveProfileBadge"], 39.99, false),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.customProfileBackground"], 39.99, false),

                // DonationReward("Personalizar nome/avatar da Loritta nas notificações do YouTube/Twitch/Twitter", 39.99, false),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.reducedCooldown"], 39.99, false),

                // ===[  COMPLETE  ]===

                // ===[   NUMBERS  ]===
                DonationReward(locale["${LOCALE_PREFIX}.rewards.everyMinuteSonhos"], 39.99, false, callback = { column ->
                    when {
                        column >= 99.99 -> +"10"
                        column >= 39.99 -> +"4"
                        column >= 19.99 -> +"2"
                        else -> +"0"
                    }
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.dailyMultiplier"], 19.99, false, callback = { column ->
                    + (ServerPremiumPlans.getPlanFromValue(column).dailyMultiplier.toString() + "x")
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.maxLevelUpRoles"], 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).maxLevelUpRoles.toString()
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.maxMemberCounters"], 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).memberCounterCount.toString()
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.maxSocialAccountsRelay"], 19.99, false, callback = { column ->
                    + ServerPremiumPlans.getPlanFromValue(column).maxYouTubeChannels.toString()
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.maxDailyLimit"], 39.99, false, callback = { column ->
                    + UserPremiumPlans.getPlanFromValue(column).maxDreamsInDaily.toString()
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.giveBackRepChange"], 39.99, false, callback = { column ->
                    + (UserPremiumPlans.getPlanFromValue(column).loriReputationRetribution.toString() + "%")
                }),
                DonationReward(locale["${LOCALE_PREFIX}.rewards.globalExperienceMultiplier"], 99.99, false, callback = { column ->
                    + (ServerPremiumPlans.getPlanFromValue(column).globalXpMultiplier.toString() + "x")
                })
        )

        plansTable.appendBuilder(
                StringBuilder().appendHTML(true).table(classes = "fancy-table centered-text") {
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
                                            0.0 -> locale["${LOCALE_PREFIX}.plans.free"]
                                            19.99 -> locale["${LOCALE_PREFIX}.plans.essential"]
                                            39.99 -> locale["${LOCALE_PREFIX}.plans.recommended"]
                                            99.99 -> locale["${LOCALE_PREFIX}.plans.complete"]
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
                        // =====[ PREMIUM PLANS ]=====
                        td {
                            + ""
                        }

                        val needsToLogin = m.userIdentification == null
                        val url = "https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email&response_type=code&client_id=297153970613387264"

                        td {
                        }

                        fun TD.createBuyPlanButton(buttonPlanId: String, isBigger: Boolean) {
                            if (isBigger)
                                style = "background-color: #83ff836b;"

                            if (needsToLogin) {
                                a(href = url) {
                                    div(classes = "button-discord button-discord-info pure-button") {
                                        if (isBigger)
                                            style = "font-size: 1.2em;"

                                        i(classes = "fas fa-gift") {}
                                        +" ${locale["${LOCALE_PREFIX}.buyPlan"]}"
                                    }
                                }
                            } else {
                                div(classes = "button-discord button-discord-info pure-button") {
                                    id = buttonPlanId
                                    if (isBigger)
                                        style = "font-size: 1.2em;"

                                    i(classes = "fas fa-gift") {}
                                    +" ${locale["${LOCALE_PREFIX}.buyPlan"]}"
                                }
                            }
                        }

                        td {
                            createBuyPlanButton("donate-button-plan1", false)
                        }

                        td {
                            createBuyPlanButton("donate-button-plan2", true)
                        }

                        td {
                            createBuyPlanButton("donate-button-plan3", false)
                        }
                    }
                }
        )

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

                                        PaymentUtils.requestAndRedirectToPaymentUrl(o)
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
        val o = object {
            val money = price
        }

        PaymentUtils.requestAndRedirectToPaymentUrl(o)
    }

    data class DonationReward(val name: String, val minimumDonation: Double, val doNotDisplayInPlans: Boolean, val callback: TD.(Double) -> Unit = { column ->
        if (column >= minimumDonation) {
            i("fas fa-check") {}
        } else {
            i("fas fa-times") {}
        }
    })
}