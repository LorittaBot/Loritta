import utils.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

// external val guildId: String

object PartnerView {
	val visibleModal get() = jq(".tingle-modal--visible")
	var activeInformation: LorittaServerSample? = null

	fun start() {
		println("Starting partner view... ^-^ Pulling partner information from Loritta owo")

		val selfProfileDiv = document.getElementById("self-profile-json")!!.innerHTML
		println(selfProfileDiv)
		val serverInformationDiv = document.getElementById("server-information-json")!!.innerHTML
		println(serverInformationDiv)

		val data = JSON.parse<Json>(selfProfileDiv)

		if (data.toJson().get("api:code") != LoriWebCodes.UNAUTHORIZED)
			selfProfile = data.toJson<LorittaProfile>()

		val json = JSON.parse<Json>(serverInformationDiv)
		if (json.get("api:code") == 3) {
			window.location.href = "${loriUrl}servers"
		} else {
			openServerModal(json.toJson<LorittaServerSample>(), true, legacyLocale)
		}
	}

	fun openServerModal(information: LorittaServerSample, direct: Boolean, locale: LegacyBaseLocale) {
		println("Opening server modal... ${JSON.stringify(information)}")
		println("Is direct? $direct")
		println("---")
		println("Can vote? ${information.canVote}")
		println("Can't vote reason: ${information.cantVoteReason}")
		println("Can vote in... ${information.canVoteNext}")
		println("---")

		val ts1Promotion = Audio("${loriUrl}assets/snd/ts1_promotion.mp3")

		Moment.locale("pt-br")

		activeInformation = information
		val converter = ShowdownConverter()

		val template = jq("#guild-template").clone()
		template.find(".guild-name").text(information.name)

		var description = information.serverListConfig.description ?: ""
		information.serverEmotes.forEach {
			description = description.replace(":${it.name}:", "<img class=\"discord-emote\" src=\"${it.imageUrl}\">")
		}

		description = converter.makeHtml(description)
		template.find(".description").html(description)
		template.find(".icon").attr("src", (information.iconUrl ?: "${loriUrl}assets/img/unknown.png") + "?size=1024")
		template.find(".member-count").text(information.memberCount)
		template.find(".online-count").text(information.onlineCount)
		template.find(".guild-owner").text(information.ownerName + "#" + information.ownerDiscriminator)
		if (direct) {
			if (information.hasCustomBackground)
				jq("html")
						.css("background", "url(\"${loriUrl}assets/img/servers/backgrounds/${information.id}.png?v=${information.backgroundKey}\") no-repeat center center fixed")
			else
				jq("html").attr("style", "background-size: initial !important;")
		}

		information.serverEmotes.forEach {
			template.find(".emotes").append(
					jq("<img>")
							.attr("src", it.imageUrl)
							.attr("alt", it.name)
							.attr("title", ":" + it.name + ":")
							.css("height", "24px")
							.css("width", "auto")
			)
		}

		information.serverListConfig.keywords.forEach {
			if (it != null) {
				template.find(".keywords").append(jq("<span>").addClass("keyword").text(locale["KEYWORD_" + it.toString()]))
			}
		}

		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		if (!information.joinedServer) {
			modal.addFooterBtn("<i class=\"fab fa-discord\"></i> Entrar", "button-discord button-discord-info pure-button button-discord-modal") {
				println("Adding user to guild...")
				jQuery.get("${loriUrl}api/v1/server-list/join/?guildId=${information.id}", { data, b, c ->
					println(data.stringify())

					val payload = data.toJson()

					val apiCode = payload.get("api:code").unsafeCast<Int>()

					when (apiCode) {
						LoriWebCodes.SUCCESS -> {
							println("Success!")
						}
						LoriWebCodes.UNKNOWN_GUILD -> {
							println("wat")
						}
						LoriWebCodes.UNAUTHORIZED -> {
							val json = json()
							json.set("redirectUrl", "${loriUrl}s/${information.id}?force")
							window.location.href = "https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email%20guilds.join&response_type=code&client_id=297153970613387264&state=${window.btoa(json.stringify())}"
						}
					}
				})
				modal.close()
			}
		} else {
			modal.addFooterBtn("<i class=\"fab fa-discord\"></i> Você já está no Servidor!", "button-discord button-discord-disabled pure-button button-discord-modal") {

			}
		}

		/* modal.addFooterBtn("<i class=\"fas fa-external-link-alt\"></i> Website", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			println("CLICKED2!!!")
			modal.close()
		} */

		if (!direct) {
			modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
				println("CLICKED3!!!")
				modal.close()
			}
		}

		modal.setContent(template.html())

		modal.open()

		val visible = jq(".tingle-modal--visible")

		val redirectUrl = "${loriUrl}s/${information.id}"
		val json = json()
		json.set("redirectUrl", redirectUrl)

		if (information.canVote) {
			visible.find(".upvote-notification").text(locale["UPVOTE_PleaseCompleteCaptcha"])
		} else {
			val code = information.cantVoteReason!!
			val reason = when (code) {
				1 -> locale["UPVOTE_LogInDiscord"]
				2 -> locale["UPVOTE_NeedsToBeMember"]
				3 -> locale["UPVOTE_JoinedAtLeastOneHour"]
				4 -> {
					val moment = Moment(information.canVoteNext!!)
					locale["UPVOTE_CanVoteAgain", moment.fromNow()]
				}
				else -> "Error: $code"
			}

			println("Error code: $code")
			if (code != 1) {
				visible.find(".upvote-notification").text(reason)
				visible.find(".server-upvote-button")
						.addClass("button-discord-disabled")
						.removeClass("button-discord-success")
			} else {
				visible.find(".server-upvote-button")
						.addClass("button-discord-success")
						.removeClass("button-discord-disabled")
				visible.find(".server-upvote-button")
						.click {
							window.location.href = "https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email%20guilds.join&response_type=code&client_id=297153970613387264&state=${window.btoa(JSON.stringify(json))}"
						}
			}
		}

		// SISTEMA DE PROMOVER SERVIDORES
		val hypeButton = visible.find(".server-hype-button")
		val currentTimeMillis = Date().getTime()

		// Para reaproveitar coisas, nós iremos reaprovitar algumas razões de "não poder votar"
		val code = information.cantVoteReason

		val promoteError = when (code) {
			1 -> locale["UPVOTE_LogInDiscord"]
			2 -> locale["UPVOTE_NeedsToBeMember"]
			3 -> locale["UPVOTE_JoinedAtLeastOneHour"]
			else -> null
		}

		if (promoteError != null) {
			visible.find(".promote-notification").text(promoteError)
		} else {
			if (currentTimeMillis >= information.lastBump.toString().toLong() + 14_400_000) {
				println("Yes, can hype!")
				hypeButton.addClass("button-discord-success")
						.removeClass("button-discord-disabled")

				val profile = selfProfile

				if (profile == null) {
					hypeButton.click {
						window.location.href = "https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email%20guilds.join&response_type=code&client_id=297153970613387264&state=${window.btoa(JSON.stringify(json))}"
					}
				} else {
					if (profile.dreams >= 750) {
						hypeButton.click {
							hypeButton.removeClass("button-discord-success")
									.addClass("button-discord-disabled")

							jQuery.post("${loriUrl}api/v1/server-list/bump?guildId=${information.id}", { data, b, c ->
								println(data.stringify())

								val vote = data.toJson()

								val codeResponse = vote.get("api:code").unsafeCast<Int>()

								if (codeResponse == 0) { // SUCCESS
									hypeButton.addClass("button-discord-disabled")
											.removeClass("button-discord-success")
									visibleModal.find(".promote-notification").text("Obrigado por promover! ^-^")
									ts1Promotion.play()
								} else {
									val error = when (codeResponse) {
										LoriWebCodes.UNAUTHORIZED -> locale["UPVOTE_LogInDiscord"]
										LoriWebCodes.UNKNOWN_GUILD -> locale["UPVOTE_UnknownGuild"]
										LoriWebCodes.NOT_IN_GUILD -> locale["UPVOTE_NeedsToBeMember"]
										LoriWebCodes.INVALID_CAPTCHA_RESPONSE -> locale["UPVOTE_InvalidCaptchaResponse"]
										LoriWebCodes.ALREADY_VOTED_TODAY -> {
											val moment = Moment(information.canVoteNext!!)
											locale["PROMOTE_CanPromoteAgain", moment.fromNow()]
										}
										LoriWebCodes.NOT_VERIFIED -> locale["UPVOTE_NotVerified"]
										LoriWebCodes.BAD_EMAIL -> locale["UPVOTE_BadEmail"]
										LoriWebCodes.BAD_IP -> locale["UPVOTE_BadIp"]
										LoriWebCodes.INSUFFICIENT_FUNDS -> "Sonhos insuficientes!"
										else -> "Error: $codeResponse"
									}

									hypeButton.addClass("button-discord-disabled")
											.removeClass("button-discord-success")
									visibleModal.find(".promote-notification").text(error)
								}
							})
						}
					} else {
						hypeButton.addClass("button-discord-disabled")
								.removeClass("button-discord-success")

						visible.find(".promote-notification").html("Você não possui Sonhos suficientes para promover! Que tal ganhar alguns sonhos <a href=\"${loriUrl}daily\">clicando aqui</a>?")
					}
				}
			} else {
				println("Nah, can't hype!")

				val canBumpAgain = information.lastBump
				val moment = Moment(canBumpAgain)
				moment.add(4, "h")

				visible.find(".promote-notification").text(locale["PROMOTE_CanPromoteAgain", moment.fromNow()])
			}
		}

		jq(".tingle-modal--visible").addClass("tingle-modal--overflow")

		if (information.canVote) {
			GoogleRecaptcha.render(visible.find(".g-recaptcha").get()[0], RecaptchaOptions(
					"6LfRyUkUAAAAAASo0YM4IZBqvkzxyRWJ1Ydw5weC",
					"recaptchaCallback",
					"compact"
			))
		}

		LoriServerList.injectAdvertisements(visibleModal)
		LoriServerList.injectAdvertisements(visibleModal)
	}

	@JsName("recaptchaCallback")
	fun recaptchaCallback(response: String) {
		val ts1SkillUp = Audio("${loriUrl}assets/snd/ts1_skill.mp3")

		println("reCAPTCHA completed! Activating upvote button...")
		val information = activeInformation
		if (information == null) {
			println("reCAPTCHA complete, but no active information... bug?")
			return
		}
		visibleModal.find(".upvote-notification").text("")
		visibleModal.find(".server-upvote-button")
				.addClass("button-discord-success")
				.removeClass("button-discord-disabled")
				.click {
					if (visibleModal.find(".server-upvote-button").hasClass("button-discord-disabled"))
						return@click

					visibleModal.find(".server-upvote-button")
							.removeClass("button-discord-success")
							.addClass("button-discord-disabled")

					jQuery.post("${loriUrl}api/v1/server-list/vote?guildId=${information.id}&recaptcha=$response", { data, b, c ->
						println(data.stringify())

						val vote = data.toJson()

						val codeResponse = vote.get("api:code").unsafeCast<Int>()


						if (codeResponse == 0) { // SUCCESS
							jq(".tingle-modal--visible")
									.find(".server-upvote-button")
									.addClass("button-discord-disabled")
									.removeClass("button-discord-success")
							visibleModal.find(".upvote-notification").text("Obrigado por votar! ^-^")
							ts1SkillUp.play()
						} else {
							js("grecaptcha.reset()")
							val error = when (codeResponse) {
								LoriWebCodes.UNAUTHORIZED -> legacyLocale["UPVOTE_LogInDiscord"]
								LoriWebCodes.UNKNOWN_GUILD -> legacyLocale["UPVOTE_UnknownGuild"]
								LoriWebCodes.NOT_IN_GUILD -> legacyLocale["UPVOTE_NeedsToBeMember"]
								LoriWebCodes.INVALID_CAPTCHA_RESPONSE -> legacyLocale["UPVOTE_InvalidCaptchaResponse"]
								LoriWebCodes.ALREADY_VOTED_TODAY -> {
									val moment = Moment(information.canVoteNext!!)
									legacyLocale["UPVOTE_CanVoteAgain", moment.fromNow()]
								}
								LoriWebCodes.NOT_VERIFIED -> legacyLocale["UPVOTE_NotVerified"]
								LoriWebCodes.BAD_EMAIL -> legacyLocale["UPVOTE_BadEmail"]
								LoriWebCodes.BAD_IP -> legacyLocale["UPVOTE_BadIp"]
								else -> "Error: $codeResponse"
							}

							jq(".tingle-modal--visible")
									.find(".server-upvote-button")
									.addClass("button-discord-disabled")
									.removeClass("button-discord-success")
							visibleModal.find(".upvote-notification").text(error)
						}
					})
				}

	}

	class PartnerInformation(
			val id: String,
			val iconUrl: String?,
			val invite: String?,
			val name: String,
			val tagline: String,
			val description: String,
			val keywords: Array<LorittaPartner.Keyword>,
			val ownerId: String,
			val ownerName: String,
			val ownerDiscriminator: String,
			val ownerAvatarUrl: String,
			val memberCount: Int,
			val onlineCount: Int,
			val serverEmotes: Array<Emote>,
			val canVote: Boolean,
			val cantVoteReason: Int?,
			val canVoteNext: Long?,
			val joinedServer: Boolean
	)

	class Emote(
			val name: String,
			val imageUrl: String
	)
}