package net.perfectdreams.spicymorenitta.views.dashboard

import LoriDashboard
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.page
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.browser.document

@ImplicitReflectionSerializer
object BadgeView {
    @JsName("start")
    fun start() {
        document.addEventListener("DOMContentLoaded", {
            val premiumAsJson = document.getElementById("badge-json")?.innerHTML!!

            println("premiumAsJson: $premiumAsJson")

            val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

            (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked = guild.donationConfig.customBadge

            LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1") {
                if (guild.donationKey == null || 19.99 > guild.donationKey.value) {
                    Stuff.showPremiumFeatureModal()
                    return@applyBlur false
                }
                return@applyBlur true
            }
        })
    }

    @JsName("prepareSave")
    fun prepareSave() {
        val file = page.getElementById("upload-badge").asDynamic().files[0]

        if (file != null) {
            val reader = FileReader()

            reader.readAsDataURL(file)
            reader.onload = {
                val imageAsBase64 = reader.result
                save(imageAsBase64 as? String)
            }
        } else {
            save(null)
        }
    }

    fun save(base64Image: String?) {
        SaveUtils.prepareSave("badge", extras = {
            it["customBadge"] = (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked
            it["badgeImage"] = base64Image
        })
    }
}