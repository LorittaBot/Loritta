package net.perfectdreams.spicymorenitta.routes.user.dashboard

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import kotlinx.serialization.parseList
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.page
import kotlin.browser.document
import kotlin.dom.clear

class ProfileListDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/profiles") {
    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        val premiumAsJson = document.getElementById("profile-list-json")?.innerHTML!!
        val profileAsJson = document.getElementById("profile-json")?.innerHTML!!

        val shipEffects = JSON.nonstrict.parseList<ProfileLayout>(premiumAsJson)
        val profile = JSON.nonstrict.parse<Profile>(profileAsJson)

        generateEntries(profile, shipEffects)
    }

    @ImplicitReflectionSerializer
    fun generateEntries(profile: Profile, shipEffects: List<ProfileLayout>) {
        val el = page.getElementById("ship-active-effects")

        el.clear()

        el.append {
            div(classes = "pure-g vertically-centered-content") {
                shipEffects.sortedBy { if (it.price == -1.0) 9999999.0 else it.price }.forEach { shipEffect ->
                    println(shipEffect.internalName + " - " + shipEffect.activated)
                    div(classes = "pure-u-1 pure-u-md-1-3") {
                        div {
                            style = "text-align: center;"
                            img(src = "${loriUrl}assets/img/profiles/${shipEffect.shortName}.png") {
                                style = "width: 100%;"
                            }
                            h2 {
                                +when (shipEffect.internalName) {
                                    "NostalgiaProfileCreator" -> "Padrão"
                                    "DefaultProfileCreator" -> "Moderno"
                                    "MSNProfileCreator" -> "MSN"
                                    "OrkutProfileCreator" -> "Orkut"
                                    "MonicaAtaProfileCreator" -> "Mônica \"ata\""
                                    "CowboyProfileCreator" -> "Cowboy"
                                    "LoriAtaProfileCreator" -> "Loritta \"ata\""
                                    "UndertaleProfileCreator" -> "Undertale Battle"
                                    "PlainWhiteProfileCreator" -> "Simplesmente Branco"
                                    "PlainOrangeProfileCreator" -> "Simplesmente Laranja"
                                    "PlainPurpleProfileCreator" -> "Simplesmente Roxo"
                                    "PlainAquaProfileCreator" -> "Simplesmente Azul"
                                    "PlainGreenProfileCreator" -> "Simplesmente Verde"
                                    "PlainGreenHeartsProfileCreator" -> "Simplesmente Verde com Flores"
                                    "NextGenProfileCreator" -> "Próxima Geração"
                                    "Halloween2019ProfileCreator" -> "Evento de Halloween 2019"
                                    "Christmas2019ProfileCreator" -> "Evento de Natal 2019"
                                    "LorittaChristmas2019ProfileCreator" -> "Evento de Natal 2019"
                                    else -> shipEffect.internalName
                                }
                            }
                            h3 {
                                if (shipEffect.price != -1.0) {
                                    +"Preço: ${shipEffect.price} sonhos"
                                }
                            }
                            if (shipEffect.price != -1.0) {
                                div(classes = "button-discord pure-button") {
                                    style = "font-size: 1.25em; margin: 5px;"

                                    if (shipEffect.alreadyBought || shipEffect.price > profile.money) {
                                        classes += "button-discord-disabled"
                                    } else {
                                        classes += "button-discord-info"
                                        onClickFunction = {
                                            SaveUtils.prepareSave("profile_design", endpoint = "${loriUrl}api/v1/users/self-profile", extras = {
                                                it["buyItem"] = "profile"
                                                it["profileType"] = shipEffect.internalName
                                            }, onFinish = {
                                                if (it.statusCode in 200..299) {
                                                    generateEntries(profile, JSON.nonstrict.parseList(it.body))
                                                }
                                            })
                                        }
                                    }

                                    +"Comprar"
                                }
                            }
                            div(classes = "button-discord pure-button") {
                                if (!shipEffect.alreadyBought || shipEffect.activated) {
                                    classes += "button-discord-disabled"
                                } else {
                                    classes += "button-discord-info"

                                    onClickFunction = {
                                        SaveUtils.prepareSave("profile_design", endpoint = "${loriUrl}api/v1/users/self-profile", extras = {
                                            it["setActiveProfileDesign"] = shipEffect.internalName
                                        }, onFinish = {
                                            if (it.statusCode in 200..299) {
                                                generateEntries(profile, JSON.nonstrict.parseList(it.body))
                                            }
                                        })
                                    }
                                }

                                style = "font-size: 1.25em; margin: 5px;"
                                +"Ativar"
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    class Profile(
            val id: String,
            val money: Double
    )

    @Serializable
    class ProfileLayout(
            val internalName: String,
            val shortName: String,
            val price: Double,
            val alreadyBought: Boolean,
            val activated: Boolean
    )
}