package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.EmbedBuilder
import org.jooby.Request
import org.jooby.Response
import java.awt.Color

class APILoriTransferBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/transfer-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		// A Transfer Balance endpoint é algo especial, serve para transferir dinheiro para uma conta para a outra...
		// Mas, para evitar pessoas abusando da API, a API pergunta para o usuário se ele quer realmente transferir os sonhos.
		// Nós reenviamos para o client os dados da transação, utilizando uma URL especificada
		val json = JsonObject()

		val body = jsonParser.parse(req.body().value()).obj

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
			json["api:message"] = "Not a number"
			json["api:code"] = LoriWebCodes.UNAUTHORIZED
			return json.toString()
		}

		if (0 >= quantity) {
			json["api:message"] = "Trying to withdraw less or equal to zero amount"
			json["api:code"] = LoriWebCodes.UNAUTHORIZED
			return json.toString()
		}

		if (quantity > lorittaProfile.dreams) {
			json["api:message"] = "INSUFFICIENT_FUNDS"
			json["api:code"] = LoriWebCodes.INSUFFICIENT_FUNDS
			return json.toString()
		}

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			json["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return json.toString()
		}

		val member = guild.getMemberById(userId)

		if (member == null) {
			json["invalidUser"] = userId
			json["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return json.toString()
		}

		val receiver = guild.getMemberById(receiverId)

		if (receiver == null) {
			json["invalidUser"] = receiver
			json["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return json.toString()
		}

		Loritta.logger.info("Enviando requisição de transferências de sonhos ($quantity sonhos) para ${lorittaProfile.userId}, motivo: ${reason} - ID: ${guildId}")

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
						json["api:message"] = "Not a number"
						json["api:code"] = LoriWebCodes.UNAUTHORIZED
						HttpRequest.post(webhookUrl)
								.send(json.toString())
								.ok()
						return@onReactionAddByAuthor
					}

					if (0 >= quantity) {
						json["api:message"] = "Trying to withdraw less or equal to zero amount"
						json["api:code"] = LoriWebCodes.UNAUTHORIZED
						HttpRequest.post(webhookUrl)
								.send(json.toString())
								.ok()
						return@onReactionAddByAuthor
					}

					if (quantity > lorittaProfile.dreams) {
						json["api:message"] = "INSUFFICIENT_FUNDS"
						json["api:code"] = LoriWebCodes.INSUFFICIENT_FUNDS
						HttpRequest.post(webhookUrl)
								.send(json.toString())
								.ok()
						return@onReactionAddByAuthor
					}

					val before = lorittaProfile.dreams
					lorittaProfile.dreams -= quantity
					receiverProfile.dreams += quantity

					// Transação feita!
					json["api:code"] = LoriWebCodes.SUCCESS
					json["payerBalance"] = lorittaProfile.dreams
					json["receiverBalance"] = receiverProfile.dreams
					json["metadata"] = metadata

					HttpRequest.post(webhookUrl)
							.send(json.toString())
							.ok()

					loritta save lorittaProfile
					loritta save receiverProfile

					Loritta.logger.info("${lorittaProfile.userId} teve $quantity sonhos (antes possuia $before sonhos) transferidos para ${receiverProfile.userId}. motivo: ${reason} - ID: ${guildId}")
					return@onReactionAddByAuthor
				}
			}
		} catch (e: Exception) {
			json["api:code"] = LoriWebCodes.DISABLED_DIRECT_MESSAGES
			return json.toString()
		}

		json["api:code"] = LoriWebCodes.SUCCESS

		return json.toString()
	}
}