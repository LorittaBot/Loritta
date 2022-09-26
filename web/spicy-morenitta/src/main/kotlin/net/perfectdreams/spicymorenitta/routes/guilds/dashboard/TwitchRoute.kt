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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.embededitor.data.crosswindow.Placeholder
import net.perfectdreams.loritta.embededitor.data.crosswindow.RenderType
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

class TwitchRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/twitch") {
	companion object {
		private const val LOCALE_PREFIX = "modules.twitch"
	}

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val textChannels: List<ServerConfig.TextChannel>,
			val trackedTwitchChannels: Array<ServerConfig.TrackedTwitchAccount>
	)

	val trackedTwitchAccounts = mutableListOf<ServerConfig.TrackedTwitchAccount>()
	val cachedChannelByUserId = mutableMapOf<Long, TwitchAccountInfo>()
	val cachedChannelByUserLogin = mutableMapOf<String, TwitchAccountInfo>()
	
	override fun onUnload() {
		trackedTwitchAccounts.clear()
		cachedChannelByUserId.clear()
		cachedChannelByUserLogin.clear()
	}

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "activekeys", "twitch", "textchannels")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val stuff = document.select<HTMLDivElement>("#level-stuff")

			stuff.append {
				div(classes = "tracked-twitch-accounts") {}

				hr {}
			}

			val addEntryButton = document.select<HTMLButtonElement>("#add-new-entry")
			addEntryButton.onClick {
				val premiumPlan = guild.activeDonationKeys.getPlan()

				if (trackedTwitchAccounts.size >= premiumPlan.maxTwitchChannels) {
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

				editTrackedTwitchAccount(
						guild,
						null,
						ServerConfig.TrackedTwitchAccount(
								-1L,
								-1L,
								"{link}"
						)
				)
			}

			trackedTwitchAccounts.addAll(guild.trackedTwitchChannels)

			updateTrackedTwitchAccountsList(guild)
		}
	}

	private fun updateTrackedTwitchAccountsList(guild: PartialGuildConfiguration) {
		val trackedDiv = document.select<HTMLDivElement>(".tracked-twitch-accounts")

		trackedDiv.clear()

		trackedDiv.append {
			if (trackedTwitchAccounts.isEmpty()) {
				listIsEmptySection()
			} else {
				for (account in trackedTwitchAccounts) {
					createTrackedTwitchAccountEntry(guild, account)
				}
			}
		}
	}

	fun TagConsumer<HTMLElement>.createTrackedTwitchAccountEntry(guild: PartialGuildConfiguration, trackedTwitchAccount: ServerConfig.TrackedTwitchAccount) {
		this.div(classes = "discord-generic-entry timer-entry") {
			attributes["data-twitch-account"] = trackedTwitchAccount.twitchUserId.toString()

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
							trackedTwitchAccounts.remove(trackedTwitchAccount)
							updateTrackedTwitchAccountsList(guild)
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
			val accountInfo = loadAccountInfoFromUserId(trackedTwitchAccount.twitchUserId)
			info("Loading info for account ${trackedTwitchAccount.twitchUserId}...")

			val trackedDiv = document.select<HTMLDivElement>("[data-twitch-account='${trackedTwitchAccount.twitchUserId}']")

			if (accountInfo == null) {
				trackedTwitchAccounts.remove(trackedTwitchAccount)
				updateTrackedTwitchAccountsList(guild)
			} else {
				val currentChannel = guild.textChannels.firstOrNull { it.id == trackedTwitchAccount.channelId }

				val channelName = currentChannel?.let { "#${it.name}" } ?: "???"

				trackedDiv.select<HTMLImageElement>(".amino-small-image")
						.src = accountInfo.profileImageUrl

				trackedDiv.select<HTMLDivElement>(".entry-title")
						.innerText = "${accountInfo.displayName} (${accountInfo.login})"

				trackedDiv.select<HTMLDivElement>(".toggleSubText")
						.innerText = channelName

				trackedDiv.select<HTMLDivElement>(".edit-button")
						.onClick {
							editTrackedTwitchAccount(guild, accountInfo, trackedTwitchAccount)
						}

				info("yey")
			}
		}
	}

	private fun editTrackedTwitchAccount(guild: PartialGuildConfiguration, accountInfo: TwitchAccountInfo?, trackedTwitchAccount: ServerConfig.TrackedTwitchAccount) {
		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.addFooterBtn("Salvar", "button-discord button-discord-info pure-button button-discord-modal") {
			// Iremos salvar a conta atual, aplicando as mudanças realizadas modal.close()
			var position = trackedTwitchAccounts.size

			if (trackedTwitchAccounts.contains(trackedTwitchAccount)) { // A conta atual já existe...
				// Caso já exista, vamos remover e recolocar na posição certa
				position = trackedTwitchAccounts.indexOf(trackedTwitchAccount)
				if (position == -1)
					position = trackedTwitchAccounts.size
				trackedTwitchAccounts.remove(trackedTwitchAccount)
			}

			val channelId = visibleModal.select<HTMLInputElement>(".choose-channel")
					.value

			val text = visibleModal.select<HTMLInputElement>(".choose-text")
					.value

			debug("Adding ${visibleModal.getAttribute("data-twitch-account-id")} account to the tracked Twitch accounts list...")
			val account = ServerConfig.TrackedTwitchAccount(
					channelId.toLong(),
					visibleModal.getAttribute("data-twitch-account-id")?.toLongOrNull() ?: -1L,
					text
			)

			trackedTwitchAccounts.add(position, account)

			modal.close()

			updateTrackedTwitchAccountsList(guild)
		}

		modal.addFooterBtn("Cancelar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.setContent(
				createHTML().div {
					div(classes = "category-name") {
						+ "Conta do Twitch"
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

							input(classes = "twitch-account") {
								if (accountInfo != null) {
									value = "https://www.twitch.com/channel/${accountInfo.login}"
								}
								placeholder = "https://www.twitch.tv/alanzoka"
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

											if (channel.id == trackedTwitchAccount.channelId) {
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

									+trackedTwitchAccount.message
								}
							}
						}
					}
				}
		)
		modal.open()
		modal.trackOverflowChanges(m)

		fun processAccountInfo(accountInfo: TwitchAccountInfo) {
			visibleModal.select<HTMLDivElement>(".account-config")
					.removeClass("blurSection")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.displayName

			visibleModal.select<HTMLImageElement>(".account-icon")
					.src = accountInfo.profileImageUrl
					.replace("_normal", "_400x400")

			visibleModal.select<HTMLDivElement>(".category-name")
					.innerText = accountInfo.displayName

			visibleModal.setAttribute("data-twitch-account-id", accountInfo.id.toString())
		}

		val youTubeAccountInput = visibleModal.select<HTMLInputElement>(".twitch-account")
		youTubeAccountInput.delayedTyping(m, 2_500, {
			info("Writing something, blurring content...")
			visibleModal.select<HTMLDivElement>(".account-config")
					.addClass("blurSection")
		}) {
			info("Finished typing! Loading data...")

			val screenName = youTubeAccountInput.value
					.split("/").last { it.isNotEmpty() }

			m.launch {
				val accountInfo = loadAccountInfoFromUserLogin(screenName)

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
								net.perfectdreams.loritta.legacy.utils.Placeholders.LINK.asKey,
								"https://twitch.tv/alanzoka",
								locale["${LOCALE_PREFIX}.channelLink"],
								RenderType.TEXT,
								false
						)
				),
				showTemplates = false
		)
	}

	private suspend fun loadAccountInfoFromUserId(userId: Long): TwitchAccountInfo? {
		info("Loading info for account ${userId}...")

		if (cachedChannelByUserId.containsKey(userId))
			return cachedChannelByUserId[userId]

		val response = http.get("${window.location.origin}/api/v1/twitch/channel") {
			parameter("id", userId)
		}

		val statusCode = response.status

		if (statusCode != HttpStatusCode.OK) {
			warn("Status Code is $statusCode, oof")
			return null
		} else {
			val text = response.bodyAsText()
			val accountInfo = parseAccountInfo(text)
			cachedChannelByUserId[userId] = accountInfo
			cachedChannelByUserLogin[accountInfo.login] = accountInfo
			return accountInfo
		}
	}

	private suspend fun loadAccountInfoFromUserLogin(userLogin: String): TwitchAccountInfo? {
		info("Loading info for account ${userLogin}...")

		if (cachedChannelByUserLogin.containsKey(userLogin))
			return cachedChannelByUserLogin[userLogin]

		val response = http.get("${window.location.origin}/api/v1/twitch/channel") {
			parameter("login", userLogin)
		}

		val statusCode = response.status

		if (statusCode != HttpStatusCode.OK) {
			warn("Status Code is $statusCode, oof")
			return null
		} else {
			val text = response.bodyAsText()
			val accountInfo = parseAccountInfo(text)
			cachedChannelByUserLogin[userLogin] = accountInfo
			cachedChannelByUserId[accountInfo.id] = accountInfo
			return accountInfo
		}
	}

	private fun parseAccountInfo(payload: String) = JSON.nonstrict.decodeFromString(TwitchAccountInfo.serializer(), payload)

	@Serializable
	class TwitchAccountInfo(
			val login: String,
			val id: Long,
			@SerialName("display_name")
			val displayName: String,
			@SerialName("profile_image_url")
			val profileImageUrl: String
	)

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("twitch", extras = {
			val accounts = mutableListOf<Json>()

			for (tracked in trackedTwitchAccounts) {
				accounts.add(
						json(
								"channel" to tracked.channelId.toString(),
								"twitchUserId" to tracked.twitchUserId.toString(),
								"message" to tracked.message
						)
				)
			}

			it["accounts"] = accounts
		})
	}
}