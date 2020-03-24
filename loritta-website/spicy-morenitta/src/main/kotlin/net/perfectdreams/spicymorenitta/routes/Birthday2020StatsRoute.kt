package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Json

class Birthday2020StatsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/birthday-2020/stats"), Logging {
	val totalPrizeCount: HTMLElement
		get() = document.select<HTMLDivElement>("#total-prize-count")

	var currentPoints = 0

	@UseExperimental(ImplicitReflectionSerializer::class)
	override fun onRender(call: ApplicationCall) {
		super.onRender(call)

		m.launch {
			val teamStatsJob = m.async {
				val result = http.get<String> {
					url("${window.location.origin}/api/v1/birthday-2020/team")
				}
				JSON.parse<Json>(result)
			}

			val teamStats = teamStatsJob.await()
			val isActive = teamStats["isActive"] as Boolean
			val currentTeam = teamStats["team"] as String?

			val rewards = if (currentTeam == "PANTUFA") {
				Birthday2020Route.pantufaRewards
			} else {
				Birthday2020Route.gabrielaRewards
			}

			val body = document.select<HTMLDivElement>("#birthday-2020")

			body.append {
				div {
					h1 {
						+"Você tem "
						span {
							id = "total-prize-count"
							+"X"
						}
						+" presentes!"
					}
					h2 {
						+ "Team ${currentTeam}"
					}

					div {
						id = "next-reward-progress-bar-bg"
						style = "width: 100%; height: 1em; background-color: #b9b909;"

						div {
							id = "next-reward-progress-bar"
							style = "height: 100%; width: 0%; transition: 0.3s; background-color: yellow; box-shadow: yellow 0px 0px 10px, yellow 0px 0px 10px, yellow 0px 0px 10px"
						}
					}
				}
			}

			val collectedPointsSoundEffects = listOf(
					Audio("/assets/snd/present_get1.ogg"),
					Audio("/assets/snd/present_get2.ogg"),
					Audio("/assets/snd/present_get3.ogg"),
					Audio("/assets/snd/present_get4.ogg")
			)

			val rewardSoundEffect = Audio("/assets/snd/ts1_promotion2.mp3")

			val eventSource = EventSource("/api/v1/birthday-2020/stats")

			eventSource.addEventListener("open", {
				console.log(it)
				debug("Open!")
			}, false)

			eventSource.addEventListener("error", {
				debug("Error!")
				console.log(it)
			}, false)

			eventSource.addEventListener("message", {
				console.log(it)

				it as MessageEvent
				it.data as String

				debug(it.data)

				val json = JSON.parse<Json>(it.data as String)
				val type = json["type"] as String?
				if (type == "collectedPoint" || type == "syncPoints") {
					val total = json["total"] as Int
					totalPrizeCount.innerText = total.toString()

					val receivedRewards = rewards.filter { total >= it.requiredPoints }
							.sortedByDescending { it.requiredPoints }
					val nextRewards = rewards.filter { it.requiredPoints > total }
							.sortedBy { it.requiredPoints }

					val lastTierWasAt = receivedRewards.firstOrNull()?.requiredPoints ?: 0

					if (type == "collectedPoint") {
						val sfx = collectedPointsSoundEffects.random()
						sfx.currentTime = 0.0
						sfx.play()

						val hasAnyNewReward = rewards.filter { it.requiredPoints in currentPoints..total }

						if (hasAnyNewReward.isNotEmpty())
							rewardSoundEffect.play()
					}

					if (nextRewards.isNotEmpty()) {
						val nextReward = nextRewards.first()
						val percentage = (total.toDouble() - lastTierWasAt) / (nextReward.requiredPoints - lastTierWasAt)
						val nextRewardProgressBar = document.select<HTMLDivElement>("#next-reward-progress-bar")
						nextRewardProgressBar.style.width = "${percentage * 100}%"
					}

					currentPoints = total
				}
			}, false)
		}
	}
}