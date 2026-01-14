package net.perfectdreams.loritta.morenitta.utils

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.entities.WebhookType
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.exceptions.PermissionException
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await

object WebhookUtils {
	val webhookRegex = Regex("/api/webhooks/([0-9]+)/([A-z0-9_-]+)")

	/**
	 * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
	 *
	 * @param textChannel Canal de texto
	 * @param name        Nome do Webhook
	 * @return TemmieWebhook pronto para ser usado
	 */
	suspend fun getOrCreateWebhook(loritta: LorittaBot, channel: MessageChannel, name: String): WebhookClient<*>? {
		if (channel.type == ChannelType.PRIVATE) // Se a Loritta não pode acessar as webhooks do servidor, retorne null
			return null

		val textChannel = channel as TextChannel

		if (!textChannel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS))
			return null

		val webhookList = textChannel.guild.retrieveWebhooks().await()
				.filter {
					// Webhooks created by users or bots are INCOMING and we only want to get webhooks created by Loritta!
					// See: https://github.com/discord/discord-api-docs/issues/3056
					it.type == WebhookType.INCOMING && it.ownerAsUser?.idLong == channel.jda.selfUser.idLong
				}

		val webhooks = webhookList.filter { it.channel == textChannel }
		val webhook = if (webhooks.isEmpty()) {
			try { // try catch, já que pelo visto a verificação acima falha as vezes
				textChannel.createWebhook(name).await()
			} catch (e: PermissionException) {
				return null
			}
		} else {
			webhooks[0]
		}

		return WebhookClient.createClient(webhook.jda, webhook.url)
	}
}