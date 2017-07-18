package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.core.Permission
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
	fun getOrCreateWebhook(textChannel: TextChannel, name: String): TemmieWebhook? {
		if (textChannel == null || !textChannel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) { // Se a Loritta não pode acessar as webhooks do servidor, retorne null
			return null;
		}
		val webhookList = textChannel.guild.webhooks.complete();

		val webhooks = webhookList.filter{ it.channel == textChannel }
		var webhook: Webhook?

		if (webhooks.isEmpty()) {
			try { // try catch, já que pelo visto a verificação acima falha as vezes
				webhook = textChannel.guild.controller.createWebhook(textChannel, name).complete()
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

fun getOrCreateWebhook(textChannel: TextChannel, name: String): TemmieWebhook? {
	return WebhookUtils.getOrCreateWebhook(textChannel, name)
}