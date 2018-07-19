package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.economy

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriWebCode
import mu.KotlinLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import java.awt.Color

@Path("/api/v1/economy/transfer-balance")
class LoriTransferBalanceController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		// A Transfer Balance endpoint é algo especial, serve para transferir dinheiro para uma conta para a outra...
		// Mas, para evitar pessoas abusando da API, a API pergunta para o usuário se ele quer realmente transferir os sonhos.
		// Nós reenviamos para o client os dados da transação, utilizando uma URL especificada
		res.type(MediaType.json)

		val json = JsonObject()

		val receivedPayload = req.body().value()
		logger.debug { "Recebi pedido de transferência! $receivedPayload" }

		val body = jsonParser.parse(receivedPayload).obj

		val userId = body["userId"].string // User ID
		val quantity = body["quantity"].double // Quantidade a ser transferida
		val reason = body["reason"].string // Motivo da transação
		val guildId = body["guildId"].string // Guild ID
		val receiverId = body["receiverId"].string // Quem irá receber a transferência

		val title = body["name"].string // Nome do produto
		val image = body["image"].nullString // Imagem que será mostrada no embed
		val color = body["color"].nullInt // Cor da embed
		val metadata = body["metadata"].string // Metadata da transação
		val webhookUrl = body["url"].string // Webhook URL

		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		if (quantity.isNaN()) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.INVALID_NUMBER,
					"Provided quantity \"${quantity}\" is Not a Number"
			))
			return
		}

		if (0 >= quantity) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.INVALID_NUMBER,
					"Trying to withdraw less than or equal to zero"
			))
			return
		}

		if (quantity > lorittaProfile.dreams) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.INSUFFICIENT_FUNDS,
					"User ${lorittaProfile.userId} has less than ${quantity} dreams"
			))
			return
		}

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.UNKNOWN_GUILD,
					"Guild ${guildId} doesn't exist or it isn't loaded yet"
			))
			return
		}

		val member = guild.getMemberById(userId)

		if (member == null) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.MEMBER_NOT_IN_GUILD,
					"Member ${userId} is not in guild ${guild.id}"
			))
			return
		}

		val receiver = guild.getMemberById(receiverId)

		if (receiver == null) {
			res.status(Status.BAD_REQUEST)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.MEMBER_NOT_IN_GUILD,
					"Member ${receiver} is not in guild ${guild.id}"
			))
			return
		}

		logger.debug { "Enviando requisição de transferências de sonhos ($quantity sonhos) para ${lorittaProfile.userId}, motivo: ${reason} - ID: ${guildId}" }

		val embed = EmbedBuilder()
		embed.setTitle("Requisição de Transferência (${title})")
		embed.setDescription("`${guild.name}` pediu para transferir $quantity Sonhos para `${receiver.user.name}#${receiver.user.discriminator}`\n\nClique em ✅ para confirmar a transferência.")

		if (image != null) {
			embed.setImage(image)
		}

		if (color != null) {
			embed.setColor(Color(color))
		}

		try {
			val message = member.user.openPrivateChannel().complete().sendMessage(embed.build()).complete()
			message.addReaction("✅").queue()

			message.onReactionAddByAuthor(member.user.id) {
				if (it.reactionEmote.name == "✅") {
					message.delete().complete()

					val lorittaProfile = loritta.getLorittaProfileForUser(userId)
					val receiverProfile = loritta.getLorittaProfileForUser(receiverId)

					if (quantity.isNaN()) {
						val json = WebsiteUtils.createErrorPayload(
								LoriWebCode.INVALID_NUMBER,
								"Provided quantity \"${quantity}\" is Not a Number"
						)
						HttpRequest.post(webhookUrl)
								.contentType("application/json")
								.send(json.toString())
								.ok()
						return@onReactionAddByAuthor
					}

					if (0 >= quantity) {
						val json = WebsiteUtils.createErrorPayload(
								LoriWebCode.INVALID_NUMBER,
								"Trying to withdraw less than or equal to zero"
						)
						HttpRequest.post(webhookUrl)
								.contentType("application/json")
								.send(json.toString())
								.ok()
						return@onReactionAddByAuthor
					}

					if (quantity > lorittaProfile.dreams) {
						val json = WebsiteUtils.createErrorPayload(
								LoriWebCode.INSUFFICIENT_FUNDS,
								"User ${lorittaProfile.userId} has less than ${quantity} dreams"
						)
						HttpRequest.post(webhookUrl)
								.contentType("application/json")
								.send(json.toString())
								.ok()

						return@onReactionAddByAuthor
					}

					val before = lorittaProfile.dreams
					lorittaProfile.dreams -= quantity
					receiverProfile.dreams += quantity

					// Transação feita!
					json["payerBalance"] = lorittaProfile.dreams
					json["receiverBalance"] = receiverProfile.dreams
					json["metadata"] = metadata
					json["guildId"] = guildId
					json["receiverId"] = receiverId
					json["quantity"] = quantity
					json["userId"] = userId

					HttpRequest.post(webhookUrl)
							.contentType("application/json")
							.send(json.toString())
							.ok()

					loritta save lorittaProfile
					loritta save receiverProfile

					logger.info { "${lorittaProfile.userId} teve $quantity sonhos (antes possuia $before sonhos) transferidos para ${receiverProfile.userId}. motivo: ${reason} - ID: ${guildId}" }
					return@onReactionAddByAuthor
				}
			}

			res.status(Status.CREATED)
			res.send(
					jsonObject(
							"messageId" to message.id
					)
			)
			return
		} catch (e: ErrorResponseException) {
			if (e.errorCode == 50007) {
				res.status(Status.BAD_REQUEST)
				res.send(WebsiteUtils.createErrorPayload(
						LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
						"Member ${member.user.id} disabled direct messages"
				))
				return
			}
		}
	}
}