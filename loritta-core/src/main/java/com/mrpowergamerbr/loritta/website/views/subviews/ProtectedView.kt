package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.tables.BlacklistedGuilds
import net.perfectdreams.loritta.utils.DiscordUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import java.util.*

abstract class ProtectedView : AbstractView() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		val hostHeader = req.header("Host").valueOrNull() ?: return false

		if (path.startsWith("/dashboard")) {
			val state = req.param("state")
			val guildId = req.param("guild_id")

			// Caso o usuário utilizou o invite link que adiciona a Lori no servidor, terá o parâmetro "guild_id" na URL
			// Se o parâmetro exista, vamos redirecionar!

			if (!req.param("code").isSet) {
				if (!req.session().get("discordAuth").isSet) {
					if (req.header("User-Agent").valueOrNull() == Constants.DISCORD_CRAWLER_USER_AGENT) {
						res.send(WebsiteUtils.getDiscordCrawlerAuthenticationPage())
					} else {
						val state = JsonObject()
						state["redirectUrl"] = "https://$hostHeader" + req.path()
						res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}")
					}
					return false
				}
			} else {
				val code = req.param("code").value()

				val storedUserIdentification = variables["userIdentification"] as SimpleUserIdentification?

				val userIdentification = if (code == "from_master" && storedUserIdentification != null) {
					// Veio do master cluster, vamos apenas tentar autenticar com os dados existentes!
					storedUserIdentification
				} else {
					val auth = TemmieDiscordAuth(code, "https://$hostHeader/dashboard", loritta.discordConfig.discord.clientId, loritta.discordConfig.discord.clientSecret).apply {
						debug = false
					}

					auth.doTokenExchange()
					val userIdentification = auth.getUserIdentification()

					req.session()["discordAuth"] = GSON.toJson(auth)

					userIdentification
				}

				// Verificar se o usuário é (possivelmente) alguém que foi banido de usar a Loritta
				val trueIp = req.trueIp
				val dailiesWithSameIp = transaction(Databases.loritta) {
					Dailies.select {
						(Dailies.ip eq trueIp)
					}.toMutableList()
				}

				val userIds = dailiesWithSameIp.map { it[Dailies.id] }.distinct()

				val bannedProfiles = transaction(Databases.loritta) {
					Profiles.select { Profiles.id inList userIds and Profiles.isBanned }
							.toMutableList()
				}

				if (bannedProfiles.isNotEmpty())
					logger.warn { "User ${userIdentification.id} has banned accounts in ${trueIp}! IDs: ${bannedProfiles.joinToString(transform = { it[Profiles.id].toString() })}" }

				if (state.isSet) {
					// state = base 64 encoded JSON
					val decodedState = Base64.getDecoder().decode(state.value()).toString(Charsets.UTF_8)
					val jsonState = jsonParser.parse(decodedState).obj
					val redirectUrl = jsonState["redirectUrl"].nullString

					if (redirectUrl != null) {
						res.redirect(redirectUrl)
						return true
					}
				}

				if (guildId.isSet) {
					val isRedirectedFromMaster = req.param("from_master")

					if (!isRedirectedFromMaster.isSet) {
						val cluster = DiscordUtils.getLorittaClusterForGuildId(guildId.value().toLong())

						if (cluster.getUrl() != hostHeader) {
							logger.info { "Received guild $guildId via OAuth2 scope, but the guild isn't in this cluster! Redirecting to where the user should be... $cluster" }

							// Vamos redirecionar!
							res.redirect("https://${cluster.getUrl()}/dashboard?guild_id=${guildId.value()}&code=from_master")
							return true
						}
					}

					logger.info { "Received guild $guildId via OAuth2 scope, sending DM to the guild owner..." }
					var guildFound = false
					var tries = 0
					val maxGuildTries = loritta.config.loritta.website.maxGuildTries

					while (!guildFound && maxGuildTries > tries) {
						val guild = lorittaShards.getGuildById(guildId.value())

						if (guild != null) {
							logger.info { "Guild ${guild} was successfully found after $tries tries! Yay!!" }

							val serverConfig = loritta.getServerConfigForGuild(guild.id)

							// Agora nós iremos pegar o locale do servidor
							val locale = loritta.getLegacyLocaleById(serverConfig.localeId)

							val userId = userIdentification.id

							val user = runBlocking { lorittaShards.retrieveUserById(userId) }

							if (user != null) {
								val member = guild.getMember(user)

								if (member != null) {
									// E, se o membro não for um bot e possui permissão de gerenciar o servidor ou permissão de administrador...
									if (!user.isBot && (member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR))) {
										// Verificar coisas antes de adicionar a Lori
										val blacklisted = transaction(Databases.loritta) {
											BlacklistedGuilds.select {
												BlacklistedGuilds.id eq guild.idLong
											}.firstOrNull()
										}

										if (blacklisted != null) {
											val blacklistedReason = blacklisted[BlacklistedGuilds.reason]

											// Envie via DM uma mensagem falando sobre o motivo do ban
											val message = locale["LORITTA_BlacklistedServer", blacklistedReason]

											user.openPrivateChannel().queue {
												it.sendMessage(message).queue({
													guild.leave().queue()
												}, {
													guild.leave().queue()
												})
											}
											return false
										}

										val profile = loritta.getOrCreateLorittaProfile(guild.owner!!.user.id)
										if (profile.isBanned) { // Dono blacklisted
											// Envie via DM uma mensagem falando sobre a Loritta!
											val message = locale["LORITTA_OwnerLorittaBanned", guild.owner?.user?.asMention, profile.bannedReason
													?: "???"]

											user.openPrivateChannel().queue {
												it.sendMessage(message).queue({
													guild.leave().queue()
												}, {
													guild.leave().queue()
												})
											}
											return false
										}

										// Envie via DM uma mensagem falando sobre a Loritta!
										val message = locale["LORITTA_ADDED_ON_SERVER", user.asMention, guild.name, loritta.instanceConfig.loritta.website.url, locale["LORITTA_SupportServerInvite"], loritta.legacyCommandManager.commandMap.size + loritta.commandManager.commands.size, "${loritta.instanceConfig.loritta.website.url}donate"]

										user.openPrivateChannel().queue {
											it.sendMessage(message).queue()
										}
									}
								}
							}
							guildFound = true // Servidor detectado, saia do loop!
						} else {
							tries++
							logger.warn { "Received guild $guildId via OAuth2 scope, but I'm not in that guild yet! Waiting for 1s... Tries: ${tries}" }
							Thread.sleep(1_000)
						}
					}

					if (tries == maxGuildTries) {
						// oof
						logger.warn { "Received guild $guildId via OAuth2 scope, we tried ${maxGuildTries} times, but I'm not in that guild yet! Telling the user about the issue..." }

						res.send("""
							|<p>Parece que você tentou me adicionar no seu servidor, mas mesmo assim eu não estou nele!</p>
							|<ul>
							|<li>Tente me readicionar, as vezes isto acontece devido a um delay entre o tempo até o Discord atualizar os servidores que eu estou. <a href="https://loritta.website/dashboard">https://loritta.website/dashboard</a></li>
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
						return true
					}

					res.redirect("https://$hostHeader/dashboard/configure/${guildId.value()}")
					return true
				}

				res.redirect("https://$hostHeader/dashboard") // Redirecionar para a dashboard, mesmo que nós já estejamos lá... (remove o "code" da URL)
			}
			return true
		}
		return false
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		if (req.header("User-Agent").valueOrNull() == Constants.DISCORD_CRAWLER_USER_AGENT)
			return WebsiteUtils.getDiscordCrawlerAuthenticationPage()

		if (!req.session().isSet("discordAuth")) { // Caso discordAuth não exista, vamos redirecionar para a tela de autenticação
			res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl)
			return "Redirecionando..."
		}

		val discordAuth = GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
		try {
			discordAuth.isReady(true)
		} catch (e: Exception) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl)
			return "Redirecionando..."
		}
		variables["discordAuth"] = discordAuth
		return try {
			renderProtected(req, res, path, variables, discordAuth)
		} catch (e: TemmieDiscordAuth.TokenExchangeException) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl)
			"Redirecionando..."
		} catch (e: TemmieDiscordAuth.UnauthorizedException) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl)
			"Redirecionando..."
		}
	}

	abstract fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String
}