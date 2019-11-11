package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.DateUtils
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.page
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.js.Date

@ImplicitReflectionSerializer
object PremiumKeyView {
    @JsName("start")
    fun start() {
        document.addEventListener("DOMContentLoaded", {
            val premiumAsJson = document.getElementById("premium-json")?.innerHTML!!

            println("premiumAsJson: $premiumAsJson")

            val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

            generateStuff(guild)
        })
    }

    fun generateStuff(guild: ServerConfig.Guild) {
        val premiumContent = page.getElementById("premium-content")
        premiumContent.clear()

        premiumContent.appendChild(
                document.create.div {
                    id = "premium-stuff"
                    val donationKey = guild.donationKey
                    div {
                        style = "text-align: center;"
                        if (donationKey == null) {
                            h1 { +"Você não tem nenhuma key ativada neste servidor!" }
                        } else {
                            h1 { +"Você está usando key ${donationKey.id}" }
                        }
                    }
                    hr {}
                    h1 { +"Suas Keys" }
                    // Apenas mostrar keys que estão ainda válidas (Ou seja, que ainda não expiraram!)
                    for (donationKey in guild.selfMember.donationKeys!!.filter { it.expiresAt >= Date().getTime() }) {
                        createKeyEntry(guild.selfMember, donationKey)
                    }
                })
    }

    fun DIV.createKeyEntry(selfMember: ServerConfig.SelfMember, donationKey: ServerConfig.DonationKey) {
        this.div(classes = "discord-generic-entry timer-entry") {
            img(classes = "amino-small-image") {
                style = "width: 6%; height: auto; border-radius: 999999px; float: left; position: relative; bottom: 8px;"
                if (donationKey.usesKey != null) {
                    src = donationKey.usesKey.iconUrl ?: selfMember.effectiveAvatarUrl
                } else {
                    src = selfMember.effectiveAvatarUrl
                }
            }
            div(classes = "pure-g") {
                div(classes = "pure-u-1 pure-u-md-18-24") {
                    div {
                        style = "margin-left: 10px; margin-right: 10;"
                        div(classes = "amino-title entry-title") {
                            style = "font-family: Whitney,Helvetica Neue,Helvetica,Arial,sans-serif;"
                            + "Key ${donationKey.id}"
                        }
                        div(classes = "amino-title toggleSubText") {
                            if (donationKey.usesKey != null) {
                                +"R$${donationKey.value} • Ativado em ${donationKey.usesKey.name} • Expirará em ${DateUtils.formatDateDiff(Date().getTime(), donationKey.expiresAt.toDouble())}"
                            } else {
                                +"R$${donationKey.value} • Expirará em ${DateUtils.formatDateDiff(Date().getTime(), donationKey.expiresAt.toDouble())}"
                            }
                        }
                    }
                }
                div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-right-aligned") {
                    button(classes="button-discord button-discord-edit pure-button edit-timer-button") {
                        onClickFunction = {
                            println("Saving!")
                            SaveUtils.prepareSave("premium", {
                                it["keyId"] = donationKey.id
                            }, onFinish = {
                                val guild = JSON.nonstrict.parse<ServerConfig.Guild>(it.body)

                                generateStuff(guild)
                            })
                        }
                        + "Ativar"
                    }
                }
            }
        }
    }
}