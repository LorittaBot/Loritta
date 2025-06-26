package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.locale.PersonalPronoun
import net.perfectdreams.loritta.morenitta.utils.locale.getPersonalPronoun
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class PostUserReputationsRoute(loritta: LorittaBot) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/{userId}/reputation") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
		private val mutex = Mutex()

		fun sendReputationToCluster(loritta: LorittaBot, guildId: String, channelId: String, giverId: String, receiverId: String, reputationCount: Long) {
			if (guildId.isValidSnowflake() && channelId.isValidSnowflake()) {
				val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, guildId.toLong())

				try {
					HttpRequest.post("${cluster.getInternalUrl(loritta)}/api/v1/loritta/send-reputation-message")
						.userAgent(loritta.lorittaCluster.getUserAgent(loritta))
						.header("Authorization", loritta.lorittaInternalApiKey.name)
						.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
						.readTimeout(loritta.config.loritta.clusterReadTimeout)
						.send(
							gson.toJson(
								jsonObject(
									"guildId" to guildId,
									"channelId" to channelId,
									"giverId" to giverId,
									"receiverId" to receiverId,
									"reputationCount" to reputationCount
								)
							)
						)
						.ok()
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${cluster.name} ${cluster.id} offline!" }
					throw ClusterOfflineException(cluster.id, cluster.name)
				}
			}
		}

		suspend fun sendReputationReceivedMessage(loritta: LorittaBot, guildId: String, channelId: String, giverId: String, giverProfile: Profile, receiverId: String, reputationCount: Int) {
			logger.info { "Received sendReputation request in $guildId $channelId by $giverId for $receiverId" }

			if (guildId.isValidSnowflake() && channelId.isValidSnowflake()) {
				// Iremos verificar se o usuário *pode* usar comandos no canal especificado
				val channel = loritta.lorittaShards.getGuildMessageChannelById(channelId)

				if (channel != null) {
					if (!channel.canTalk()) // Eu não posso falar!
						return
					val member = channel.guild.retrieveMemberById(giverId).await()
					if (member == null || !channel.canTalk(member)) // O usuário não está no servidor ou não pode falar no chat
						return

					if (!channel.guild.selfMember.hasPermission(channel, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS)) // Permissões
						return

					val serverConfig = loritta.getOrCreateServerConfig(guildId.toLong())
					val receiverProfile = loritta.getOrCreateLorittaProfile(receiverId)
					val receiverSettings = loritta.newSuspendedTransaction {
						receiverProfile.settings
					}

					val lorittaUser = GuildLorittaUser(loritta, member, LorittaUser.loadMemberLorittaPermissions(loritta, serverConfig, member), giverProfile)

					if (serverConfig.blacklistedChannels.contains(channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) // O usuário não pode enviar comandos no canal
						return

					val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

					// Tudo certo? Então vamos enviar!
					val reply = LorittaReply(
						locale[
								"commands.command.reputation.success",
								"<@${giverId}>",
								"<@$receiverId>",
								reputationCount,
								Emotes.LORI_OWO,
								"<${loritta.config.loritta.website.url}user/${receiverId}/rep?guild=${guildId}&channel=${channelId}>",
								receiverSettings.gender.getPersonalPronoun(locale, PersonalPronoun.THIRD_PERSON, "<@$receiverId>")
						],
						Emotes.LORI_HEART
					)

					channel.sendMessage(reply.build()).queue()
				}
			}
		}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val receiver = call.parameters["userId"] ?: return

		if (userIdentification.id == receiver) {
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.FORBIDDEN,
					"You can't give a reputation to yourself, silly!"
				)
			)
		}

		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		val content = json["content"].string
		val token = json["token"].string
		val guildId = json["guildId"].nullString
		val channelId = json["channelId"].nullString

		if (!MiscUtils.checkRecaptcha(loritta.config.loritta.googleRecaptcha.reputationToken, token))
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.INVALID_RECAPTCHA
				)
			)

		val ip = call.request.trueIp

		mutex.withLock {
			val lastReputationGiven = loritta.newSuspendedTransaction {
				Reputation.find {
					(Reputations.givenById eq userIdentification.id.toLong()) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq ip)
				}.sortedByDescending { it.receivedAt }.firstOrNull()
			}

			val diff = System.currentTimeMillis() - (lastReputationGiven?.receivedAt ?: 0L)

			if (3_600_000 > diff)
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.COOLDOWN
					)
				)

			val reputationsEnabled = loritta.transaction {
				Profile.findById(receiver.toLong())?.settings?.reputationsEnabled ?: true
			}

			if (!reputationsEnabled)
				throw WebsiteAPIException(HttpStatusCode.Forbidden, WebsiteUtils.createErrorPayload(loritta, LoriWebCode.FORBIDDEN))

			val userIdentification = discordAuth.getUserIdentification()
			val status = MiscUtils.verifyAccount(loritta, userIdentification, ip)
			val email = userIdentification.email
			logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
			logger.info { "Is verified? ${userIdentification.verified}" }
			logger.info { "Email ${email}" }
			logger.info { "IP: $ip" }
			MiscUtils.handleVerification(loritta, status)

			giveReputation(userIdentification.id.toLong(), ip, userIdentification.email!!, receiver.toLong(), content)

			val donatorPaid = loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())
			var randomChance = UserPremiumPlans.getPlanFromValue(donatorPaid).loriReputationRetribution

			if (chance(randomChance)) { // Lori é fofis e retribuiu reputações :eu_te_moido:
				GlobalScope.launch(loritta.coroutineDispatcher) {
					delay(LorittaBot.RANDOM.nextLong(8000, 15001)) // Delay aleatório para ficar mais "real"

					giveReputation(
						loritta.config.loritta.discord.applicationId.toString().toLong(),
						"127.0.0.1",
						"me@loritta.website",
						userIdentification.id.toLong(),
						"Stay awesome :3"
					)

					val reputationCount = loritta.newSuspendedTransaction {
						Reputations.selectAll().where { Reputations.receivedById eq userIdentification.id.toLong() }.count()
					}

					if (guildId != null && channelId != null) {
						sendReputationToCluster(loritta, guildId, channelId, loritta.config.loritta.discord.applicationId.toString(), userIdentification.id, reputationCount)
					}
				}
			}

			val reputations = loritta.newSuspendedTransaction {
				Reputation.find { Reputations.receivedById eq receiver.toLong() }.sortedByDescending { it.receivedAt }
			}

			if (guildId != null && channelId != null)
				sendReputationToCluster(loritta, guildId, channelId, userIdentification.id, receiver, reputations.size.toLong())

			call.respondJson(jsonObject())
		}
	}

	suspend fun giveReputation(giver: Long, giverIp: String, giverEmail: String, receiver: Long, content: String) {
		logger.info { "$giver ($giverIp/$giverEmail) deu uma reputação para $receiver! Motivo: $content" }
		loritta.newSuspendedTransaction {
			Reputation.new {
				this.givenById = giver
				this.givenByIp = giverIp
				this.givenByEmail = giverEmail
				this.receivedById = receiver
				this.content = content
				this.receivedAt = System.currentTimeMillis()
			}
		}
	}
}