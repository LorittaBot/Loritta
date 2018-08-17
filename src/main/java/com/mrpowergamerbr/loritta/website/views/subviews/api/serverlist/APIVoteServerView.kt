package com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Role
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APIVoteServerView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/vote"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val recaptcha = req.param("recaptcha").value()
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val type = req.param("guildId").value()

		val serverConfig = loritta.serversColl.find(
				Filters.eq("_id", type)
		).firstOrNull()

		if (serverConfig == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guild = lorittaShards.getGuildById(type)!!
		if (guild.getMemberById(userIdentification.id) == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return payload.toString()
		}


		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${Loritta.config.recaptchaToken}&response=$recaptcha")
				.body()

		val jsonParser = jsonParser.parse(body).obj

		val success = jsonParser["success"].bool

		if (!success) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INVALID_CAPTCHA_RESPONSE
			return payload.toString()
		}

		val ips = req.header("X-Forwarded-For").value() // Cloudflare, Apache
		val ip = ips.split(", ")[0]

		// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
		// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
		val vote = serverConfig.serverListConfig.votes.lastOrNull {
			it.id == userIdentification.id || it.ip == ip
		}

		if (vote != null) {
			val votedAt = vote.votedAt

			val calendar = Calendar.getInstance()
			calendar.timeInMillis = votedAt
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.add(Calendar.DAY_OF_MONTH, 1)
			val tomorrow = calendar.timeInMillis

			val canVote = System.currentTimeMillis() > tomorrow

			if (!canVote) {
				val payload = JsonObject()
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				return payload.toString()
			}
		}

		val status = MiscUtils.verifyAccount(userIdentification, ip)

		if (!status.canAccess) {
			val payload = JsonObject()
			return when (status) {
				MiscUtils.AccountCheckResult.STOP_FORUM_SPAM,
				MiscUtils.AccountCheckResult.BAD_HOSTNAME,
				MiscUtils.AccountCheckResult.OVH_HOSTNAME -> {
					// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
					// 0 = Stop Forum Spam
					// 1 = Bad hostname
					// 2 = OVH IP
					payload["api:code"] = LoriWebCodes.BAD_IP
					payload["reason"] = when (status) {
						MiscUtils.AccountCheckResult.STOP_FORUM_SPAM -> 0
						MiscUtils.AccountCheckResult.BAD_HOSTNAME -> 1
						MiscUtils.AccountCheckResult.OVH_HOSTNAME -> 2
						else -> -1
					}
				}
				MiscUtils.AccountCheckResult.BAD_EMAIL -> {
					payload["api:code"] = LoriWebCodes.BAD_EMAIL

				}
				MiscUtils.AccountCheckResult.NOT_VERIFIED -> {
					payload["api:code"] = LoriWebCodes.NOT_VERIFIED
				}
				else -> throw RuntimeException("Missing !canAccess result! ${status.name}")
			}.toString()
		}

		val votedAt = System.currentTimeMillis()
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = votedAt
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val tomorrow = calendar.timeInMillis

		serverConfig.serverListConfig.votes.add(
				ServerListConfig.ServerVote(
						userIdentification.id,
						System.currentTimeMillis(),
						ip,
						userIdentification.email!!
				)
		)

		loritta save serverConfig

		val member = guild.getMemberById(userIdentification.id)

		if (serverConfig.serverListConfig.sendOnVote && serverConfig.serverListConfig.voteBroadcastChannelId != null && serverConfig.serverListConfig.voteBroadcastMessage != null) {
			val textChannel = guild.getTextChannelById(serverConfig.serverListConfig.voteBroadcastChannelId)

			if (textChannel != null) {
				val customTokens = mutableMapOf<String, String>(
						"vote-count" to serverConfig.serverListConfig.votes.count { it.id == member.user.id }.toString()
				)

				val message = MessageUtils.generateMessage(
						serverConfig.serverListConfig.voteBroadcastMessage!!,
						listOf(guild, member),
						guild,
						customTokens
				)

				if (message != null)
					textChannel.sendMessage(message).complete()
			}
		}

		val voteCount = serverConfig.serverListConfig.votes.count { it.id == userIdentification.id }
		val roleIds = serverConfig.autoroleConfig.rolesVoteRewards.filter { it.voteCount >= voteCount }.flatMap { it.roles }

		val roles = mutableListOf<Role>()

		roleIds.forEach { // E pegar a role dependendo do ID!
			try {
				val role = guild.getRoleById(it)

				if (role != null && !member.roles.contains(role) && !role.isPublicRole && !role.isManaged && guild.selfMember.canInteract(role)) {
					roles.add(role)
				}
			} catch (e: NumberFormatException) {} // The specified ID is not a valid snowflake (null).
		}

		if (roles.isNotEmpty()) {
			if (roles.size == 1) {
				guild.controller.addSingleRoleToMember(member, roles[0]).reason("Autorole").complete()
			} else {
				guild.controller.addRolesToMember(member, roles).reason("Autorole").complete()
			}
		}

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS
		payload["votedAt"] = System.currentTimeMillis()
		payload["canVoteAgain"] = tomorrow
		return payload.toString()
	}
}