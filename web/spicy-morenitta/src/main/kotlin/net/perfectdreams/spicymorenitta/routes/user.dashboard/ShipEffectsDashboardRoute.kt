@file:JsExport
package net.perfectdreams.spicymorenitta.routes.user.dashboard

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JSON
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.page
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement

class ShipEffectsDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/ship-effects") {
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        val premiumAsJson = document.getElementById("ship-effects-json")?.innerHTML!!

        val shipEffects = JSON.nonstrict.decodeFromString(ListSerializer(ShipEffect.serializer()), premiumAsJson)
        val profileAsJson = document.getElementById("profile-json")?.innerHTML!!

        val profile = JSON.nonstrict.decodeFromString(Profile.serializer(), profileAsJson)

        val buyButton = page.getElementById("buy-button") as HTMLButtonElement
        if (3000 > profile.money) {
            buyButton.addClass("button-discord-disabled")
        } else {
            buyButton.addClass("button-discord-success")
        }

        if (profile.money > 3000) {
            buyButton.onclick = {
                prepareSave()
            }
        }

        val el = page.getElementById("ship-active-effects")

        println(shipEffects.size)
        shipEffects.forEach { shipEffect ->
            el.append {
                div {
                    + (shipEffect.user1Id + " + " + shipEffect.user2Id + " com ${shipEffect.editedShipValue}%")
                }
            }
        }
    }

    @JsName("buy")
    fun prepareSave() {
        SaveUtils.prepareSave("ship_effect", endpoint = "${loriUrl}api/v1/users/self-profile", extras = {
            it["buyItem"] = "ship_effect"
            it["editedValue"] = (page.getElementById("newShipValue") as HTMLInputElement).value
            var userNamePlusDiscriminator = (page.getElementById("userName") as HTMLInputElement).value
            if (userNamePlusDiscriminator.contains("#")) {
                val split = userNamePlusDiscriminator.split("#")
                userNamePlusDiscriminator = split[0].trim() + "#" + split[1]
            }
            it["user2NamePlusDiscriminator"] = userNamePlusDiscriminator
        }, onFinish = {
            if (it.statusCode in 200..299) {
                window.location.href = window.location.href + "?bought"
            } else {

            }
        })
    }

    @Serializable
    class Profile(
            val id: Long,
            val money: Double
    )

    @Serializable
    class ShipEffect(
            val buyerId: String,
            val user1Id: String,
            val user2Id: String,
            val editedShipValue: Int,
            val expiresAt: Long
    )
}