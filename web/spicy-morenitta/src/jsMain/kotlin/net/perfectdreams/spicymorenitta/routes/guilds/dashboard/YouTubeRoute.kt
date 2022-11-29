@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import jq
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.embededitor.data.crosswindow.Placeholder
import net.perfectdreams.loritta.embededitor.data.crosswindow.RenderType
import net.perfectdreams.loritta.common.utils.Placeholders
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.extensions.listIsEmptySection
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import net.perfectdreams.spicymorenitta.views.dashboard.getPlan
import org.w3c.dom.*
import kotlin.collections.set
import kotlin.js.Json
import kotlin.js.json

class YouTubeRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/youtube") {
	companion object {
		private const val LOCALE_PREFIX = "modules.youtube"
	}

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val textChannels: List<ServerConfig.TextChannel>,
			val trackedYouTubeChannels: List<ServerConfig.TrackedYouTubeAccount>
	)

	val trackedYouTubeAccounts = mutableListOf<ServerConfig.TrackedYouTubeAccount>()
	val cachedChannelByChannelUrl = mutableMapOf<String, YouTubeAccountInfo>()

	override fun onUnload() {
		trackedYouTubeAccounts.clear()
		cachedChannelByChannelUrl.clear()
	}

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "activekeys", "youtube", "textchannels")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val stuff = document.select<HTMLDivElement>("#level-stuff")

			stuff.append {
				div(classes = "tracked-youtube-accounts") {}

				hr {}
			}

			val addEntryButton = document.select<HTMLButtonElement>("#add-new-entry")
			addEntryButton.onClick {
				val premiumPlan = guild.activeDonationKeys.getPlan()

				if (trackedYouTubeAccounts.size >= premiumPlan.maxYouTubeChannels) {
					Stuff.showPremiumFeatureModal {
						h2 {
							+ "Adicione todos os seus amigos!"
						}
						p {
							+ "Faça upgrade para poder adicionar mais canais!"
						}
					}
					return@onClick
				}

				editTrackedYouTubeAccount(
						guild,
						null,
						ServerConfig.TrackedYouTubeAccount(
								-1L,
								"",
								"{link}"
						)
				)
			}

			trackedYouTubeAccounts.addAll(guild.trackedYouTubeChannels)

			updateTrackedYouTubeAccountsList(guild)
		}
	}

	private fun updateTrackedYouTubeAccountsList(guild: PartialGuildConfiguration) {
		val trackedDiv = document.select<HTMLDivElement>(".tracked-youtube-accounts")

		trackedDiv.clear()

		trackedDiv.append {
			if (trackedYouTubeAccounts.isEmpty()) {
				listIsEmptySection()
			} else {
				for (account in trackedYouTubeAccounts) {
					createTrackedYouTubeAccountEntry(guild, account)
				}
			}
		}
	}

	private fun TagConsumer<HTMLElement>.createTrackedYouTubeAccountEntry(guild: PartialGuildConfiguration, trackedYouTubeAccount: ServerConfig.TrackedYouTubeAccount) {
		this.div(classes = "discord-generic-entry timer-entry") {
			attributes["data-youtube-account"] = trackedYouTubeAccount.youTubeChannelId

			img(classes = "amino-small-image") {
				style = "width: 6%; height: auto; border-radius: 999999px; float: left; position: relative; bottom: 8px;"
				// src =
			}

			div(classes = "pure-g") {
				div(classes = "pure-u-1 pure-u-md-18-24") {
					div {
						style = "margin-left: 10px; margin-right: 10;"
						div(classes = "amino-title entry-title") {
							style = "font-family: Whitney,Helvetica Neue,Helvetica,Arial,sans-serif;"
							+ "..."
						}
						div(classes = "amino-title toggleSubText") {
							+ "..."
						}
					}
				}
				div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-content") {
					button(classes = "button-discord button-discord-edit pure-button delete-button") {
						style = "margin-right: 8px; min-width: 0px;"

						onClickFunction = {
							trackedYouTubeAccounts.remove(trackedYouTubeAccount)
							updateTrackedYouTubeAccountsList(guild)
						}

						i(classes = "fas fa-trash") {}
					}
					button(classes = "button-discord button-discord-edit pure-button edit-button") {
						+"Editar"
					}
				}
			}
		}

		m.launch {
			val accountInfo = loadAccountInfoFromUrl("https://www.youtube.com/channel/${trackedYouTubeAccount.youTubeChannelId}")
			info("Loading info for account ${trackedYouTubeAccount.youTubeChannelId}...")

			val trackedDiv = document.select<HTMLDivElement>("[data-youtube-account='${trackedYouTubeAccount.youTubeChannelId}']")

			if (accountInfo == null) {
				trackedYouTubeAccounts.remove(trackedYouTubeAccount)
				updateTrackedYouTubeAccountsList(guild)
			} else {
				val currentChannel = guild.textChannels.firstOrNull { it.id == trackedYouTubeAccount.channelId }

				val channelName = currentChannel?.let { "#${it.name}" } ?: "???"

				trackedDiv.select<HTMLImageElement>(".amino-small-image")
						.src = accountInfo.avatarUrl

				trackedDiv.select<HTMLDivElement>(".entry-title")
						.innerText = "${accountInfo.title} (${accountInfo.channelId})"

				trackedDiv.select<HTMLDivElement>(".toggleSubText")
						.innerText = channelName

				trackedDiv.select<HTMLDivElement>(".edit-button")
						.onClick {
							editTrackedYouTubeAccount(guild, accountInfo, trackedYouTubeAccount)
						}

				info("yey")
			}
		}
	}

	private fun editTrackedYouTubeAccount(guild: PartialGuildConfiguration, accountInfo: YouTubeAccountInfo?, trackedYouTubeAccount: ServerConfig.TrackedYouTubeAccount) {
		val modal = TingleModal(
			jsObject<TingleOptions> {
				footer = true
				cssClass = arrayOf("tingle-modal--overflow")
				closeMethods = arrayOf()
			}
		)

		modal.addFooterBtn("Salvar", "button-discord button-discord-info pure-button button-discord-modal") {
			// Iremos salvar a conta atual, aplicando as mudanças realizadas modal.close()
			var position = trackedYouTubeAccounts.size

			if (trackedYouTubeAccounts.contains(trackedYouTubeAccount)) { // A conta atual já existe...
				// Caso já exista, vamos remover e recolocar na posição certa
				position = trackedYouTubeAccounts.indexOf(trackedYouTubeAccount)
				if (position == -1)
					position = trackedYouTubeAccounts.size
				trackedYouTubeAccounts.remove(trackedYouTubeAccount)
			}

			val channelId = visibleModal.select<HTMLInputElement>(".choose-channel")
					.value

			val text = visibleModal.select<HTMLInputElement>(".choose-text")
					.value

			debug("Adding ${visibleModal.getAttribute("data-youtube-account-id")} account to the tracked YouTube accounts list...")
			val account = ServerConfig.TrackedYouTubeAccount(
					channelId.toLong(),
					visibleModal.getAttribute("data-youtube-account-id") ?: "",
					text
			)

			trackedYouTubeAccounts.add(position, account)

			modal.close()

			updateTrackedYouTubeAccountsList(guild)
		}

		modal.addFooterBtn("Cancelar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.setContent(
				createHTML().div {
					div(classes = "category-name") {
						+ "Conta do YouTube"
					}

					div {
						style = "display: flex;"


						div {
							style = "flex: 0 1 25%;"

							img(classes = "account-icon") {
								style = "width: 100%; height: auto; float: left; border-radius: 999999px;"

								src = "https://i.imgur.com/s4dTtBy.jpg"
							}
						}

						div {
							style = "flex-grow: 1; margin: 0.25em;"

							h5(classes = "section-title") {
								+ locale["$LOCALE_PREFIX.userName"]
							}

							input(classes = "youtube-account") {
								if (accountInfo != null) {
									value = "https://www.youtube.com/channel/${accountInfo.channelId}"
								}
								placeholder = "https://www.youtube.com/channel/UC-eeXSRZ8cO-i2NZYrWGDnQ"
							}

							div(classes = "account-config blurSection") {
								h5(classes = "section-title") {
									+ locale["$LOCALE_PREFIX.channel"]
								}

								select("choose-channel") {
									style = "box-sizing: border-box !important; width: 100%;"
									// style = "width: 100%;"
									// style = "width: 320px;"

									for (channel in guild.textChannels) {
										option {
											value = channel.id.toString()

											if (channel.id == trackedYouTubeAccount.channelId) {
												selected = true
											}

											+("#${channel.name}")
										}
									}
								}

								h5(classes = "section-title") {
									+ locale["$LOCALE_PREFIX.theMessage"]
								}

								textArea(classes = "choose-text") {
									style = "box-sizing: border-box !important; width: 100%;"

									+trackedYouTubeAccount.message
								}

								/* createToggle(
										"Utilizar Webhooks"
								) { result ->
									Stuff.showPremiumFeatureModal {
										h2 {
											+ "Seja diferente d e diferente!"
										}
										p {
											+ "Faça upgrade para o Plano Recomendado para poder customizar o nome e avatar as notificações!"
										}
									}
									result
								} */
							}
						}
					}
				}
		)
		modal.open()
		modal.trackOverflowChanges(m)

		fun processAccountInfo(accountInfo: YouTubeAccountInfo) {
			visibleModal.select<HTMLDivElement>(".account-config")
					.removeClass("blurSection")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.title

			visibleModal.select<HTMLImageElement>(".account-icon")
					.src = accountInfo.avatarUrl
					.replace("_normal", "_400x400")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.title

			visibleModal.setAttribute("data-youtube-account-id", accountInfo.channelId)
		}

		val youTubeAccountInput = visibleModal.select<HTMLInputElement>(".youtube-account")
		youTubeAccountInput.delayedTyping(m, 2_500, {
			info("Writing something, blurring content...")
			visibleModal.select<HTMLDivElement>(".account-config")
					.addClass("blurSection")
		}) {
			info("Finished typing! Loading data...")

			val screenName = youTubeAccountInput.value

			m.launch {
				val accountInfo = loadAccountInfoFromUrl(screenName)

				if (accountInfo == null) {
					visibleModal.select<HTMLDivElement>(".account-config")
							.addClass("blurSection")
				} else {
					processAccountInfo(accountInfo)
				}
			}
		}

		if (accountInfo != null)
			processAccountInfo(accountInfo)

		LoriDashboard.configureTextArea(
				jq(".tingle-modal--visible .choose-text"),
				true,
				false,
				null,
				true,
				listOf(
						Placeholder(
								Placeholders.LINK.asKey,
								"https://youtu.be/p3G5IXn0K7A",
								locale["${LOCALE_PREFIX}.videoLink"],
								RenderType.TEXT,
								false
						)
				),
				showTemplates = false
		)
	}

	private suspend fun loadAccountInfoFromUrl(channelUrl: String): YouTubeAccountInfo? {
		info("Loading info for account ${channelUrl}...")

		if (cachedChannelByChannelUrl.containsKey(channelUrl)) {
			info("Loading cached channel $channelUrl ${cachedChannelByChannelUrl[channelUrl]}")
			return cachedChannelByChannelUrl[channelUrl]
		}

		val response = http.get("${window.location.origin}/api/v1/youtube/channel") {
			parameter("channelLink", channelUrl)
		}

		val statusCode = response.status

		if (statusCode != HttpStatusCode.OK) {
			warn("Status Code is $statusCode, oof")
			return null
		} else {
			val text = response.bodyAsText()
			val accountInfo = parseAccountInfo(text)
			cachedChannelByChannelUrl[channelUrl] = accountInfo
			return accountInfo
		}
	}

	private fun parseAccountInfo(payload: String) = JSON.nonstrict.decodeFromString(YouTubeAccountInfo.serializer(), payload)

	@Serializable
	class YouTubeAccountInfo(
			val title: String,
			val avatarUrl: String,
			val channelId: String
	)

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("youtube", extras = {
			val accounts = mutableListOf<Json>()

			for (tracked in trackedYouTubeAccounts) {
				accounts.add(
						json(
								"channel" to tracked.channelId.toString(),
								"youTubeChannelId" to tracked.youTubeChannelId,
								"message" to tracked.message
						)
				)
			}

			it["accounts"] = accounts
		})
	}
}