import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import utils.AbstractCommand
import utils.CommandCategory

object CommandsView {
	fun start() {
		LoriDashboard.showLoadingBar("Carregando...")

		jQuery.post("${loriUrl}api/v1/misc/get-commands", { data, b, c ->
			LoriDashboard.hideLoadingBar()

			val commands = data.toJson<Array<AbstractCommand>>()

			for ((index, category) in CommandCategory.values().withIndex()) {
				val filteredCommands = commands.filter { it.category.toString() == category.toString() }

				val stringBuilder = StringBuilder()

				var image = when (category) {
					CommandCategory.SOCIAL -> "${loriUrl}assets/img/social.png"
					CommandCategory.POKEMON -> "${loriUrl}assets/img/pokemon.png"
					CommandCategory.MINECRAFT -> "${loriUrl}assets/img/loritta_pudim.png"
					CommandCategory.FUN -> "${loriUrl}assets/img/vieirinha.png"
					CommandCategory.UTILS -> "${loriUrl}assets/img/utils.png"
					CommandCategory.MUSIC -> "${loriUrl}assets/img/loritta_headset.png"
					CommandCategory.ANIME -> "${loriUrl}assets/img/loritta_anime.png"
					else -> "${loriUrl}assets/img/loritta_gabizinha_v1.png"
				}

				stringBuilder.appendHTML().div(if (index % 2 == 0) "evenWrapper" else "oddWrapper") {
					fun addCommandInformation()  {
						div("sectionText") {
							h2("sectionHeader") {
								+ legacyLocale[category.fancyTitle]
							}
							p { +legacyLocale[category.description] }
						}
					}

					div("contentWrapper") {
						ins("adsbygoogle") {
							style = "display:block"
							attributes.put("data-ad-client", "ca-pub-9989170954243288")
							attributes.put("data-ad-slot", "4611100335")
							attributes.put("data-ad-format", "auto")
						}

						script {
							+ "(adsbygoogle = window.adsbygoogle || []).push({});"
						}

						div("vertically-centered-content") {
							div("pure-g vertically-centered-content") {
								if (index % 2 == 0) {
									div("pure-u-1 pure-u-md-1-4") {
										img(null, image, "animate-on-scroll-left is-invisible") {
											style = "width: 100%;"
										}
									}
									div("pure-u-1 pure-u-md-3-4") {
										addCommandInformation()
									}
								} else {
									div("pure-u-1 pure-u-md-3-4") {
										addCommandInformation()
									}
									div("pure-u-1 pure-u-md-1-4") {
										img(null, image, "animate-on-scroll-right is-invisible") {
											style = "width: 100%;"
										}
									}
								}
							}
						}

						ins("adsbygoogle") {
							style = "display:block"
							attributes.put("data-ad-client", "ca-pub-9989170954243288")
							attributes.put("data-ad-slot", "4611100335")
							attributes.put("data-ad-format", "auto")
						}

						script {
							+ "(adsbygoogle = window.adsbygoogle || []).push({});"
						}

						hr {}

						div("pure-g") {
							for (command in filteredCommands) {
								div("pure-u-1 pure-u-md-1-2") {
									var usage = command.usage ?: ""
									p {
										style = "font-weight: bold; font-size: 1.1em;"
										+ "+${command.label} $usage"
									}
									/* p {
										style = "opacity: 0.6;"
										+ command.aliases.joinToString(", ", transform = { "+" + it })
									} */
									// p { +command.description }
								}
							}
						}
					}
				}

				println(category)
				jq("#wrapper").append(stringBuilder.toString())
			}
		})
	}
}