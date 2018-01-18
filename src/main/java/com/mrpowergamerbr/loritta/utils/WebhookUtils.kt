package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.Webhook
import net.dv8tion.jda.core.exceptions.PermissionException

object WebhookUtils {
	/**
	 * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
	 *
	 * @param textChannel Canal de texto
	 * @param name        Nome do Webhook
	 * @return TemmieWebhook pronto para ser usado
	 */
	fun getOrCreateWebhook(channel: MessageChannel, name: String): TemmieWebhook? {
		if (channel == null || channel.type == ChannelType.PRIVATE) // Se a Loritta não pode acessar as webhooks do servidor, retorne null
			return null

		val textChannel = channel as TextChannel

		if (!textChannel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS))
			return null

		val webhookList = textChannel.guild.webhooks.complete();

		val webhooks = webhookList.filter{ it.channel == textChannel }
		var webhook: Webhook?

		if (webhooks.isEmpty()) {
			try { // try catch, já que pelo visto a verificação acima falha as vezes
				webhook = textChannel.createWebhook(name).complete()
			} catch (e: PermissionException) {
				return null
			}
		} else {
			webhook = webhooks[0];
		}

		val temmie = TemmieWebhook(webhook.url, true);

		return temmie;
	}
}

fun getOrCreateWebhook(channel: MessageChannel, name: String): TemmieWebhook? {
	return WebhookUtils.getOrCreateWebhook(channel, name)
}