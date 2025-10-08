package net.perfectdreams.loritta.morenitta.website.routes

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.delay
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlacklistedGuilds
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.*
import net.perfectdreams.loritta.morenitta.website.views.UserBannedView
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.temmiewebsession.LorittaTemmieDiscordAuth
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll
import java.util.*

abstract class RequiresDiscordLoginLocalizedRoute(loritta: LorittaBot, path: String) : LocalizedRoute(loritta, path) {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification)

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		if (call.request.path().endsWith("/dashboard")) {
            val hostHeader: String
            val scheme: String

			if (call.request.header("Dashboard-Proxy") == "true") {
                hostHeader = call.request.headers["X-Forwarded-Host"]!!
                scheme = call.request.headers["X-Forwarded-Proto"]!!
            } else {
                hostHeader = call.request.hostFromHeader()
                scheme = LorittaWebsite.WEBSITE_URL.split(":").first()
            }

            logger.info { "Host Header is $hostHeader and Scheme is $scheme" }
			val state = call.parameters["state"]
			val guildId = call.parameters["guild_id"]
			val code = call.parameters["code"]

			println("Dashboard Auth Route")
			val session: LorittaJsonWebSession = call.sessions.get<LorittaJsonWebSession>() ?: LorittaJsonWebSession.empty()
			val discordAuth = session.getDiscordAuth(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
            logger.info { "Session is $session, Discord Auth is $discordAuth" }

			// Caso o usuário utilizou o invite link que adiciona a Lori no servidor, terá o parâmetro "guild_id" na URL
			// Se o parâmetro exista, vamos redirecionar!
			if (code == null) {
				if (discordAuth == null) {
					if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
						call.respondHtml(WebsiteUtils.getDiscordCrawlerAuthenticationPage(loritta))
					} else {
						redirect(LorittaDiscordOAuth2AuthorizeScopeURL(loritta,  "$scheme://$hostHeader" + call.request.path()).toString(), false)
					}
				}
			} else {
				val storedUserIdentification = session.getUserIdentification(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)

				val userIdentification = if (code == "from_master") {
					// Veio do master cluster, vamos apenas tentar autenticar com os dados existentes!
					storedUserIdentification ?: run {
						// Okay... mas e se for nulo? Veio do master mas não tem session cache? Como pode??
						// Iremos apenas pedir para o usuário reautenticar, porque alguma coisa deu super errado!
						redirect(LorittaDiscordOAuth2AuthorizeScopeURL(loritta, "$scheme://$hostHeader" + call.request.path()).toString(), false)
					}
				} else {
					val auth = LorittaTemmieDiscordAuth(
						call,
						loritta.config.loritta.discord.applicationId.toString(),
						loritta.config.loritta.discord.clientSecret,
						code,
						"$scheme://$hostHeader/dashboard",
						listOf("identify", "guilds", "email")
					)

					auth.doTokenExchange()
					val userIdentification = auth.getUserIdentification()
                    logger.info { "Successfully authenticated! ${userIdentification.username} (${userIdentification.id})" }
					val forCache = userIdentification.toWebSessionIdentification()
					call.sessions.set(
						session.copy(
							base64CachedIdentification = Base64.getEncoder().encode(forCache.toJson().toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8),
							base64StoredDiscordAuthTokens = Base64.getEncoder().encode(auth.toJson().toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
						)
					)

					forCache
				}

				// Verificar se o usuário é (possivelmente) alguém que foi banido de usar a Loritta
				/* val trueIp = call.request.trueIp
				val dailiesWithSameIp = loritta.newSuspendedTransaction {
					Dailies.selectAll().where {
						(Dailies.ip eq trueIp)
					}.toMutableList()
				}

				val userIds = dailiesWithSameIp.map { it[Dailies.id] }.distinct()

				val bannedProfiles = loritta.newSuspendedTransaction {
					Profiles.selectAll().where { Profiles.id inList userIds and Profiles.isBanned }
							.toMutableList()
				}

				if (bannedProfiles.isNotEmpty())
					logger.warn { "User ${userIdentification.id} has banned accounts in ${trueIp}! IDs: ${bannedProfiles.joinToString(transform = { it[Profiles.id].toString() })}" } */

				if (state != null) {
					// state = base 64 encoded JSON
					val decodedState = Base64.getDecoder().decode(state).toString(Charsets.UTF_8)
					val jsonState = JsonParser.parseString(decodedState).obj
					val redirectUrl = jsonState["redirectUrl"].nullString

					if (redirectUrl != null) {
						// Check if we are redirecting to Loritta's trusted URLs
						val lorittaDomain = loritta.connectionManager.getDomainFromUrl(loritta.config.loritta.website.url)
						val redirectDomain = loritta.connectionManager.getDomainFromUrl(redirectUrl)

						if (lorittaDomain == redirectDomain) {
                            logger.info { "Redirecting to $redirectUrl due to state..." }
                            redirect(redirectUrl, false)
                        } else
							logger.warn { "Someone tried to make me redirect to somewhere that isn't my website domain! Tried to redirect to $redirectDomain" }
					}
				}

				if (guildId != null) {
					if (code != "from_master") {
						val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, guildId.toLong())

						if (cluster.getUrl(loritta) != hostHeader) {
							logger.info { "Received guild $guildId via OAuth2 scope, but the guild isn't in this cluster! Redirecting to where the user should be... $cluster" }

							// Vamos redirecionar!
							redirect("${cluster.getUrl(loritta)}/dashboard?guild_id=${guildId}&code=from_master", true)
						}
					}

					logger.info { "Received guild $guildId via OAuth2 scope, sending DM to the guild owner..." }
					var guildFound = false
					var tries = 0
					val maxGuildTries = loritta.config.loritta.website.maxGuildTries

					while (!guildFound && maxGuildTries > tries) {
						val guild = loritta.lorittaShards.getGuildById(guildId)

						if (guild != null) {
							logger.info { "Guild ${guild} was successfully found after $tries tries! Yay!!" }

							val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)

							// Now we are going to save the server's new locale ID, based on the user's locale
							// This fixes issues because Discord doesn't provide the voice channel server anymore
							// (which, well, was already a huge workaround anyway)
							loritta.newSuspendedTransaction {
								serverConfig.localeId = locale.id
							}

							val userId = userIdentification.id

							val user = loritta.lorittaShards.retrieveUserById(userId)

							if (user != null) {
								val member = guild.getMember(user)

								if (member != null) {
									// E, se o membro não for um bot e possui permissão de gerenciar o servidor ou permissão de administrador...
									if (!user.isBot && (member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR))) {
										// Verificar coisas antes de adicionar a Lori
										val blacklisted = loritta.newSuspendedTransaction {
											BlacklistedGuilds.selectAll().where {
												BlacklistedGuilds.id eq guild.idLong
											}.firstOrNull()
										}

										if (blacklisted != null) {
											val blacklistedReason = blacklisted[BlacklistedGuilds.reason]

											// Envie via DM uma mensagem falando sobre o motivo do ban
											val message = locale.getList("website.router.blacklistedServer", blacklistedReason)

											loritta.getOrRetrievePrivateChannelForUser(user)
												.sendMessage(message.joinToString("\n")).queue({
													guild.leave().queue()
												}, {
													guild.leave().queue()
												})
											return
										}

										val guildOwner = guild.owner

										// Sometimes the guild owner can be null, that's why we need to check if it is null or not!
										if (guildOwner != null) {
											val profile = loritta.getLorittaProfile(guildOwner.user.id)
											val bannedState = profile?.getBannedState(loritta)
											if (bannedState != null) { // Dono blacklisted
												// Envie via DM uma mensagem falando sobre a Loritta!
												val message = locale.getList("website.router.ownerLorittaBanned", guild.owner?.user?.asMention, bannedState[BannedUsers.reason]
													?: "???").joinToString("\n")

												loritta.getOrRetrievePrivateChannelForUser(user)
													.sendMessage(message)
													.queue({
														guild.leave().queue()
													}, {
														guild.leave().queue()
													})
												return
											}

											// Envie via DM uma mensagem falando sobre a Loritta!
											val message = locale.getList(
												"website.router.addedOnServer",
												user.asMention,
												guild.name,
												loritta.config.loritta.website.url + "commands",
												loritta.config.loritta.website.url + "guild/${guild.id}/configure/",
												loritta.config.loritta.website.url + "guidelines",
												loritta.config.loritta.website.url + "donate",
												loritta.config.loritta.website.url + "support",
												Emotes.LORI_PAT,
												Emotes.LORI_NICE,
												Emotes.LORI_HEART,
												Emotes.LORI_COFFEE,
												Emotes.LORI_SMILE,
												Emotes.LORI_PRAY,
												Emotes.LORI_BAN_HAMMER,
												Emotes.LORI_RICH,
												Emotes.LORI_HEART1.toString() + Emotes.LORI_HEART2.toString()
											).joinToString("\n")

											loritta.getOrRetrievePrivateChannelForUser(user)
												.sendMessage(message)
												.queue()
										}
									}
								}
							}
							guildFound = true // Servidor detectado, saia do loop!
						} else {
							tries++
							logger.warn { "Received guild $guildId via OAuth2 scope, but I'm not in that guild yet! Waiting for 1s... Tries: ${tries}" }
							delay(1_000)
						}
					}

					if (tries == maxGuildTries) {
						// oof
						logger.warn { "Received guild $guildId via OAuth2 scope, we tried ${maxGuildTries} times, but I'm not in that guild yet! Telling the user about the issue..." }

						call.respondHtml(
							"""
							|<p>Parece que você tentou me adicionar no seu servidor, mas mesmo assim eu não estou nele!</p>
							|<ul>
							|<li>Tente me readicionar, as vezes isto acontece devido a um delay entre o tempo até o Discord atualizar os servidores que eu estou. <a href="$scheme://loritta.website/dashboard">$scheme://loritta.website/dashboard</a></li>
							|<li>
							|Verifique o registro de auditoria do seu servidor, alguns bots expulsam/banem ao adicionar novos bots. Caso isto tenha acontecido, expulse o bot que me puniu e me readicione!
							|<ul>
							|<li>
							|<b>Em vez de confiar em um bot para "proteger" o seu servidor:</b> Veja quem possui permissão de administrador ou de gerenciar servidores no seu servidor, eles são os únicos que conseguem adicionar bots no seu servidor. Existem boatos que existem "bugs que permitem adicionar bots sem permissão", mas isto é mentira.
							|</li>
							|</ul>
							|</li>
							|</ul>
							|<p>Desculpe pela inconveniência ;w;</p>
						""".trimMargin())
						return
					}

					redirect("$scheme://$hostHeader/guild/${guildId}/configure", false)
					return
				}

                val redirectUrl = "$scheme://$hostHeader/dashboard"
                logger.info { "Redirecting to $redirectUrl" }
				redirect(redirectUrl, false) // Redirecionar para a dashboard, mesmo que nós já estejamos lá... (remove o "code" da URL)
			}
		}

		var start = System.currentTimeMillis()
		val session = call.lorittaSession
		logger.info { "Time to get session: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val discordAuth = session.getDiscordAuth(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
		logger.info { "Time to get Discord Auth: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()
		val userIdentification = session.getUserIdentification(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
		logger.info { "Time to get User Identification: ${System.currentTimeMillis() - start}" }

		if (discordAuth == null || userIdentification == null) {
			onUnauthenticatedRequest(call, locale, i18nContext)
			return
		}

		val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
		val bannedState = profile.getBannedState(loritta)
		if (bannedState != null) {
			call.respondHtml(
				UserBannedView(
					loritta,
					i18nContext,
					locale,
					getPathWithoutLocale(call),
					profile,
					bannedState
				).generateHtml()
			)
			return
		}

		onAuthenticatedRequest(call, locale, i18nContext, discordAuth, userIdentification)
	}

	open suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		// redirect to authentication owo
		redirect(LorittaDiscordOAuth2AuthorizeScopeURL(loritta, LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + call.request.path()).toString(), false)
	}
}