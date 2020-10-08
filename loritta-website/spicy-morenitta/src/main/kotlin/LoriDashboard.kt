
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.appendHTML
import net.perfectdreams.loritta.embededitor.EmbedEditorCrossWindow
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.data.crosswindow.*
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.MessageEvent
import org.w3c.dom.Audio
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

	fun configureTextChannelSelect(selectChannelDropdown: JQuery, textChannels: List<net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig.TextChannel>, selectedChannelId: Long?) {
		val optionData = mutableListOf<dynamic>()

		for (it in textChannels) {
			val option = object{}.asDynamic()
			option.id = it.id
			val text = "<span style=\"font-weight: 600;\">#${it.name}</span>"
			option.text = text

			if (!it.canTalk)
				option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(231, 76, 60);\">${legacyLocale["DASHBOARD_NoPermission"].replace("!", "")}</span>"

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

			val channel = textChannels.firstOrNull { it.id.toString() == channelId }

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

	fun configureTextArea(
			jquery: JQuery,
			markdownPreview: Boolean = false,
			serverConfig: ServerConfig?,
			sendTestMessages: Boolean = false,
			textChannelSelect: JQuery? = null,
			showPlaceholders: Boolean = false,
			placeholders: List<Placeholder> = listOf(),
			showTemplates: Boolean = false,
			templates: Map<String, String> = mapOf()
	) {
		val div = jq("<div>") // wrapper
				.css("position", "relative")

		val wrapperWithButtons = jq("<div>")
				.css("display", "flex")
				.css("gap", "0.5em")
				.css("flex-wrap", "wrap")
				.css("justify-content", "space-between")

		if (showTemplates) {
			val select = jq("<select>")
			wrapperWithButtons.append(select)

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

		val newElement = document.create.button(classes = "button-discord button-discord-info pure-button") {
			i(classes = "fas fa-edit") {}
			+ " ${locale["website.dashboard.advancedEditor"]}"

			onClickFunction = {
				val extendedWindow = window.open("https://embeds.loritta.website/")!!

				window.addEventListener("message", { event ->
					event as MessageEvent
					println("Received message ${event.data} from ${event.origin}")

					// We check for embeds.loritta.website because AdSense can also send messages via postMessage
					if (event.origin.contains("embeds.loritta.website") && event.source == extendedWindow) {
						println("Received message from our target source, yay!")

						val packet = EmbedEditorCrossWindow.communicationJson.parse(PacketWrapper.serializer(), event.data as String)

						if (packet.m is ReadyPacket) {
							println("Is ready packet, current text area is ${jquery.`val`()}")

							val content = createMessageFromString(jquery.`val`() as String)

							extendedWindow.postMessage(
									EmbedEditorCrossWindow.communicationJson.stringify(
											PacketWrapper.serializer(),
											PacketWrapper(
													MessageSetupPacket(
															content,
															placeholders
													)
											)
									),
									"*"
							)
						} else if (packet.m is UpdatedMessagePacket) {
							jquery.`val`((packet.m as UpdatedMessagePacket).content)

							// Trigger a update
							jquery.trigger("input", null) // Para recalcular a preview
							AutoSize.update(jquery) // E para o AutoSize recalcular o tamanho
						}
					}
				})
			}
		}

		wrapperWithButtons.append(newElement)

		if (sendTestMessages) {
			// <button onclick="sendMessage('joinMessage', 'canalJoinId')" class="button-discord button-discord-info pure-button">Test Message</button>
			val button = jq("<button>")
					.addClass("button-discord button-discord-info pure-button")
					.html("<i class=\"fas fa-paper-plane\"></i> ${locale["website.dashboard.testMessage"]}")

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

			wrapperWithButtons.append(button)
		}

		wrapperWithButtons.insertBefore(jquery)

		console.log(div)

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
								placeholders.filterNot { it.hidden }.forEach {
									tr {
										td {
											style = "white-space: nowrap;"
											code(classes = "inline") {
												+ it.name
											}
										}
										td {
											+ (it.description ?: "???")
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

		if (markdownPreview) {
			val markdownPreview = jq("<div>")
					.attr("id", jquery.attr("id") + "-markdownpreview")
					.attr("class", "discord-style")

			jquery.on("input") { event, args ->
				val content = jquery.`val`() as String

				val message = createMessageFromString(content)

				val renderer = EmbedRenderer(message, placeholders)

				// Clear the current preview
				markdownPreview.empty()

				// And now render the embed!
				markdownPreview.get()[0].append {
					div(classes = "theme-light") {
						renderer.generateMessagePreview(this)
					}
				}
			}

			markdownPreview.insertAfter(
					jquery
			)
		}

		jquery.trigger("input", null)
	}

	/**
	 * Creates a (Discord) message from a string.
	 *
	 * If it is a valid JSON, it will be generated via [EmbedRenderer.json], if it is invalid, a object
	 * will be created with the [content]
	 *
	 * @param  the input, can be a JSON object as a string or a raw message
	 * @return a [DiscordMessage]
	 */
	fun createMessageFromString(content: String): DiscordMessage {
		return try {
			val rawMessage = content
					.replace("\n", "")
					.replace("\r", "")

			// Try parsing the current content as JSON
			if (!(rawMessage.startsWith("{") && rawMessage.endsWith("}"))) // Just to avoid parsing the (probably invalid) JSON
				throw RuntimeException("Not in a JSON format")

			EmbedRenderer.json
					.parse(DiscordMessage.serializer(), content)
		} catch (e: Throwable) {
			// If it fails, probably it is a raw message, so we are going to just create a
			// DiscordMessage based on that
			DiscordMessage(content)
		}
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