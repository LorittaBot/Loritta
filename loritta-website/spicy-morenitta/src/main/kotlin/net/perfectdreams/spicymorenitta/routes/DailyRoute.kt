package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.parse
import loriUrl
import net.perfectdreams.loritta.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.GoogleRecaptchaUtils
import net.perfectdreams.spicymorenitta.utils.LoriWebCode
import net.perfectdreams.spicymorenitta.utils.locale.buildAsHtml
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.collections.set
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json
import kotlin.random.Random

class DailyRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/daily") {
    override val keepLoadingScreen: Boolean
        get() = true

    val dailyNotification: Element
        get() = document.select<HTMLElement>(".daily-notification")
    val dailyRewardButton: Element
        get() = document.select<HTMLElement>(".daily-reward-button")
    val dailyPrewrapper: Element
        get() = document.select<HTMLDivElement>("#daily-prewrapper")
    val dailyWrapper: Element
        get() = document.select<HTMLDivElement>("#daily-wrapper")

    companion object {
        const val USER_PADDING = 2

        @ImplicitReflectionSerializer
        @JsName("recaptchaCallback")
        fun recaptchaCallback(response: String) {
            val currentRoute = SpicyMorenitta.INSTANCE.currentRoute
            if (currentRoute is DailyRoute)
                currentRoute.recaptchaCallback(response)
        }

        private val randomEmotes = listOf(
                "/assets/img/daily/here_comes_the_money.gif",
                "/assets/img/daily/lori_rica.png",
                "/assets/img/daily/lori_woop.gif",
                "/assets/img/daily/lori_ehissoai.gif",
                "/assets/img/daily/lori_confetti.gif",
                "/assets/img/daily/lori_yay_wobbly.gif",
                "/assets/img/daily/ferret.gif"
        )
    }

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.launch {
            val response = http.get<HttpResponse> {
                url("${window.location.origin}/api/v1/economy/daily-reward-status")
            }

            val dailyRewardStatusAsString = response.receive<String>()

            debug("Daily Reward Status: $dailyRewardStatusAsString")

            val data = JSON.parse<Json>(dailyRewardStatusAsString)

            if (checkIfThereAreErrors(response, data))
                return@launch

            val receivedDailyWithSameIp = data["receivedDailyWithSameIp"] as Int

            dailyNotification.textContent = if (receivedDailyWithSameIp == 0) {
                locale["website.daily.pleaseCompleteReCaptcha"]
            } else {
                locale["website.daily.alreadyReceivedPrizesWithTheSameIp"]
            }

            GoogleRecaptchaUtils.render(jq("#daily-captcha").get()[0], RecaptchaOptions(
                    "6LfRyUkUAAAAAASo0YM4IZBqvkzxyRWJ1Ydw5weC",
                    "recaptchaCallback",
                    "normal"
            ))
            m.hideLoadingScreen()
        }

        // Atualizar sem travar o status
        updateLeaderboard()
    }

    fun checkIfThereAreErrors(response: HttpResponse, data: Json): Boolean {
        if (response.status != HttpStatusCode.OK) {
            // oof, parece ser um erro!
            val error = data["error"] as Json?

            if (error == null) {
                dailyNotification.textContent = locale["website.daily.thisShouldNeverHappen", response.status.value]
                return true
            }

            val code = error["code"] as Int
            val webCode = LoriWebCode.fromErrorId(code)

            dailyNotification.textContent = when (webCode) {
                LoriWebCode.UNAUTHORIZED -> {
                    dailyRewardButton.addClass("button-discord-success")
                    dailyRewardButton.removeClass("button-discord-disabled")
                    dailyRewardButton.onClick {
                        val json = json()
                        json["redirectUrl"] = "${loriUrl}daily"
                        window.location.href = "https://discordapp.com/oauth2/authorize?redirect_uri=${loriUrl}dashboard&scope=identify%20guilds%20email%20guilds.join&response_type=code&client_id=297153970613387264&state=${window.btoa(JSON.stringify(json))}"
                    }

                    locale["website.daily.notLoggedIn"]
                }
                LoriWebCode.ALREADY_GOT_THE_DAILY_REWARD_SAME_ACCOUNT_TODAY -> {
                    val moment = Moment(data["canPayoutAgain"].unsafeCast<Long>())
                    locale["website.daily.alreadyReceivedDailyReward", moment.fromNow()]
                }
                LoriWebCode.ALREADY_GOT_THE_DAILY_REWARD_SAME_IP_TODAY -> {
                    val moment = Moment(data["canPayoutAgain"].unsafeCast<Long>())
                    locale["website.daily.alreadyReceivedDailyReward", moment.fromNow()]
                }
                LoriWebCode.BLACKLISTED_EMAIL -> locale["website.daily.blacklistedEmail"]
                LoriWebCode.BLACKLISTED_IP -> locale["website.daily.blacklistedIp"]
                LoriWebCode.UNVERIFIED_ACCOUNT -> locale["website.daily.unverifiedAccount"]
                LoriWebCode.INVALID_RECAPTCHA -> locale["website.daily.invalidReCaptcha"]
                LoriWebCode.MFA_DISABLED -> locale["website.daily.pleaseActivate2FA"]
                else -> locale["website.daily.thisShouldNeverHappen", webCode.name]
            }
            return true
        } else return false
    }

    @ImplicitReflectionSerializer
    fun updateLeaderboard() {
        // Iremos pegar o leaderboard em uma task separada, já que o endpoint é diferente :)
        m.launch {
            val leaderboardElement = document.select<HTMLElement>("#leaderboard")
            val response = http.get<HttpResponse> {
                url("${window.location.origin}/api/v1/economy/sonhos-leaderboard/around")
                parameter("padding", "2")
            }

            val payload = kotlinx.serialization.json.JSON.nonstrict.parse<LeaderboardResponse>(response.receive())

            val rankPosition = payload.rankPosition
            val usersAround = payload.usersAround

            leaderboardElement.clear()

            if (rankPosition != null) {
                if (usersAround != null) {
                    document.select<HTMLElement>("#leaderboard").append {
                        table("fancy-table") {
                            style = "width: 100%;"
                            tr {
                                th {
                                    + locale["website.daily.leaderboard.position"]
                                }
                                th {

                                }
                                th {
                                    + locale["website.daily.leaderboard.name"]
                                }
                                th {
                                    + "Sonhos"
                                }
                            }

                            for ((index, user) in usersAround.withIndex()) {
                                tr {
                                    // Não vamos colocar opacidade se o usuário estiver bem perto do topo
                                    if (rankPosition >= 2) {
                                        if (index == 0) {
                                            style = "opacity: 0.35;"
                                        }
                                        if (index == 1) {
                                            style = "opacity: 0.7;"
                                        }
                                    }
                                    if (index == (USER_PADDING + USER_PADDING + 1) - 2) {
                                        style = "opacity: 0.7;"
                                    }
                                    if (index == (USER_PADDING + USER_PADDING + 1) - 1) {
                                        style = "opacity: 0.35;"
                                    }

                                    td {
                                        +"#${user.position}"
                                    }
                                    td {
                                        img(src = user.avatarUrl) {
                                            style = "border-radius: 100%; width: 2em;"
                                        }
                                    }
                                    td {
                                        if (user.id.toString() == m.userIdentification?.id) {
                                            classes += "rainbow-animated-text"
                                        }
                                        + user.name
                                        span {
                                            style = "opacity: 0.5;"
                                            + "#${user.discriminator}"
                                        }
                                    }
                                    td {
                                        +"${user.money}"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ImplicitReflectionSerializer
    @JsName("recaptchaCallback")
    fun recaptchaCallback(response: String) {
        val ts1Promotion2 = Audio("${loriUrl}assets/snd/ts1_promotion2.mp3")
        val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")
        dailyNotification.clear()

        debug("reCAPTCHA Token: $response")

        dailyRewardButton.addClass("button-discord-success")
        dailyRewardButton.removeClass("button-discord-disabled")
        dailyRewardButton.onClick {
            dailyRewardButton.addClass("button-discord-disabled")
            dailyRewardButton.removeClass("button-discord-success")

            m.launch {
                val response = http.get<HttpResponse> {
                    url("${window.location.origin}/api/v1/economy/daily-reward")
                    parameter("recaptcha", response)
                }

                val dailyRewardStatusAsString = response.receive<String>()

                debug("Daily Reward: $dailyRewardStatusAsString")

                val data = JSON.parse<Json>(dailyRewardStatusAsString)

                if (checkIfThereAreErrors(response, data))
                    return@launch

                val payload = kotlinx.serialization.json.JSON.nonstrict.parse<DailyResponse>(JSON.stringify(data))

                jq("#daily-wrapper").fadeTo(500, 0, {
                    dailyWrapper.asDynamic().style.position = "absolute"

                    dailyPrewrapper.prepend {
                        div {
                            id = "daily-info"
                            style = "opacity: 0;"

                            h2 {
                                + locale["website.daily.congratulations"]
                            }

                            h1 {
                                + "0"
                                id = "dailyPayout"
                            }

                            h2 {
                                + "Sonhos!"
                            }

                            p {
                                locale.buildAsHtml(locale["website.daily.wantMoreSonhos"], { control ->
                                    if (control == 0) {
                                        a(href = "/user/@me/dashboard/bundles") {
                                            + locale["website.daily.clickingHere"]
                                        }
                                    }
                                }, { str ->
                                    + str
                                })
                            }

                            if (payload.sponsoredBy != null) {
                                h1 {
                                    + locale["website.daily.youEarnedMoreSonhos", payload.sponsoredBy.multipliedBy]
                                }
                                img(src = payload.sponsoredBy.guild.iconUrl) {
                                    attributes["width"] = "128"
                                    attributes["height"] = "128"
                                    attributes["style"] = "border-radius: 99999px;"
                                }
                                h2 {
                                    +payload.sponsoredBy.guild.name
                                }
                                p {
                                    + locale["website.daily.sponsoredStatus", payload.sponsoredBy.originalPayout, payload.sponsoredBy.guild.name, payload.dailyPayout]
                                }
                                if (payload.sponsoredBy.user != null) {
                                    p {
                                        + locale["website.daily.thankTheSponsor", "${payload.sponsoredBy.user.name}#${payload.sponsoredBy.user.discriminator}", payload.dailyPayout - payload.sponsoredBy.originalPayout]
                                    }
                                }
                            }

                            p {
                                +"Agora você possui ${payload.currentBalance} sonhos, que tal gastar os seus sonhos "
                                val random = Random(Date().getTime().toInt()).nextInt(0, 4)
                                when (random) {
                                    0 -> {
                                        +"na rifa (+rifa)"
                                    }
                                    1 -> {
                                        a(href = "${loriUrl}user/@me/dashboard/ship-effects") {
                                            +"alterando o valor do ship entre você e a sua namoradx"
                                        }
                                    }
                                    2 -> {
                                        a(href = "${loriUrl}user/@me/dashboard/profiles") {
                                            +"alterando o look do seu perfil"
                                        }
                                    }
                                    3 -> {
                                        +"doando eles para a sua pessoa favorita (+pagar)"
                                    }
                                }

                                +"?"
                            }

                            if (payload.failedGuilds.isNotEmpty()) {
                                for (failedGuild in payload.failedGuilds) {
                                    p {
                                        +"Você poderia ganhar x${failedGuild.multiplier} sonhos "

                                        when (failedGuild.type) {
                                            DailyGuildMissingRequirement.REQUIRES_MORE_TIME -> {
                                                +"após ficar por mais de 15 dias em "
                                            }
                                            DailyGuildMissingRequirement.REQUIRES_MORE_XP -> {
                                                +"sendo mais ativo em "
                                            }
                                        }

                                        +failedGuild.guild.name
                                        +"!"
                                    }
                                }
                            }

                            img {
                                width = "128"
                                height = "128"

                                src = randomEmotes.random()
                            }

                            p {
                                + locale["website.daily.comeBackLater"]
                            }
                        }
                    }

                    jq("#daily-wrapper").css("position", "absolute")
                    val prepended = jq("#daily-info")

                    prepended.fadeTo(500, 1)

                    val countUp = CountUp("dailyPayout", 0.0, payload.dailyPayout.toDouble(), 0, 7.5, CountUpOptions(
                            true,
                            true,
                            "",
                            ""
                    ))

                    ts1Promotion2.play()
                    updateLeaderboard()
                    countUp.start {
                        println("Finished!!!")
                        cash.play()
                    }
                })
            }
        }
    }

    @Serializable
    class DailyResponse(
            val receivedDailyAt: String,
            val dailyPayout: Int,
            val sponsoredBy: Sponsored? = null,
            val currentBalance: Double,
            val failedGuilds: Array<FailedGuildDailyStats>
    )

    @Serializable
    class Guild(
            // É deserializado para String pois JavaScript é burro e não funciona direito com Longs
            val name: String,
            val iconUrl: String,
            val id: String
    )

    @Serializable
    class Sponsored(
            val guild: Guild,
            val user: ServerConfig.SelfMember? = null,
            val multipliedBy: Double,
            val originalPayout: Double
    )

    @Serializable
    class FailedGuildDailyStats(
            val guild: Guild,
            val type: DailyGuildMissingRequirement,
            val data: Long,
            val multiplier: Double
    )

    @Serializable
    class LeaderboardResponse(
            val rankPosition: Int? = null,
            val usersAround: List<UserAround>? = null
    )

    @Serializable
    class UserAround(
            val id: Long,
            val money: Long,
            val position: Long,
            val name: String,
            val discriminator: String,
            val avatarUrl: String
    )
}