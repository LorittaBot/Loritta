
import org.w3c.files.FileReader
import utils.LorittaPartner
import kotlin.browser.document

object ConfigurePartnerView {
	val vanityUrlInput: JQuery by lazy {
		jq("#vanityUrl")
	}

	val vanityUrlExample: JQuery by lazy {
		jq("#vanityUrlExample")
	}

	val addKeyword: JQuery by lazy {
		jq("#addKeyword")
	}

	val keywords: JQuery by lazy {
		jq("#keywords")
	}

	val keywordList: JQuery by lazy {
		jq("#keywordList")
	}

	val uploadBackground: JQuery by lazy {
		jq("#uploadBackground")
	}

	var isPartner = false

	fun start() {
		document.addEventListener("DOMContentLoaded", {
			val serverConfig = LoriDashboard.loadServerConfig()
			val serverListConfig = serverConfig.serverListConfig

			// Nós precisamos colocar isso no save data porque...? Não sei
			// O servidor irá dar erro caso o usuário tente enviar isPartner = true mas o servidor usa isPartner = false
			// O servidor também irá ignorar qualquer conteúdo que não partners não possuem (como vanityUrl)
			this.isPartner = serverListConfig.isPartner

			println("Keywords: ${serverListConfig.keywords.joinToString(", ")}")

			for (keyword in LorittaPartner.Keyword.values()) {
				println("Adding keyword $keyword...")
				keywords.append(
						jq("<div>")
								.addClass("pure-u-1")
								.addClass("pure-u-md-1-3")
								.append(
										jq("<label>").append(
												jq("<input>").attr("type", "checkbox")
														.attr("name", keyword.toString())
														.attr("value", "keywords")
														.attr("data-keyword-checkbox", "true")
														.prop("checked", serverListConfig.keywords.firstOrNull { it.toString() == keyword.toString() } != null)

										).append(
												jq("<span>")
														.text(" " + legacyLocale["KEYWORD_" + keyword.toString()])
														.addClass("keyword")
														.attr("style", "margin-left: 6px;${if (keyword == LorittaPartner.Keyword.NSFW) {
															"background-color: rgb(163, 48, 48);"
														} else {
															""
														}}")
										)
								)
				)
			}

			vanityUrlInput.on("input") { _, _ ->
				var vanityUrl = vanityUrlInput.`val`().unsafeCast<String>()
				vanityUrl = vanityUrl.replace(" ", "-")
				vanityUrl = vanityUrl.toLowerCase()
				vanityUrlInput.`val`(vanityUrl)
				changeVanityUrlText()
			}
			changeVanityUrlText()

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1")
			LoriDashboard.applyBlur("#hiddenIfDisabled3", "#cmn-toggle-2")
			LoriDashboard.applyBlur("#hiddenIfDisabled4", "#cmn-toggle-3")

			LoriDashboard.configureTextArea(jq("#voteBroadcastMessage"), true, serverConfig, true, jq("#chooseChannel"))
			LoriDashboard.configureTextArea(jq("#promoteBroadcastMessage"), true, serverConfig, true, jq("#chooseChannel2"))

			for (textChannel in serverConfig.textChannels) {
				val option = jq("<option>")
						.attr("value", textChannel.id)
						.text("#${textChannel.name}")

				if (!textChannel.canTalk) {
					option.attr("disabled", "disabled")
				}

				val voteSel = option.clone()
				val promoteSel = option.clone()

				if (serverConfig.serverListConfig.voteBroadcastChannelId == textChannel.id) {
					voteSel.attr("selected", "selected")
				}
				if (serverConfig.serverListConfig.asDynamic().promoteBroadcastChannelId == textChannel.id) {
					promoteSel.attr("selected", "selected")
				}

				jq("#chooseChannel").append(voteSel.clone())
				jq("#chooseChannel2").append(promoteSel.clone())
			}

			if (!serverListConfig.isPartner && !serverListConfig.isSponsored) {
				jq("#hiddenIfDisabled2").addClass("blurSection")
				jq("#hiddenInfo2").text("Funções exclusivas para partners e patrocinadores")
			}
		})
	}

	fun changeVanityUrlText() {
		val vanityUrl = vanityUrlInput.`val`()
		vanityUrlExample.text("${loriUrl}s/$vanityUrl")
		vanityUrlExample.attr("src", "${loriUrl}s/$vanityUrl")
	}

	@JsName("prepareSave")
	fun prepareSave() {
		println("Preparing save... wow!")

		val file = uploadBackground.get()[0].asDynamic().files[0]

		if (file != null) {
			val reader = FileReader()

			reader.readAsDataURL(file)
			reader.onload = {
				val imageAsBase64 = reader.result
				save(imageAsBase64)
			}
		} else {
			save(null)
		}
	}

	fun save(imageAsBase64: String?) {
		SaveStuff.prepareSave("server_list", { payload ->
			val addedKeywords = keywords.find("[data-keyword-checkbox]").toArray()
					.filter {
						it.asDynamic().checked
					}
					.map {
						it.getAttribute("name")
					}

			payload.set("keywords", addedKeywords)

			if (imageAsBase64 != null) {
				payload.set("backgroundImage", imageAsBase64)
			}

			payload.set("isPartner", isPartner)
		})
	}
}