package net.perfectdreams.spicymorenitta.views

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.spicymorenitta.utils.Audio
import net.perfectdreams.spicymorenitta.utils.HttpRequest
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.page
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlin.js.Json
import kotlin.js.json

object ReputationView {
	@JsName("recaptchaCallback")
	fun recaptchaCallback(userId: String, token: String) {
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
					url = "${loriUrl}api/v1/user/$userId/reputation",
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
}