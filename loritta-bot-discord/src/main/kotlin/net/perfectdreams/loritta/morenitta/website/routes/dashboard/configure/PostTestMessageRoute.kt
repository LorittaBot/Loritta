package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.placeholders.SectionPlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.utils.placeholders.RenderableMessagePlaceholder
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageRequest
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.concurrent.TimeUnit

class PostTestMessageRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/test-message") {
	// THE HACKIEST HACK YOU EVER HACKED
	private val messageSendCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<Long, Long>().asMap()

	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val member = guild.retrieveMemberOrNullById(userIdentification.id.toLong())!! // Hack!
		val user = member.user
		val userId = user.idLong
		val request = Json.decodeFromString<TestMessageRequest>(call.receiveText())
		val rawMessage = request.message
		val channelId = request.channelId
		val section = SectionPlaceholders.sections.first { it.type == request.sectionType }

		val lorittaRenderablePlaceholders = mutableListOf<RenderableMessagePlaceholder>()

		// This is a WORKAROUND (as always...)
		// We are going to "map down" the placeholders into the customTokens map
		for (placeholder in request.placeholders) {
			val lorittaPlaceholder = section.placeholders.firstOrNull {
				it.names.any { it.placeholder.name == placeholder.key }
			}

			if (lorittaPlaceholder != null) {
				lorittaRenderablePlaceholders.add(
					RenderableMessagePlaceholder(
						lorittaPlaceholder,
						placeholder.value
					)
				)
			}
		}

		// Rate Limit
		val last = messageSendCooldown.getOrDefault(userId, 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff) {
			call.respondBodyAsHXTrigger(
				status = HttpStatusCode.TooManyRequests
			) {
				playSoundEffect = "config-error"
				showSpicyToast(
					EmbeddedSpicyToast.Type.WARN,
					"Algo deu errado ao enviar a mensagem!",
				) {
					text("Você já enviou uma mensagem recentemente! Espere um pouco antes de tentar enviar uma nova mensagem.")
				}
			}
			return
		}

		messageSendCooldown[userId] = System.currentTimeMillis()

		val channel = if (channelId == null) {
			loritta.getOrRetrievePrivateChannelForUser(user)
		} else {
			guild.getGuildMessageChannelById(channelId)
		}

		if (channel == null) {
			call.respondBodyAsHXTrigger(
				status = HttpStatusCode.BadRequest
			) {
				playSoundEffect = "config-error"
				showSpicyToast(
					EmbeddedSpicyToast.Type.WARN,
					"Algo deu errado ao enviar a mensagem!",
				) {
					text("O canal que você selecionou não existe.")
				}
			}
			return
		}

		// A bit hacky, but whatever!!
		val message = MessageUtils.generateMessage(
			rawMessage,
			guild,
			lorittaRenderablePlaceholders,
			true
		)

		// This is a bit crappy, but we need to create a builder from the already generated message
		val patchedMessage = MessageCreateBuilder.from(message)
		if (5 > patchedMessage.components.size) { // Below the component limit
			patchedMessage.addActionRow(
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
							i18nContext.get(I18nKeysData.Common.TestMessageWarning.MessageWasTestedByUser("${user.asMention} [${user.asUserNameCodeBlockPreviewTag(true)}]")),
							Emotes.LoriCoffee
						)

						styled(
							i18nContext.get(I18nKeysData.Common.TestMessageWarning.DontWorryTheMessageWillOnlyShowUpWhileTesting),
							Emotes.LoriLurk
						)
					}
				}
			)
		}

		try {
			channel.sendMessage(patchedMessage.build()).await()
		} catch (e: Exception) {
			call.respondBodyAsHXTrigger(
				status = HttpStatusCode.BadRequest
			) {
				playSoundEffect = "config-error"
				showSpicyToast(
					EmbeddedSpicyToast.Type.WARN,
					"Algo deu errado ao enviar a mensagem!"
				) {
					text("Não foi possível enviar a mensagem.")
				}
			}
			return
		}

		call.respondBodyAsHXTrigger(
			status = HttpStatusCode.OK
		) {
			playSoundEffect = "config-saved"
			showSpicyToast(
				EmbeddedSpicyToast.Type.SUCCESS,
				"Mensagem enviada!",
			)
		}
	}
}