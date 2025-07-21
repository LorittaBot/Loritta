package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PostSendMessageGuildRoute(loritta: LorittaBot) : RequiresAPIGuildAuthRoute(loritta, "/send-message") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		// Rate Limit
		val last = loritta.apiCooldown.getOrDefault(call.request.trueIp, 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff)
			throw WebsiteAPIException(
				HttpStatusCode.TooManyRequests,
				net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.RATE_LIMIT,
					"Rate limit!"
				)
			)

		loritta.apiCooldown[call.request.trueIp] = System.currentTimeMillis()

		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }
		val channelId = json["channelId"].nullString
		val messageString = json["message"].string
		val customTokens = json["customTokens"].nullObj
		val sourceList = json["sources"].nullArray

		val sources = mutableListOf<Any>(guild)

		if (sourceList != null) {
			for (element in sourceList) {
				val str = element.string

				when (str) {
					"user" -> sources.add(loritta.lorittaShards.getUserById(userIdentification.id)!!)
					"member" -> {
						val member = guild.getMemberById(userIdentification.id)

						if (member != null)
							sources.add(member)
					}
				}
			}
		}

		val tokens = mutableMapOf<String, String>()

		customTokens?.toMap()?.forEach { key, value ->
			tokens[key] = value.string
		}

		val message = try {
			MessageUtils.generateMessage(messageString, sources, guild, tokens)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		} ?: throw WebsiteAPIException(
			HttpStatusCode.BadRequest,
			WebsiteUtils.createErrorPayload(
				loritta,
				LoriWebCode.INVALID_MESSAGE,
				"Invalid message"
			)
		)

		if (channelId != null) {
			val channel = guild.getGuildMessageChannelById(channelId)
				?: throw WebsiteAPIException(
					HttpStatusCode.BadRequest,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.CHANNEL_DOESNT_EXIST,
						"Channel ${channelId} doesn't exist"
					)
				)

			if (!channel.canTalk())
				throw WebsiteAPIException(
					HttpStatusCode.BadRequest,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.CANT_TALK_IN_CHANNEL,
						"Channel ${channelId} doesn't exist"
					)
				)

			// This is a bit crappy, but we need to create a builder from the already generated message
			val patchedMessage = MessageCreateBuilder.from(message)
			if (5 > patchedMessage.components.size) { // Below the component limit
				val member = guild.retrieveMemberOrNullById(userIdentification.id)
				val user = member?.user
				val i18nContext = loritta.languageManager.getI18nContextById(serverConfig.localeId)
				patchedMessage.addComponents(
					ActionRow.of(
						loritta.interactivityManager.button(
							false,
							ButtonStyle.SECONDARY,
							i18nContext.get(I18nKeysData.Common.TestMessageWarning.ButtonLabel),
							{
								this.loriEmoji = Emotes.LoriCoffee
							}
						) {
							it.reply(true) {
								styled(
									i18nContext.get(I18nKeysData.Common.TestMessageWarning.MessageWasTestedByUser("${user?.asMention} [${user?.asUserNameCodeBlockPreviewTag(true)}]")),
									Emotes.LoriCoffee
								)

								styled(
									i18nContext.get(I18nKeysData.Common.TestMessageWarning.DontWorryTheMessageWillOnlyShowUpWhileTesting),
									Emotes.LoriLurk
								)
							}
						}
					)
				)
			}

			val message = channel.sendMessage(patchedMessage.build()).await()

			call.respondJson(jsonObject("messageId" to message.id), HttpStatusCode.Created)
			return
		} else {
			val privateUser = loritta.lorittaShards.getUserById(userIdentification.id) ?: throw WebsiteAPIException(
				HttpStatusCode.BadRequest,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
					"Member ${userIdentification.id} disabled direct messages"
				)
			)

			try {
				// This is a bit crappy, but we need to create a builder from the already generated message
				val patchedMessage = MessageCreateBuilder.from(message)
				if (5 > patchedMessage.components.size) { // Below the component limit
					val member = guild.retrieveMemberOrNullById(userIdentification.id)
					val user = member?.user
					val i18nContext = loritta.languageManager.getI18nContextById(serverConfig.localeId)
					patchedMessage.addComponents(
						ActionRow.of(
							loritta.interactivityManager.button(
								false,
								ButtonStyle.SECONDARY,
								i18nContext.get(I18nKeysData.Common.TestMessageWarning.ButtonLabel),
								{
									this.loriEmoji = Emotes.LoriCoffee
								}
							) {
								it.reply(true) {
									styled(
										i18nContext.get(I18nKeysData.Common.TestMessageWarning.MessageWasTestedByUser("${user?.asMention} [${user?.asUserNameCodeBlockPreviewTag(true)}]")),
										Emotes.LoriCoffee
									)

									styled(
										i18nContext.get(I18nKeysData.Common.TestMessageWarning.DontWorryTheMessageWillOnlyShowUpWhileTesting),
										Emotes.LoriLurk
									)
								}
							}
						)
					)
				}

				val message = loritta.getOrRetrievePrivateChannelForUser(privateUser).sendMessage(patchedMessage.build()).await()

				call.respondJson(jsonObject("messageId" to message.id), HttpStatusCode.Created)
				return
			} catch (e: Exception) {
				throw WebsiteAPIException(
					HttpStatusCode.BadRequest,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
						"Member ${userIdentification.id} disabled direct messages"
					)
				)
			}
		}
	}
}