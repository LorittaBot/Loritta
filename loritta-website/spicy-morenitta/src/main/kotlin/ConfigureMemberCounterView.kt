
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.spicymorenitta.utils.appendBuilder
import net.perfectdreams.spicymorenitta.utils.selectAll
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import userdata.CounterThemes
import userdata.CounterUtils
import userdata.MemberCounterConfig
import utils.ServerConfig
import utils.getTextChannelConfig
import kotlin.browser.document
import kotlin.dom.clear

object ConfigureMemberCounterView {
	const val MAX_DONATOR_TOGGLES = 3
	const val MAX_USER_TOGGLES = 1

	lateinit var serverConfig: ServerConfig

	fun start() {
		document.addEventListener("DOMContentLoaded", {
			serverConfig = LoriDashboard.loadServerConfig()

			println(serverConfig.donationKey?.value)
			val howManyTogglesCanBeEnabled = if ((serverConfig.donationKey?.value ?: 0.0) >= 19.99) {
				MAX_DONATOR_TOGGLES
			} else {
				MAX_USER_TOGGLES
			}

			println("User can enable $howManyTogglesCanBeEnabled toggles! ${(serverConfig.donationKey?.value ?: 0.0) >= 19.99}")
			val textChannels = jq("#member-counter-list")

			for (textChannel in serverConfig.textChannels) {
				val memberCounterConfig = serverConfig.getTextChannelConfig(textChannel).memberCounterConfig

				val entry = jq("<div>")
						.attr("data-text-channel-id", textChannel.id)

				entry.append(
						jq("<div>")
								.attr("class", "flavourText")
								.text("#" + textChannel.name)
				)

				val toggle = LoriDashboard.createToggle("enableCounter", "Ativar contador de membros", "Após ativar, utilize {counter} no texto do seu tópico para ativar o contador!", false, memberCounterConfig != null)
				val jToggle = toggle.second
				entry.append(
						jToggle
				)

				val textAreaWrapper = jq("<div>")
						.attr("id", "text-area-wrapper-${textChannel.id}")

				entry.append(
						jq("<div>")
								.attr("class", "flavourText")
								.text("Tópico do Canal (Será utilizado após alguém entrar/sair)")
				)

				textAreaWrapper.append(
						jq("<textarea>")
								.attr("id", "text-area-${textChannel.id}")
								.`val`(
										if (memberCounterConfig == null) {
											println("Using text channel's topic!")
											textChannel.topic ?: ""
										} else {
											println("Using member counter's topic!")
											memberCounterConfig.topic
										}
								)
				)

				textAreaWrapper[0]!!.appendBuilder(
						StringBuilder().appendHTML(false).div {
							div(classes = "flavourText") {
								+ "Tema do contador de membros"
							}
							select(classes = "counter-theme") {
								for (theme in CounterThemes.values()) {
									option {
										if (memberCounterConfig != null) {
											// println(memberCounterConfig.theme)
											// println("theme.name == memberCounterConfig.theme? ${theme.name == memberCounterConfig.theme}")
											selected = theme.name == memberCounterConfig.theme
										}
										// println(locale[theme.localizedName])
										value = theme.name
										+ locale[theme.localizedName]
									}
								}
							}
							div(classes = "flavourText") {
								+ "Preenchimento com Zeros"
							}
							input(InputType.number, classes = "counter-padding") {
								min = "1"
								max = "10"
								value = (memberCounterConfig?.padding ?: 5).toString()
							}
							div(classes = "counter-preview") {
								+ "Aqui irá ficar a preview do contador, quando existir... algum dia"
							}
						}
				)

				entry.append(textAreaWrapper)

				val selectThemeElement = (entry[0]!!.querySelector(".counter-theme")!! as HTMLSelectElement)
				val counterPaddingElement = (entry[0]!!.querySelector(".counter-padding")!! as HTMLInputElement)
				val counterPreviewElement = (entry[0]!!.querySelector(".counter-preview")!! as HTMLDivElement)

				val callback: () -> (Unit) = {
					val theme = CounterThemes.valueOf(selectThemeElement.value)

					counterPreviewElement.clear()
					val padding = counterPaddingElement.value.toInt()
					// counterPreviewElement.append(theme.localizedName)

					val counterBuilder5 = CounterUtils.generatePrettyCounterHtml(5, theme, padding)
					counterPreviewElement.appendBuilder(counterBuilder5)
					val counterBuilder10 = CounterUtils.generatePrettyCounterHtml(10, theme, padding)
					counterPreviewElement.appendBuilder(counterBuilder10)
					val counterBuilder250 = CounterUtils.generatePrettyCounterHtml(250, theme, padding)
					counterPreviewElement.appendBuilder(counterBuilder250)
					val counterBuilderAllChars = CounterUtils.generatePrettyCounterHtml(1234567890, theme, padding)
					counterPreviewElement.appendBuilder(counterBuilderAllChars)
				}

				selectThemeElement.onchange = {
					callback.invoke()
				}

				counterPaddingElement.onchange = {
					callback.invoke()
				}

				callback.invoke()

				entry.append("<hr>")

				textChannels.append(entry)

				LoriDashboard.applyBlur("#text-area-wrapper-${textChannel.id}", "#cmn-toggle-${toggle.first.toString()}")

				jq("#cmn-toggle-${toggle.first}").click {
					// it.preventDefault()
					val theToggle = jq("#cmn-toggle-${toggle.first}")
					println("Something was changed! ${theToggle.`is`(":checked")}")

					if (theToggle.`is`(":checked")) {
						val enabledToggles = document.selectAll<HTMLInputElement>(".cmn-toggle")
								.filter {
									it.checked
								}

						val howManyTogglesAreEnabled = enabledToggles.size
						println("There are $howManyTogglesAreEnabled enabled toggles")

						// O howManyTogglesAreEnabled contém o toggle atual, já que o jQuery é assim
						if (howManyTogglesAreEnabled > howManyTogglesCanBeEnabled) {
							println("too many toggles enabled! ${howManyTogglesAreEnabled} > ${howManyTogglesCanBeEnabled}, untoggling... max is ${MAX_USER_TOGGLES}")
							it.preventDefault()
							it.stopPropagation()

							if (howManyTogglesCanBeEnabled >= MAX_USER_TOGGLES) {
								println("showing premium modal")
								Stuff.showPremiumFeatureModal()
							}
						}
					}
				}
			}
		})
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("text_channels", {
			val entries = mutableListOf<dynamic>()

			val divs = jq("[data-text-channel-id]")

			divs.each { index, _elem ->
				val elem = jQuery(_elem)

				val textChannelId = elem.attr("data-text-channel-id")

				val isEnabled = elem.find(".cmn-toggle").`is`(":checked")

				if (isEnabled) {
					val dyn = object{}.asDynamic()
					dyn.id = textChannelId
					dyn.memberCounterConfig = MemberCounterConfig(
							elem.find("#text-area-${textChannelId}").`val`() as String,
							elem.find(".counter-theme").`val`() as String,
							(elem.find(".counter-padding").`val`() as String).toInt()
					)

					entries.add(dyn)
				}
			}

			it["entries"] = entries
		})
	}
}