package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import jq
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.Audio
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URLSearchParams
import utils.GoogleRecaptcha
import utils.RecaptchaOptions
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
            val response = http.post("${loriUrl}api/v1/users/$userId/reputation") {
                setBody(JSON.stringify(json))
            }

            val text = response.bodyAsText()

            println("Received: $text")

            if (response.status.value == 200) {
                println("Deu certo!")
                val ts1SkillUp = Audio("${loriUrl}assets/snd/ts1_skill.mp3")
                ts1SkillUp.play()

                updateReputationLeaderboard()

                page.getElementByClass("reputation-button").addClass("button-discord-disabled")
                page.getElementByClass("reputation-button").removeClass("button-discord-info")
            } else {
                println("Deu ruim!!!")
            }
        }
    }

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

        updateReputationLeaderboard()
    }

    fun updateReputationLeaderboard() {
        SpicyMorenitta.INSTANCE.launch {
            val leaderboardResultAsString = http.get("${loriUrl}api/v1/users/$userId/reputation")
                .bodyAsText()
            val leaderboardResult = kotlinx.serialization.json.Json.decodeFromString(ReputationLeaderboardResponse.serializer(), leaderboardResultAsString)

            page.getElementByClass("reputation-count").textContent = leaderboardResult.count.toString()

            val element = document.select<HTMLDivElement>(".leaderboard")

            element.clear()

            element.append {
                div(classes = "box-item") {
                    var idx = 0
                    div(classes = "rank-title") {
                        +"Placar de Reputações"
                    }
                    table {
                        tbody {
                            tr {
                                th {
                                    + "Posição"
                                }
                                th {}
                                th {
                                    + "Nome"
                                }
                            }
                            for ((count, rankUser) in leaderboardResult.rank) {
                                if (idx == 5) break

                                tr {
                                    td {
                                        img(classes = "rank-avatar", src = rankUser.effectiveAvatarUrl) { width = "64" }
                                    }
                                    td(classes = "rank-position") {
                                        +"#${idx + 1}"
                                    }
                                    td {
                                        if (idx == 0) {
                                            div(classes = "rank-name rainbow") {
                                                +rankUser.name
                                            }

                                        } else {
                                            div(classes = "rank-name") {
                                                +rankUser.name
                                            }
                                        }
                                        div(classes = "reputations-received") {
                                            +"$count reputações"
                                        }
                                    }
                                }
                                idx++
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class ReputationLeaderboardResponse(
            val count: Int,
            val rank: List<ReputationLeaderboardEntry>
    )

    @Serializable
    data class ReputationLeaderboardEntry(
            val count: Int,
            val user: ServerConfig.SelfMember
    )
}