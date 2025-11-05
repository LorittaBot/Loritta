@file:NoLiveLiterals

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import net.perfectdreams.spicymorenitta.utils.jsObject
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Audio
import org.w3c.dom.HTMLDivElement

external val guildId: String

external fun delete(p: dynamic): Boolean = definedExternally

object LoriDashboard {
	var toggleCounter = 1000

	val configSavedSfx: Audio by lazy { Audio("${loriUrl}assets/snd/config_saved.mp3") }
	val configErrorSfx: Audio by lazy { Audio("${loriUrl}assets/snd/config_error.mp3") }

	val wrapper: JQuery by lazy {
		jq("#server-configuration")
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
			val text = "<span style=\"font-weight: 600;\">#${stripHtmlTagsUsingDom(it.name)}</span>"
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
						jsObject<TingleOptions> {
							footer = true
							cssClass = arrayOf("tingle-modal--overflow")
						}
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
}

fun Int.toString(radix: Int): String {
	val value = this
	@Suppress("UnsafeCastFromDynamic")
	return js(code = "value.toString(radix)")
}