package net.perfectdreams.spicymorenitta.views

import LoriDashboard
import jQuery
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.parse
import loriUrl
import net.perfectdreams.loritta.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.spicymorenitta.utils.GoogleRecaptchaUtils
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import utils.*
import kotlin.browser.window
import kotlin.collections.set
import kotlin.js.*
import kotlin.random.Random

@UseExperimental(ImplicitReflectionSerializer::class)
object DailyView {
	fun start() {
		LoriDashboard.showLoadingBar("Carregando...")

		jQuery.get("${loriUrl}api/v1/economy/daily-reward-status", { data, b, c ->
			println("Status data: ${JSON.stringify(data)}")

			val status = data.unsafeCast<Json>()
			val code = status.get("api:code").unsafeCast<Int>()

			if (code == 0) {
				jq(".daily-notification").text("Por favor, complete o reCAPTCHA")
				GoogleRecaptchaUtils.render(jq("#daily-captcha").get()[0], RecaptchaOptions(
						"6LfRyUkUAAAAAASo0YM4IZBqvkzxyRWJ1Ydw5weC",
						"recaptchaCallback",
						"normal"
				))
			} else if (code == 4) {
				jq(".daily-reward-button")
						.addClass("button-discord-success")
						.removeClass("button-discord-disabled").click {

							val json = json()
							json.set("redirectUrl", "${loriUrl}daily")
							window.location.href = "https://discordapp.com/oauth2/authorize?redirect_uri=${loriUrl}dashboard&scope=identify%20guilds%20email%20guilds.join&response_type=code&client_id=297153970613387264&state=${window.btoa(JSON.stringify(json))}"
						}
			} else {
				Moment.locale("pt-br")
				val error = when (code) {
					4 -> "Você precisa entrar na sua conta do Discord antes de ganhar seu prêmio!"
					5 -> {
						val moment = Moment(status["canPayoutAgain"].unsafeCast<Long>())
						"Você já recebeu seu prêmio hoje! Você poderá votar novamente {0}!".replace("{0}", moment.fromNow())
					}
					7 -> "reCAPTCHA inválido!"
					9 -> "IP bloqueado!"
					else -> "Error: $code"
				}
				jq(".daily-notification").text(error)
			}
			LoriDashboard.hideLoadingBar()
		})
	}

	@JsName("recaptchaCallback")
	fun recaptchaCallback(response: String) {
		val ts1Promotion2 = Audio("${loriUrl}assets/snd/ts1_promotion2.mp3")
		val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")
		jq(".daily-notification").text("")

		println("owo recaptcha")

		jq(".daily-reward-button")
				.addClass("button-discord-success")
				.removeClass("button-discord-disabled")
				.click {
					if (jq(".daily-reward-button").hasClass("button-discord-disabled")) {
						return@click
					}

					jq(".daily-reward-button").addClass("button-discord-disabled")

					jQuery.get("${loriUrl}api/v1/economy/daily-reward?recaptcha=$response", { data, b, c ->
						println("Daily Reward: " + JSON.stringify(data))

						val json = data.unsafeCast<Json>()
						val apiCode = json.get("api:code").unsafeCast<Int>()

						// 0 = sucesso
						// 4 = não autorizado
						// 5 = já fez as coisinhas de daily hoje
						// 7 = invalid captcha response
						// 9 = IP bloqueado
						if (apiCode == 0) { // Sucesso!
							val payload = kotlinx.serialization.json.JSON.nonstrict.parse<DailyResponse>(JSON.stringify(data))

							jq("#daily-wrapper").fadeTo(500, 0, {
								jq("#daily-wrapper").css("position", "absolute")
								val prepended = jq("#daily-prewrapper").prepend(
										jq("<div>")
								)
								prepended.css("opacity", 0)

								prepended.append(
										jq("<h2>")
												.text("Parabéns! Hoje você ganhou...")
								)

								prepended.append(jq("<h1>")
										.text("0")
										.attr("id", "dailyPayout")
								)


								prepended.append(
										jq("<h2>")
												.text("Sonhos!")
								)

								if (payload.sponsoredBy != null) {
									prepended.get()[0].append {
										h1 {
											+ "Você ganhou x${payload.sponsoredBy.multipliedBy} mais sonhos, graças ao..."
										}
										img(src = payload.sponsoredBy.guild.iconUrl) {
											attributes["width"] = "128"
											attributes["height"] = "128"
											attributes["style"] = "border-radius: 99999px;"
										}
										h2 {
											+ payload.sponsoredBy.guild.name
										}
										p {
											+ "Você iria ganhar ${payload.sponsoredBy.originalPayout} sonhos, mas graças ao patrocínio do ${payload.sponsoredBy.guild.name} você ganhou ${payload.dailyPayout}!"
										}
										if (payload.sponsoredBy.user != null) {
											p {
												+"Agradeça ${payload.sponsoredBy.user.name}#${payload.sponsoredBy.user.discriminator} por ter feito você ganhar ${payload.dailyPayout - payload.sponsoredBy.originalPayout} mais sonhos que o normal!"
											}
										}
									}
								}

								prepended.get()[0].append {
									p {
										+ "Agora você possui ${payload.currentBalance} sonhos, que tal gastar os seus sonhos "
										val random = Random(Date().getTime().toInt()).nextInt(0, 4)
										when (random) {
											0 -> {
												+ "na rifa (+rifa)"
											}
											1 -> {
												a(href = "${loriUrl}user/@me/dashboard/ship-effects") {
													+ "alterando o valor do ship entre você e a sua namoradx"
												}
											}
											2 -> {
												a(href = "${loriUrl}user/@me/dashboard/profiles") {
													+ "alterando o look do seu perfil"
												}
											}
											3 -> {
												+ "doando eles para a sua pessoa favorita (+pagar)"
											}
										}

										+ "?"
									}

									if (payload.failedGuilds.isNotEmpty()) {
										for (failedGuild in payload.failedGuilds) {
											p {
												+ "Você poderia ganhar x${failedGuild.multiplier} sonhos "

												when (failedGuild.type) {
													DailyGuildMissingRequirement.REQUIRES_MORE_TIME -> {
														+ "após ficar por mais de 15 dias em"
													}
													DailyGuildMissingRequirement.REQUIRES_MORE_XP -> {
														+ "sendo mais ativo em"
													}
												}

												+ failedGuild.guild.name
												+ "!"
											}
										}
									}
								}

								prepended.append(jq("<img>")
										.attr("width", 64)
										.attr("height", 64)
										.attr("src", "https://cdn.discordapp.com/emojis/399743288673959947.gif?v=1")
								)

								prepended.append(jq("<p>")
										.text("Volte sempre!"))
								prepended.fadeTo(500, 1)

								val countUp = CountUp("dailyPayout", 0.0, payload.dailyPayout.toDouble(), 0, 7.5, CountUpOptions(
										true,
										true,
										"",
										""
								))

								ts1Promotion2.play()
								countUp.start {
									println("Finished!!!")
									cash.play()
								}
							})
						} else {
							val error = when (apiCode) {
								4 -> "Você precisa entrar na sua conta do Discord antes de ganhar seu prêmio!"
								5 -> "Você já recebeu seu prêmio hoje!"
								7 -> "reCAPTCHA inválido!"
								9 -> "IP bloqueado!"
								else -> "Error: $apiCode"
							}

							jq(".daily-notification").text(error)
						}
					})
				}
	}

	@Serializable
	class DailyResponse(
			val receivedDailyAt: String,
			val dailyPayout: Int,
			@Optional val sponsoredBy: Sponsored? = null,
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
			@Optional val user: ServerConfig.SelfMember? = null,
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
}