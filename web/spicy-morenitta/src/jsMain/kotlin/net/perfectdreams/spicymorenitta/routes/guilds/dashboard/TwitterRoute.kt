@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
import net.perfectdreams.loritta.serializable.TrackedTwitterAccount
import net.perfectdreams.loritta.common.utils.placeholders.Placeholders
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

class TwitterRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/twitter") {
	companion object {
		private const val LOCALE_PREFIX = "modules.twitter"
	}

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val textChannels: List<ServerConfig.TextChannel>,
			val trackedTwitterAccounts: Array<TrackedTwitterAccount>
	)

	val trackedTwitterAccounts = mutableListOf<TrackedTwitterAccount>()
	val cachedUsersById = mutableMapOf<Long, TwitterAccountInfo>()
	val cachedUsersByScreenName = mutableMapOf<String, TwitterAccountInfo>()

	override fun onUnload() {
		trackedTwitterAccounts.clear()
		cachedUsersById.clear()
		cachedUsersByScreenName.clear()
	}

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "activekeys", "twitter", "textchannels")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val stuff = document.select<HTMLDivElement>("#level-stuff")

			stuff.append {
				div(classes = "tracked-twitter-accounts") {}

				hr {}
			}

			val addEntryButton = document.select<HTMLButtonElement>("#add-new-entry")
			addEntryButton.onClick {
				val premiumPlan = guild.activeDonationKeys.getPlan()

				if (trackedTwitterAccounts.size >= premiumPlan.maxTwitterAccounts) {
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

				editTrackedTwitterAccount(
						guild,
						null,
						TrackedTwitterAccount(
								-1L,
								-1L,
								"{link}"
						)
				)
			}

			trackedTwitterAccounts.addAll(guild.trackedTwitterAccounts)

			updateTrackedTwitterAccountsList(guild)
		}
	}

	private fun updateTrackedTwitterAccountsList(guild: PartialGuildConfiguration) {
		val trackedDiv = document.select<HTMLDivElement>(".tracked-twitter-accounts")

		trackedDiv.clear()

		trackedDiv.append {
			if (trackedTwitterAccounts.isEmpty()) {
				listIsEmptySection()
			} else {
				for (account in trackedTwitterAccounts) {
					createTrackedTwitterAccountEntry(guild, account)
				}
			}
		}
	}

	fun TagConsumer<HTMLElement>.createTrackedTwitterAccountEntry(guild: PartialGuildConfiguration, trackedTwitterAccount: TrackedTwitterAccount) {
		this.div(classes = "discord-generic-entry timer-entry") {
			attributes["data-twitter-account"] = trackedTwitterAccount.twitterAccountId.toString()

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
							trackedTwitterAccounts.remove(trackedTwitterAccount)
							updateTrackedTwitterAccountsList(guild)
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
			val accountInfo = loadAccountInfoFromUserId(trackedTwitterAccount.twitterAccountId)
			info("Loading info for account ${trackedTwitterAccount.twitterAccountId}...")

			val trackedDiv = document.select<HTMLDivElement>("[data-twitter-account='${trackedTwitterAccount.twitterAccountId}']")

			if (accountInfo == null) {
				trackedTwitterAccounts.remove(trackedTwitterAccount)
				updateTrackedTwitterAccountsList(guild)
			} else {
				val currentChannel = guild.textChannels.firstOrNull { it.id == trackedTwitterAccount.channelId }

				val channelName = currentChannel?.let { "#${it.name}" } ?: "???"

				trackedDiv.select<HTMLImageElement>(".amino-small-image")
						.src = accountInfo.avatarUrl

				trackedDiv.select<HTMLDivElement>(".entry-title")
						.innerText = "${accountInfo.name} (@${accountInfo.screenName})"

				trackedDiv.select<HTMLDivElement>(".toggleSubText")
						.innerText = channelName

				trackedDiv.select<HTMLDivElement>(".edit-button")
						.onClick {
							editTrackedTwitterAccount(guild, accountInfo, trackedTwitterAccount)
						}

				info("yey")
			}
		}
	}

	private fun editTrackedTwitterAccount(guild: PartialGuildConfiguration, accountInfo: TwitterAccountInfo?, trackedTwitterAccount: TrackedTwitterAccount) {
		val modal = TingleModal(
			jsObject<TingleOptions> {
				footer = true
				cssClass = arrayOf("tingle-modal--overflow")
				closeMethods = arrayOf()
			}
		)

		modal.addFooterBtn("Salvar", "button-discord button-discord-info pure-button button-discord-modal") {
			// Iremos salvar a conta atual, aplicando as mudanças realizadas modal.close()
			var position = trackedTwitterAccounts.size

			if (trackedTwitterAccounts.contains(trackedTwitterAccount)) { // A conta atual já existe...
				// Caso já exista, vamos remover e recolocar na posição certa
				position = trackedTwitterAccounts.indexOf(trackedTwitterAccount)
				if (position == -1)
					position = trackedTwitterAccounts.size
				trackedTwitterAccounts.remove(trackedTwitterAccount)
			}

			val channelId = visibleModal.select<HTMLInputElement>(".choose-channel")
					.value

			val text = visibleModal.select<HTMLInputElement>(".choose-text")
					.value

			debug("Adding ${visibleModal.getAttribute("data-twitter-account-id")} account to the tracked twitter accounts list...")
			val account = TrackedTwitterAccount(
					channelId.toLong(),
					visibleModal.getAttribute("data-twitter-account-id")?.toLong() ?: 0L,
					text
			)

			trackedTwitterAccounts.add(position, account)

			modal.close()

			updateTrackedTwitterAccountsList(guild)
		}

		modal.addFooterBtn("Cancelar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.setContent(
				createHTML().div {
					div(classes = "category-name") {
						+ "Conta do Twitter"
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

							input(classes = "twitter-account") {
								if (accountInfo != null) {
									value = "@${accountInfo.screenName}"
								}

								placeholder = "@LorittaBot"
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

											if (channel.id == trackedTwitterAccount.channelId) {
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

									+trackedTwitterAccount.message
								}
							}
						}
					}
				}
		)
		modal.open()
		modal.trackOverflowChanges(m)

		fun processAccountInfo(accountInfo: TwitterAccountInfo) {
			visibleModal.select<HTMLDivElement>(".account-config")
					.removeClass("blurSection")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.name

			visibleModal.select<HTMLImageElement>(".account-icon")
					.src = accountInfo.avatarUrl
					.replace("_normal", "_400x400")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.name

			visibleModal.setAttribute("data-twitter-account-id", accountInfo.id.toString())
		}

		val twitterAccountInput = visibleModal.select<HTMLInputElement>(".twitter-account")
		twitterAccountInput.delayedTyping(m, 2_500, {
			info("Writing something, blurring content...")
			visibleModal.select<HTMLDivElement>(".account-config")
					.addClass("blurSection")
		}) {
			info("Finished typing! Loading data...")

			val screenName = twitterAccountInput.value
					.removePrefix("https://twitter.com/")
					.removePrefix("http://twitter.com/")
					.removePrefix("@")
					.removeSuffix("/")

			m.launch {
				val accountInfo = loadAccountInfoFromScreenName(screenName)

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
								"https://twitter.com/LorittaBot/status/1112093554174763008",
								locale["${LOCALE_PREFIX}.tweetLink"],
								RenderType.TEXT,
								false
						)
				),
				showTemplates = false
		)
	}

	private suspend fun loadAccountInfoFromUserId(userId: Long): TwitterAccountInfo? {
		info("Loading info for account ${userId}...")

		if (cachedUsersById.containsKey(userId))
			return cachedUsersById[userId]

		val response = http.get("${window.location.origin}/api/v1/twitter/users/show") {
			parameter("userId", userId)
		}

		val statusCode = response.status

		if (statusCode != HttpStatusCode.OK) {
			warn("Status Code is $statusCode, oof")
			return null
		} else {
			val text = response.bodyAsText()
			val accountInfo = parseAccountInfo(text)
			cachedUsersById[userId] = accountInfo
			cachedUsersByScreenName[accountInfo.screenName] = accountInfo
			return accountInfo
		}
	}

	private suspend fun loadAccountInfoFromScreenName(screenName: String): TwitterAccountInfo? {
		info("Loading info for account @${screenName}...")

		if (cachedUsersByScreenName.containsKey(screenName))
			return cachedUsersByScreenName[screenName]

		val response = http.get("${window.location.origin}/api/v1/twitter/users/show") {
			parameter("screenName", screenName)
		}

		val statusCode = response.status

		if (statusCode != HttpStatusCode.OK) {
			warn("Status Code is $statusCode, oof")
			return null
		} else {
			val text = response.bodyAsText()
			val accountInfo = parseAccountInfo(text)
			cachedUsersById[accountInfo.id] = accountInfo
			cachedUsersByScreenName[screenName] = accountInfo
			return accountInfo
		}
	}

	private fun parseAccountInfo(payload: String) = JSON.nonstrict.decodeFromString(TwitterAccountInfo.serializer(), payload)

	@Serializable
	class TwitterAccountInfo(
			val id: Long,
			val name: String,
			val screenName: String,
			val avatarUrl: String
	)

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("twitter", extras = {
			val accounts = mutableListOf<Json>()

			for (tracked in trackedTwitterAccounts) {
				accounts.add(
						json(
								"channelId" to tracked.channelId.toString(),
								"twitterAccountId" to tracked.twitterAccountId.toString(),
								"message" to tracked.message
						)
				)
			}

			it["accounts"] = accounts
		})
	}
}