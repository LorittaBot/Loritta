package net.perfectdreams.spicymorenitta.routes

import jq
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.*
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URLSearchParams
import utils.GoogleRecaptcha
import utils.RecaptchaOptions
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlin.js.Json
import kotlin.js.json

class ReputationRoute : BaseRoute("/user/{userId}/rep") {
    var userId: String? = null

    companion object {
        @JsName("recaptchaCallback")
        fun recaptchaCallback(response: String) {
            val currentRoute = SpicyMorenitta.INSTANCE.currentRoute
            if (currentRoute is ReputationRoute)
                currentRoute.recaptchaCallback(response)
        }
    }

    fun recaptchaCallback(token: String) {
        println("reCAPTCHA token is: $token")
        val urlParams = URLSearchParams(window.location.search)
        val guildId = urlParams.get("guild")
        val channelId = urlParams.get("channel")
        println("Guild is $guildId")
        println("Channel is $channelId")

        val json = json(
                "content" to (page.getElementById("reputation-reason") as HTMLTextAreaElement).value,
                "token" to token,
                "guildId" to guildId,
                "channelId" to channelId
        )

        println(json.toString())
        println(JSON.stringify(json))

        GlobalScope.launch {
            val response = HttpRequest.post(
                    url = "${loriUrl}api/v1/users/$userId/reputation",
                    data = JSON.stringify(json)
            )

            println("Received: " + response.body)
            val payload = JSON.parse<Json>(response.body)

            if (response.statusCode == 200) {
                println("Deu certo!")
                val ts1SkillUp = Audio("${loriUrl}assets/snd/ts1_skill.mp3")
                ts1SkillUp.play()
                page.getElementByClass("reputation-button").addClass("button-discord-disabled")
                page.getElementByClass("reputation-button").removeClass("button-discord-info")
                page.getElementByClass("reputation-count").innerHTML = (payload["count"] as Int).toString()
                page.getElementByClass("leaderboard").outerHTML = payload["rank"] as String
            } else {
                println("Deu ruim!!!")
            }
        }
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        userId = call.parameters["userId"]!!

        val reputationButton = document.select<HTMLButtonElement>("#reputation-button")

        if (!reputationButton.hasAttribute("data-can-give-at") && !reputationButton.hasAttribute("data-need-login")) {
            reputationButton.addClass("button-discord-info")
            reputationButton.removeClass("button-discord-disabled")

            GoogleRecaptchaUtils.render(jq("#reputation-captcha").get()[0], RecaptchaOptions(
                    "6Ld273kUAAAAAIIKfAhF4eIhBmOC80M6rx4sY2NE",
                    "recaptchaCallback",
                    "invisible"
            ))

            reputationButton.onClick {
                GoogleRecaptcha.execute()
            }
        }
    }
}