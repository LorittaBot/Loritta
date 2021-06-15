package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.chance
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.PersonalPronoun
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

class PostUserReputationsRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/{userId}/reputation") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutex = Mutex()

		fun sendReputationToCluster(guildId: String, channelId: String, giverId: String, receiverId: String, reputationCount: Long) {
			if (guildId.isValidSnowflake() && channelId.isValidSnowflake()) {
				val cluster = DiscordUtils.getLorittaClusterForGuildId(guildId.toLong())

				try {
					HttpRequest.post("https://${cluster.getUrl()}/api/v1/loritta/send-reputation-message")
							.userAgent(loritta.lorittaCluster.getUserAgent())
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

		suspend fun sendReputationReceivedMessage(guildId: String, channelId: String, giverId: String, giverProfile: Profile, receiverId: String, reputationCount: Int) {
			logger.info { "Received sendReputation request in $guildId $channelId by $giverId for $receiverId" }

			if (guildId.isValidSnowflake() && channelId.isValidSnowflake()) {
				// Iremos verificar se o usuário *pode* usar comandos no canal especificado
				val channel = lorittaShards.getTextChannelById(channelId)

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

					val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(serverConfig, member), giverProfile)

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
                                    "<${loritta.instanceConfig.loritta.website.url}user/${receiverId}/rep?guild=${guildId}&channel=${channelId}>",
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

		if (!MiscUtils.checkRecaptcha(com.mrpowergamerbr.loritta.utils.loritta.config.googleRecaptcha.reputationToken, token))
			throw WebsiteAPIException(
					HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
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
								LoriWebCode.COOLDOWN
						)
				)

			val userIdentification = discordAuth.getUserIdentification()
			val status = MiscUtils.verifyAccount(userIdentification, ip)
			val email = userIdentification.email
			logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
			logger.info { "Is verified? ${userIdentification.verified}" }
			logger.info { "Email ${email}" }
			logger.info { "IP: $ip" }
			MiscUtils.handleVerification(status)

			giveReputation(userIdentification.id.toLong(), ip, userIdentification.email!!, receiver.toLong(), content)

			val donatorPaid = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonationsAsync(userIdentification.id.toLong())
			var randomChance = UserPremiumPlans.getPlanFromValue(donatorPaid).loriReputationRetribution

			if (chance(randomChance)) { // Lori é fofis e retribuiu reputações :eu_te_moido:
				GlobalScope.launch(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
					delay(Loritta.RANDOM.nextLong(8000, 15001)) // Delay aleatório para ficar mais "real"

					giveReputation(
							loritta.discordConfig.discord.clientId.toLong(),
							"127.0.0.1",
							"me@loritta.website",
							userIdentification.id.toLong(),
							"Stay awesome :3"
					)

					val reputationCount = loritta.newSuspendedTransaction {
						Reputations.select { Reputations.receivedById eq userIdentification.id.toLong() }.count()
					}

					if (guildId != null && channelId != null) {
						sendReputationToCluster(guildId, channelId, loritta.discordConfig.discord.clientId, userIdentification.id, reputationCount)
					}
				}
			}

			val reputations = loritta.newSuspendedTransaction {
				Reputation.find { Reputations.receivedById eq receiver.toLong() }.sortedByDescending { it.receivedAt }
			}

			if (guildId != null && channelId != null)
				sendReputationToCluster(guildId, channelId, userIdentification.id, receiver, reputations.size.toLong())

			call.respondJson(jsonObject())
		}
	}

	suspend fun giveReputation(giver: Long, giverIp: String, giverEmail: String, receiver: Long, content: String) {
		logger.info("$giver ($giverIp/$giverEmail) deu uma reputação para $receiver! Motivo: $content")
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