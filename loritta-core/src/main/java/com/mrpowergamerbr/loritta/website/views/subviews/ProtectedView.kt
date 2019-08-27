package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import org.jooby.Request
import org.jooby.Response
import java.util.*

abstract class ProtectedView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		val hostHeader = req.header("Host").valueOrNull() ?: return false

		if (path.startsWith("/dashboard")) {
			val state = req.param("state")
			if (!req.param("code").isSet) {
				if (!req.session().get("discordAuth").isSet) {
					if (req.header("User-Agent").valueOrNull() == Constants.DISCORD_CRAWLER_USER_AGENT) {
						res.send(WebsiteUtils.getDiscordCrawlerAuthenticationPage())
					} else {
						val state = JsonObject()
						state["redirectUrl"] = "https://$hostHeader" + req.path()
						res.redirect(loritta.discordConfig.discord.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}")
					}
					return false
				}
			} else {
				val code = req.param("code").value()
				val auth = TemmieDiscordAuth(code, "https://$hostHeader/dashboard", loritta.discordConfig.discord.clientId, loritta.discordConfig.discord.clientSecret).apply {
					debug = false
				}
				auth.doTokenExchange()

				req.session()["discordAuth"] = GSON.toJson(auth)
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

				// Caso o usuário utilizou o invite link que adiciona a Lori no servidor, terá o parâmetro "guild_id" na URL
				// Se o parâmetro exista, redirecione automaticamente para a tela de configuração da Lori
				val guildId = req.param("guild_id")
				if (guildId.isSet) {
					val guild = lorittaShards.getGuildById(guildId.value())

					if (guild != null) {
						val serverConfig = loritta.getServerConfigForGuild(guild.id)

						// Agora nós iremos pegar o locale do servidor
						val locale = loritta.getLegacyLocaleById(serverConfig.localeId)

						val userId = auth.getUserIdentification().id

						val user = runBlocking { lorittaShards.retrieveUserById(userId) }

						if (user != null) {
							val member = guild.getMember(user)

							if (member != null) {
								// E, se o membro não for um bot e possui permissão de gerenciar o servidor ou permissão de administrador...
								if (!user.isBot && (member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR))) {
									// Verificar coisas antes de adicionar a Lori
									val blacklistedReason = loritta.blacklistedServers.entries.firstOrNull { guild.id == it.key }?.value
									if (blacklistedReason != null) { // Servidor blacklisted
										// Envie via DM uma mensagem falando sobre a Loritta!
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
										val message = locale["LORITTA_OwnerLorittaBanned", guild.owner?.user?.asMention, profile.bannedReason ?: "???"]

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
									val message = locale["LORITTA_ADDED_ON_SERVER", user.asMention, guild.name, loritta.config.loritta.website.url, locale["LORITTA_SupportServerInvite"], loritta.legacyCommandManager.commandMap.size + loritta.commandManager.commands.size, "${loritta.config.loritta.website.url}donate"]

									user.openPrivateChannel().queue {
										it.sendMessage(message).queue()
									}
								}
							}
						}
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
			res.redirect(loritta.discordConfig.discord.authorizationUrl)
			return "Redirecionando..."
		}

		val discordAuth = GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
		try {
			discordAuth.isReady(true)
		} catch (e: Exception) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordConfig.discord.authorizationUrl)
			return "Redirecionando..."
		}
		variables["discordAuth"] = discordAuth
		return try {
			renderProtected(req, res, path, variables, discordAuth)
		} catch (e: TemmieDiscordAuth.TokenExchangeException) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordConfig.discord.authorizationUrl)
			"Redirecionando..."
		} catch (e: TemmieDiscordAuth.UnauthorizedException) {
			req.session().unset("discordAuth")
			res.redirect(loritta.discordConfig.discord.authorizationUrl)
			"Redirecionando..."
		}
	}

	abstract fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String
}