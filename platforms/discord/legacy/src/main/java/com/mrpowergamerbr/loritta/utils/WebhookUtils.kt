package com.mrpowergamerbr.loritta.utils

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.WebhookType
import net.dv8tion.jda.api.exceptions.PermissionException

object WebhookUtils {
	/**
	 * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
	 *
	 * @param textChannel Canal de texto
	 * @param name        Nome do Webhook
	 * @return TemmieWebhook pronto para ser usado
	 */
	suspend fun getOrCreateWebhook(channel: MessageChannel, name: String): WebhookClient? {
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

		val temmie = WebhookClientBuilder(webhook.url)
				.setExecutorService(loritta.webhookExecutor)
				.setHttpClient(loritta.webhookOkHttpClient)
				.build()

		return temmie
	}
}