package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.page
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.browser.document

@ImplicitReflectionSerializer
object DailyMultiplierView {
    @JsName("start")
    fun start() {
        document.addEventListener("DOMContentLoaded", {
            println("a")
            val premiumAsJson = document.getElementById("daily-multiplier-json")?.innerHTML!!

            println("premiumAsJson: $premiumAsJson")

            val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

            (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked = guild.donationConfig.dailyMultiplier

            LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1") {
                if (guild.donationKey == null || 59.99 > guild.donationKey.value) {
                    Stuff.showPremiumFeatureModal()
                    return@applyBlur false
                }
                return@applyBlur true
            }
        })
    }

    @JsName("prepareSave")
    fun prepareSave() {
        SaveUtils.prepareSave("daily_multiplier", extras = {
            it["dailyMultiplier"] = (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked
        })
    }
}