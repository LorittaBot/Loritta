
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement
import utils.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.*

external val guildId: String

external fun delete(p: dynamic): Boolean = definedExternally

object LoriDashboard {
	var toggleCounter = 1000

	val configSavedSfx: Audio by lazy { Audio("${loriUrl}assets/snd/config_saved.mp3") }
	val configErrorSfx: Audio by lazy { Audio("${loriUrl}assets/snd/config_error.mp3") }

	val wrapper: JQuery by lazy {
		jq("#server-configuration")
	}

	val leftSidebar: JQuery by lazy {
		jq("#left-sidebar")
	}

	val rightSidebar: JQuery by lazy {
		jq("#right-sidebar")
	}

	val loadingScreen: JQuery by lazy {
		jq("#loading-screen")
	}

	fun loadServerConfig(): ServerConfig {
		println("Loading config from embedded data... (if available)")

		val serverConfigJson = document.getElementById("server-config-json")?.innerHTML

		println("Config (as JSON): ${serverConfigJson}")

		if (serverConfigJson != null) {
			println("Parsing the configuration...")
			val serverConfig = JSON.parse<ServerConfig>(serverConfigJson)
			println("Server's Command Prefix: ${serverConfig.commandPrefix}")
			return serverConfig
		} else {
			println("Couldn't find embedded config data in body!")
			throw RuntimeException("Couldn't find embedded config data in body!")
		}
	}

	fun showLoadingBar(text: String? = "Salvando...") {
		document.select<HTMLDivElement>("#loading-screen").apply {
			select<HTMLDivElement>(".loading-text").apply {
				textContent = text
			}
			style.opacity = "1"
		}
	}

	fun hideLoadingBar() {
		document.select<HTMLDivElement>("#loading-screen").apply {
			style.opacity = "0"
		}
	}

	fun applyBlur(toBeHidden: String, toggle: String, onToggle: (() -> (Boolean))? = null) {
		jq(toggle).click {
			val result = onToggle?.invoke() ?: true

			if (!result) {
				it.preventDefault()
				return@click
			}

			toggleBlur(toBeHidden, toggle)
		}

		toggleBlur(toBeHidden, toggle)
	}

	fun toggleBlur(toBeHidden: String, toggle: String) {
		val hide = jq(toBeHidden)
		if (jq(toggle).`is`(":checked")) {
			hide.removeClass("blurSection")
			hide.addClass("noBlur")
		} else {
			hide.removeClass("noBlur")
			hide.addClass("blurSection")
		}
	}

	fun enableBlur(toBeHidden: String) {
		val hide = jq(toBeHidden)
		hide.removeClass("noBlur")
		hide.addClass("blurSection")
	}

	fun disableBlur(toBeUnhidden: String) {
		val hide = jq(toBeUnhidden)
		hide.removeClass("blurSection")
		hide.addClass("noBlur")
	}

	fun createToggle(internalName: String, toggleMainText: String, toggleSubText: String?, needsToBeSaved: Boolean, isEnabled: Boolean): Pair<Int, JQuery> {
		val html = """<div class="toggleable-wrapper">
    <div class="information">
		$toggleMainText

		${ if (toggleSubText != null) "<div class=\"sub-text\">$toggleSubText</div>" else ""}
	</div>
	<label class="switch" for="cmn-toggle-$toggleCounter">
		<input class="cmn-toggle" type="checkbox" data-internal-name="$internalName" value="true" ${ if (needsToBeSaved) "data-send-on-save=\"true\"" else ""} ${if (isEnabled) "checked" else ""} id="cmn-toggle-$toggleCounter" type="checkbox" />
		<div class="slider round"></div>
	</label>
</div>
<br style="clear: both" />"""

		val cnt = toggleCounter
		toggleCounter++

		return Pair(cnt, jq(html))
	}

	fun configureTextChannelSelect(selectChannelDropdown: JQuery, serverConfig: ServerConfig, selectedChannelId: String?) {
		val optionData = mutableListOf<dynamic>()

		for (it in serverConfig.textChannels) {
			val option = object{}.asDynamic()
			option.id = it.id
			val text = "<span style=\"font-weight: 600;\">#${it.name}</span>"
			option.text = text

			if (!it.canTalk) {
				option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(231, 76, 60);\">${legacyLocale["DASHBOARD_NoPermission"].replace("!", "")}</span>"
			}

			if (it.id == selectedChannelId)
				option.selected = true

			optionData.add(option)
		}

		val options = object{}.asDynamic()

		options.data = optionData.toTypedArray()
		options.escapeMarkup = { str: dynamic ->
			str
		}

		selectChannelDropdown.asDynamic().select2(
				options
		)

		selectChannelDropdown.on("select2:select") { event, a ->
			val channelId = selectChannelDropdown.`val`() as String

			val channel = serverConfig.textChannels.firstOrNull { it.id == channelId }

			if (channel != null && !channel.canTalk) {
				event.preventDefault()
				selectChannelDropdown.asDynamic().select2("close")

				val modal = TingleModal(
						TingleOptions(
								footer = true,
								cssClass = arrayOf("tingle-modal--overflow")
						)
				)

				modal.setContent(
						jq("<div>").append(
								jq("<div>")
										.addClass("category-name")
										.text(legacyLocale["DASHBOARD_NoPermission"])
						).append(jq("<div>").css("text-align", "center").append(
								jq("<img>")
										.attr("src", "https://mrpowergamerbr.com/uploads/2018-06-17_11-19-43.gif")
										.css("width", "100%")
						)
						).append(jq("<div>").css("text-align", "center").append(
								jq("<p>")
										.text("Atualmente eu não consigo falar no canal que você deseja porque eu não tenho permissão para isto... \uD83D\uDE2D")
						).append(
								jq("<p>")
										.text("Para eu conseguir falar neste canal, clique com botão direito no canal que você deseja que eu possa falar, vá nas permissões, adicione um permission override para mim e dê permissão para que eu possa ler mensagens e enviar mensagens!")
						))
								.html()
				)

				modal.addFooterBtn("<i class=\"far fa-thumbs-up\"></i> Já arrumei!", "button-discord button-discord-info pure-button button-discord-modal") {
					modal.close()
					window.location.reload()
				}

				modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
					modal.close()
				}
				modal.open()
			}
		}
	}

	class SUMMARY(consumer: TagConsumer<*>) :
			HTMLTag("summary", consumer, emptyMap(),
					inlineTag = true,
					emptyTag = false), HtmlInlineTag {
	}

	fun DETAILS.summary(block: SUMMARY.() -> Unit = {}) {
		SUMMARY(consumer).visit(block)
	}

	fun configureTextArea(jquery: JQuery, markdownPreview: Boolean = false, serverConfig: ServerConfig?, sendTestMessages: Boolean = false, textChannelSelect: JQuery? = null, showPlaceholders: Boolean = false, placeholders: Map<String, String> = mapOf(), showTemplates: Boolean = false, templates: Map<String, String> = mapOf(), customTokens: Map<String, String> = mapOf()) {
		val div = jq("<div>") // wrapper
				.css("position", "relative")

		if (showTemplates) {
			println("Displaying templates")
			val select = jq("<select>")
			select.insertBefore(jquery)

			val optionData = mutableListOf<dynamic>()

			val dummyPlaceholder = object{}.asDynamic()
			dummyPlaceholder.id = ""
			dummyPlaceholder.text = ""

			optionData.add(dummyPlaceholder)

			for (it in templates) {
				val option = object{}.asDynamic()
				option.id = it.key
				// option.id = it.id
				val text = it.key
				option.text = text

				optionData.add(option)
			}

			val options = object{}.asDynamic()

			options.placeholder = "Sem ideias? Então veja nossos templates!"
			options.data = optionData.toTypedArray()
			options.escapeMarkup = { str: dynamic ->
				str
			}
			options.dropdownAutoWidth = true

			select.asDynamic().select2(
					options
			)

			select.on("select2:select") { event, a ->
				val selected = select.`val`() as String
				val result = templates[selected]
				select.asDynamic().select2("close")

				val modal = TingleModal(
						TingleOptions(
								footer = true,
								cssClass = arrayOf("tingle-modal--overflow")
						)
				)

				modal.setContent(
						StringBuilder().appendHTML(false).div {
							div(classes = "category-name") {
								+ "Você realmente quer substituir pelo template?"
							}
							p {
								+ "Ao aplicar o template, a sua mensagem atual será perdida para sempre! (A não ser se você tenha copiado ela para outro lugar, aí vida que segue né)"
							}
						}.toString()
				)

				modal.addFooterBtn("<i class=\"far fa-thumbs-up\"></i> Aplicar", "button-discord button-discord-info pure-button button-discord-modal") {
					modal.close()
					jquery.`val`(result)
					jquery.trigger("input", null) // Para recalcular a preview
					AutoSize.update(jquery) // E para o AutoSize recalcular o tamanho

				}

				modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
					modal.close()
				}

				modal.open()
			}
		}

		div.insertBefore(jquery)
		jquery.appendTo(div)

		val extendedMode = 	jq("<div>")
				.html("<i class=\"fas fa-code\"></i> Extended Mode")
				.css("background-color", "green")
				.css("top", "0px")
				.css("right", "0px")
				.css("position", "absolute")
				.css("color", "white")
				.css("opacity", "0.75")
				.css("padding", "3px")
				.css("border-radius", "0px 3px 0px 8px")
				.css("display", "none")
				.css("margin-top", "8px")

		div.append(
				extendedMode
		)

		autosize(jquery)

		if (showPlaceholders) {
			println("Displaying placeholders...")
			// Placeholders são algo mágico, parça

			val html = StringBuilder().appendHTML(false).div {
				details(classes = "fancy-details") {
					summary {
						+ "${locale["loritta.modules.generic.showPlaceholders"]} "
						i(classes = "fas fa-chevron-down") {}
					}
					div(classes = "details-content") {
						table(classes = "fancy-table") {
							thead {
								tr {
									th {
										+"Placeholder"
									}
									th {
										+"Significado"
									}
								}
								placeholders.forEach {
									tr {
										td {
											style = "white-space: nowrap;"
											code(classes = "inline") {
												+"{${it.key}}"
											}
										}
										td {
											+it.value
										}
									}
								}
							}
						}
					}
				}
			}.toString()

			jq(html).insertAfter(jquery)
		}

		if (sendTestMessages) {
			// <button onclick="sendMessage('joinMessage', 'canalJoinId')" class="button-discord button-discord-info pure-button">Test Message</button>
			val button = jq("<button>")
					.addClass("button-discord button-discord-info pure-button")
					.html("<i class=\"fas fa-paper-plane\"></i> ${legacyLocale["DASHBOARD_TestMessage"]}")

			button.on("click") { event, args ->
				val textChannelId = if (textChannelSelect != null) {
					textChannelSelect.`val`() as String
				} else {
					null
				}

				val json = json(
						"channelId" to textChannelId,
						"message" to jquery.`val`(),
						"sources" to arrayOf("user", "member")
				)

				val dynamic = object{}.asDynamic()
				dynamic.url = "${loriUrl}api/v1/guilds/$guildId/send-message"
				dynamic.type = "POST"
				dynamic.dataType = "json"
				dynamic.data = JSON.stringify(json)
				dynamic.success = { data: Json, textStatus: String, event: JQueryXHR ->
					println(data)
				}

				dynamic.error = { event: JQueryXHR, textStatus: String, errorThrown: String ->
					println("Status: " + event.status)
					println(event.response)
				}

				jQuery.ajax(
						settings = dynamic
				)
			}

			button.insertAfter(
					jquery
			)
		}

		if (markdownPreview) {
			val markdownPreview = jq("<div>")
					.attr("id", jquery.attr("id") + "-markdownpreview")

			val converter = ShowdownConverter()
			converter.setOption("simpleLineBreaks", true)

			jquery.on("input") { event, args ->
				var description = jquery.`val`() as String

				val isUsingExtendedMode = try {
					val json = JSON.parse<Json>(description.replace("\r", ""))
					json["content"] != null || json["embed"] != null
				} catch (e: dynamic) {
					false
				}

				if (isUsingExtendedMode) {
					extendedMode.css("display", "")
					val json = JSON.parse<Json>(description.replace("\r", ""))

					markdownPreview.empty()

					// extended mode
					val content = json["content"]
					val embed = json["embed"]
					if (content != null && content is String) {
						description = replaceTokens(content, serverConfig, customTokens)
						description = converter.makeHtml(description)

						markdownPreview.append(description)
					}

					if (embed != null) {
						val markdownEmbedConverter = ShowdownConverter()
						markdownEmbedConverter.setOption("simpleLineBreaks", true)

						fun replaceAndConvert(text: String): String {
							var _text = replaceTokens(text, serverConfig, customTokens)
							_text = converter.makeHtml(_text)

							return jq(_text).html()
						}

						val embed = embed as Json

						val color = embed["color"] as Int?
						val author = embed["author"] as Json?
						val title = embed["title"] as String?
						val url = embed["url"] as String?
						val description = embed["description"] as String?
						val fields = embed["fields"] as Array<Json>?
						val thumbnailObj = embed["thumbnail"] as Json?
						val thumbnailUrl = thumbnailObj?.get("url") as String?
						val imageObj = embed["image"] as Json?
						val imageUrl = imageObj?.get("url") as String?
						val footer = embed["footer"] as Json?

						// oh, embeds...
						// Baseado no https://leovoel.github.io/embed-visualizer/
						val stringBuilder = StringBuilder()
						stringBuilder.appendHTML(false)
								.div(classes = "accessory") {
									div(classes = "embed-wrapper") {
										div(classes = "embed-color-pill") {
											if (color != null) {
												val aux = ("000000" + ((color) ushr 0).toString(16))
												val hex = "#" + aux.slice(aux.length - 6 until aux.length)
												println("Hex: ${hex}")
												style = "background-color: $hex;"
											}
										}
										div(classes = "embed embed-rich") {
											div(classes = "embed-content") {
												div(classes = "embed-content-inner") {
													if (author != null) {
														div(classes = "embed-author") {
															val iconUrl = author["icon_url"] as String?
															if (iconUrl != null) {
																img(src = replaceTokens(iconUrl, serverConfig, customTokens), classes = "embed-author-icon")
															}
															val url = author["url"] as String?
															val name = author["name"] as String?

															if (name != null) {
																if (url != null) {
																	a(url, classes = "embed-author-name") {
																		unsafe {
																			+replaceAndConvert(name)
																		}
																	}
																} else {
																	span(classes = "embed-author-name") {
																		unsafe {
																			+replaceAndConvert(name)
																		}
																	}
																}
															}
														}
													}
													if (title != null) {
														if (url != null) {
															a(target = url, classes = "embed-title") {
																unsafe {
																	+replaceAndConvert(title)
																}
															}
														} else {
															span(classes = "embed-title") {
																unsafe {
																	+replaceAndConvert(title)
																}
															}
														}
													}
													if (description != null) {
														div(classes = "embed-description markup") {
															unsafe {
																+replaceAndConvert(description)
															}
														}
													}
													if (fields != null) {
														div(classes = "embed-fields") {
															for (field in fields) {
																val name = field["name"] as String?
																val value = field["value"] as String?
																val inline = field["inline"] as Boolean? ?: false

																if (name != null && value != null) {
																	div(classes = "embed-field${if (inline) " embed-field-inline" else ""}") {
																		div(classes = "embed-field-name") {
																			unsafe {
																				+replaceAndConvert(name)
																			}
																		}
																		div(classes = "embed-field-value markup") {
																			unsafe {
																				+replaceAndConvert(value)
																			}
																		}
																	}
																}
															}
														}
													}
												}
												if (thumbnailUrl != null) {
													img(src = replaceTokens(thumbnailUrl, serverConfig, customTokens)) {
														style = "max-width: 80px; max-height: 80px;"
													}
												}
											}
											if (imageUrl != null) {
												a(classes = "embed-thumbnail embed-thumbnail-rich") {
													img(classes = "image", src = replaceTokens(imageUrl, serverConfig, customTokens))
												}
											}
											if (footer != null) {
												div {
													val iconUrl = footer["icon_url"] as String?
													val text = footer["text"] as String?

													if (iconUrl != null) {
														img(src = replaceTokens(iconUrl, serverConfig, customTokens), classes = "embed-footer-icon") {
															width = "20"
															height = "20"
														}
													}

													if (text != null) {
														span("embed-footer") {
															unsafe {
																+replaceAndConvert(text)
															}
														}
													}
												}
											}
										}
									}
								}

						markdownPreview.append(
								stringBuilder.toString()
						)
					}
				} else {
					extendedMode.css("display", "none")
					description = replaceTokens(description, serverConfig, customTokens)
					description = converter.makeHtml(description)

					markdownPreview.html(description)
				}
			}

			markdownPreview.insertAfter(
					jquery
			)
		}

		jquery.trigger("input", null)
	}

	fun replaceTokens(text: String, serverConfig: ServerConfig?, customTokens: Map<String, String?> = mutableMapOf<String, String?>()): String {
		val selfUser = serverConfig?.selfUser ?: Member("123170274651668480", "Loritta", "0219", "${loriUrl}assets/img/unknown.png")
		var message = text
		val mentionUser = "<span class=\"discord-mention\">@${selfUser.name}</span>"
		val user = selfUser.name
		val userDiscriminator = selfUser.discriminator
		val userId = selfUser.id
		val nickname = selfUser.name
		val avatarUrl = selfUser.avatar
		var guildName = ""
		var guildSize = ""
		val mentionOwner = ""
		val owner = ""

		if (serverConfig != null) {
			guildName = serverConfig.guildName
			guildSize = serverConfig.memberCount.toString()
		}
		for ((token, value) in customTokens) {
			message = message.replace("{$token}", value ?: "\uD83E\uDD37")
		}

		message = message.replace("{@user}", mentionUser)
		message = message.replace("{user}", user)
		message = message.replace("{user-id}", userId)
		message = message.replace("{user-avatar-url}", avatarUrl)
		message = message.replace("{user-discriminator}", userDiscriminator)
		message = message.replace("{nickname}", nickname)
		message = message.replace("{guild}", guildName)
		message = message.replace("{guild-size}", guildSize)
		message = message.replace("{@owner}", mentionOwner)
		message = message.replace("{owner}", owner)

		// EMOTES
		// Nós fazemos uma vez antes e depois uma depois, para evitar bugs (já que :emoji: também existe dentro de <:emoji:...>
		val regex = Regex("<(a)?:([A-z0-9_-]+):([0-9]+)>", RegexOption.MULTILINE)
		message = regex.replace(message) { matchResult: MatchResult ->
			matchResult.groups.forEachIndexed { index, result ->
				println("$index group is $result")
			}
			// <img class="inline-emoji" src="https://cdn.discordapp.com/emojis/$2.png?v=1">
			val extension = if (matchResult.groups[1]?.value == "a")
				"gif"
			else
				"png"
			"<img class=\"inline-emoji\" src=\"https://cdn.discordapp.com/emojis/${matchResult.groups[3]?.value}.$extension?v=1\">"
		}

		if (serverConfig != null) {
			// TEXT CHANNELS
			for (textChannel in serverConfig.textChannels) {
				message = message.replace("#${textChannel.name}", "<#" + textChannel.id + ">")
				message = message.replace("<#" + textChannel.id + ">", "<span class=\"discord-mention\">#${textChannel.name}</span>")
			}

			// ROLES
			for (role in serverConfig.roles) {
				message = message.replace("@${role.name}", "<@&${role.id}>")

				val roleSpan = jq("<span>")
						.text("@" + role.name)
						.addClass("discord-mention")

				if (role.color != null) {
					roleSpan.css("color", "rgb(${role.color.red}, ${role.color.green}, ${role.color.blue})")
					roleSpan.css("background-color", "rgba(${role.color.red}, ${role.color.green}, ${role.color.blue}, 0.298039)")
				}

				message = message.replace("<@&${role.id}>", roleSpan.prop("outerHTML") as String)
			}

			// MEMBERS
			/* val memberRegex = Regex("<@([0-9]+)>")
			message = memberRegex.replace(message, transform = {
				val id = it.groupValues[1]

				val memberResult = serverConfig.members.firstOrNull { it.id == id }

				if (memberResult != null) {
					"<span class=\"discord-mention\">@${memberResult.name}</span>"
				} else {
					it.value
				}
			}) */

			// EMOTES (de novo)
			for (emote in serverConfig.emotes) {
				message = message.replace(":${emote.name}:", "<:${emote.name}:${emote.id}>")
			}
			message = regex.replace(message) { matchResult: MatchResult ->
				// <img class="inline-emoji" src="https://cdn.discordapp.com/emojis/$2.png?v=1">
				val extension = if (matchResult.groups[1]?.value == "a")
					"gif"
				else
					"png"
				"<img class=\"inline-emoji\" src=\"https://cdn.discordapp.com/emojis/${matchResult.groups[3]?.value}.$extension?v=1\">"
			}
		}

		return message
	}
}

/* fun <T> Any.toJson(): T {
	return JSON.parse(JSON.stringify(this))
}

fun Any.toJson(): Json {
	return JSON.parse(JSON.stringify(this))
}

fun Any.stringify(): String {
	return JSON.stringify(this)
} */

fun Int.toString(radix: Int): String {
	val value = this
	@Suppress("UnsafeCastFromDynamic")
	return js(code = "value.toString(radix)")
}