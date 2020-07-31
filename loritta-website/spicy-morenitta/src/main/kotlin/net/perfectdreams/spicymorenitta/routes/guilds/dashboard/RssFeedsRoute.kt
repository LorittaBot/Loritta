package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import io.ktor.client.request.get
import io.ktor.client.request.url
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.createHTML
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parseList
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
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.dom.removeClass
import kotlin.js.Json
import kotlin.js.json

class RssFeedsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/rss-feeds") {
	companion object {
		private const val LOCALE_PREFIX = "modules.rssfeeds"
	}

	override val keepLoadingScreen: Boolean
		get() = true

	val trackedRssFeeds = mutableListOf<ServerConfig.TrackedRssFeed>()
	val defaultFeedEntries = mutableListOf<DefaultRssFeedEntry>()

	override fun onUnload() {
		trackedRssFeeds.clear()
		defaultFeedEntries.clear()
	}

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val result = http.get<String> {
				url("${window.location.origin}/api/v1/rss/default")
			}

			defaultFeedEntries.addAll(JSON.nonstrict.parseList(result))

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val guild = DashboardUtils.retrieveGuildConfiguration(call.parameters["guildid"]!!)

			switchContentAndFixLeftSidebarScroll(call)

			val stuff = document.select<HTMLDivElement>("#level-stuff")

			stuff.append {
				div(classes = "tracked-twitter-accounts") {}

				hr {}
			}

			val addEntryButton = document.select<HTMLButtonElement>("#add-new-entry")
			addEntryButton.onClick {
				editTrackedRssFeed(
						guild,
						ServerConfig.TrackedRssFeed(
								-1L,
								"-1",
								"{link}"
						)
				)
			}

			trackedRssFeeds.addAll(guild.trackedRssFeeds)

			updateTrackedRssFeedsList(guild)
		}
	}

	@ImplicitReflectionSerializer
	private fun updateTrackedRssFeedsList(guild: ServerConfig.Guild) {
		val trackedDiv = document.select<HTMLDivElement>(".tracked-twitter-accounts")

		trackedDiv.clear()

		trackedDiv.append {
			if (trackedRssFeeds.isEmpty()) {
				listIsEmptySection()
			} else {
				for (account in trackedRssFeeds) {
					createTrackedRssFeedsEntry(guild, account)
				}
			}
		}
	}

	@ImplicitReflectionSerializer
	fun TagConsumer<HTMLElement>.createTrackedRssFeedsEntry(guild: ServerConfig.Guild, trackedTwitterAccount: ServerConfig.TrackedRssFeed) {
		this.div(classes = "discord-generic-entry timer-entry") {
			// attributes["data-twitter-account"] = trackedTwitterAccount.twitterAccountId.toString()

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
							+ trackedTwitterAccount.feedUrl
						}
						div(classes = "amino-title toggleSubText") {
							val currentChannel = guild.textChannels.firstOrNull { it.id == trackedTwitterAccount.channelId }

							val channelName = currentChannel?.let { "#${it.name}" } ?: "???"

							+ channelName
						}
					}
				}
				div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-content") {
					button(classes = "button-discord button-discord-edit pure-button delete-button") {
						style = "margin-right: 8px; min-width: 0px;"

						onClickFunction = {
							trackedRssFeeds.remove(trackedTwitterAccount)
							updateTrackedRssFeedsList(guild)
						}

						i(classes = "fas fa-trash") {}
					}
					button(classes = "button-discord button-discord-edit pure-button edit-button") {
						+"Editar"

						onClickFunction = {
							editTrackedRssFeed(guild, trackedTwitterAccount)
						}
					}
				}
			}
		}
	}

	@ImplicitReflectionSerializer
	private fun editTrackedRssFeed(guild: ServerConfig.Guild, trackedTwitterAccount: ServerConfig.TrackedRssFeed) {
		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.addFooterBtn("Salvar", "button-discord button-discord-info pure-button button-discord-modal") {
			// Iremos salvar a conta atual, aplicando as mudanças realizadas modal.close()
			var position = trackedRssFeeds.size

			if (trackedRssFeeds.contains(trackedTwitterAccount)) { // A conta atual já existe...
				// Caso já exista, vamos remover e recolocar na posição certa
				position = trackedRssFeeds.indexOf(trackedTwitterAccount)
				if (position == -1)
					position = trackedRssFeeds.size
				trackedRssFeeds.remove(trackedTwitterAccount)
			}

			val channelId = visibleModal.select<HTMLInputElement>(".choose-channel")
					.value

			val text = visibleModal.select<HTMLInputElement>(".choose-text")
					.value

			debug("Adding ${visibleModal.getAttribute("data-twitter-account-id")} account to the tracked twitter accounts list...")

			val selectedType = visibleModal.select<HTMLSelectElement>(".select-feed-type").value

			val feedUrlDiv = visibleModal.select<HTMLInputElement>(".feed-url")

			val feedUrl = if (selectedType == "my-own-feed") {
				feedUrlDiv.value
			} else {
				"{${defaultFeedEntries.first { it.feedId == selectedType }.feedId}}"
			}

			val account = ServerConfig.TrackedRssFeed(
					channelId.toLong(),
					feedUrl,
					text
			)

			trackedRssFeeds.add(position, account)

			modal.close()

			updateTrackedRssFeedsList(guild)
		}

		modal.addFooterBtn("Cancelar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.setContent(
				createHTML().div {
					div(classes = "category-name") {
						+ "Feed RSS"
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
								+ locale["$LOCALE_PREFIX.feedType"]
							}

							select(classes = "select-feed-type") {
								option {}

								option {
									value = "my-own-feed"

									+ "Utilizar minha própria Feed"
								}

								for (defaultEntry in defaultFeedEntries) {
									option {
										value = defaultEntry.feedId

										+ defaultEntry.feedId
									}
								}
							}

							h5(classes = "section-title") {
								+ locale["$LOCALE_PREFIX.feedUrl"]
							}

							input(classes = "feed-url") {
								disabled = true
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
		// modal.trackOverflowChanges(m)

		visibleModal.select<HTMLSelectElement>(".select-feed-type")
				.onchange = {
			val selectedType = visibleModal.select<HTMLSelectElement>(".select-feed-type").value

			val feedUrlDiv = visibleModal.select<HTMLInputElement>(".feed-url")

			if (selectedType == "my-own-feed") {
				feedUrlDiv.disabled = false
			} else {
				feedUrlDiv.disabled = true
				feedUrlDiv.value = defaultFeedEntries.first { it.feedId == selectedType }.feedUrl
			}

			visibleModal.select<HTMLDivElement>(".account-config")
					.removeClass("blurSection")

			asDynamic()
		}

		/* val twitterAccountInput = visibleModal.select<HTMLInputElement>(".twitter-account")
		twitterAccountInput.delayedTyping(m, 2_500, {
			info("Writing something, blurring content...")
			visibleModal.select<HTMLDivElement>(".account-config")
					.addClass("blurSection")
		}) {
			info("Finished typing! Loading data...")
		} */

		LoriDashboard.configureTextArea(
				jq(".tingle-modal--visible .choose-text"),
				true,
				null,
				false,
				null,
				true,
				listOf(), /* Placeholders.DEFAULT_PLACEHOLDERS .toMutableMap().apply {
					put("link", "Link do Tweet")
				} ,
				customTokens = mapOf(
						"link" to "https://twitter.com/LorittaBot/status/1112093554174763008"
				), */
				showTemplates = false
		)
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("rss_feeds", extras = {
			val accounts = mutableListOf<Json>()

			for (tracked in trackedRssFeeds) {
				accounts.add(
						json(
								"channelId" to tracked.channelId.toString(),
								"feedUrl" to tracked.feedUrl,
								"message" to tracked.message
						)
				)
			}

			it["rssFeeds"] = accounts
		})
	}

	@Serializable
	class DefaultRssFeedEntry(
			val feedId: String,
			val feedUrl: String
	)
}