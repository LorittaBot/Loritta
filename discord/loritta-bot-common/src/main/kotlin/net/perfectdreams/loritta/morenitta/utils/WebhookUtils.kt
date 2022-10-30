package net.perfectdreams.loritta.morenitta.utils

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import dev.kord.common.entity.Permission
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.WebhookType
import dev.kord.rest.request.KtorRequestException
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.morenitta.LorittaBot

object WebhookUtils {
    /**
     * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
     *
     * @param textChannel Canal de texto
     * @param name        Nome do Webhook
     * @return TemmieWebhook pronto para ser usado
     */
    suspend fun getOrCreateWebhook(loritta: LorittaBot, channel: Channel, name: String): WebhookClient? {
        if (channel.type == ChannelType.DM) // Se a Loritta não pode acessar as webhooks do servidor, retorne null
            return null

        val textChannel = channel

        if (!textChannel.guild.retrieveSelfMember().hasPermission(Permission.ManageWebhooks))
            return null

        val selfUser = channel.deviousFun.retrieveSelfUser()
        val webhookList = textChannel.guild.retrieveWebhooks()
            .filter {
                // Webhooks created by users or bots are INCOMING and we only want to get webhooks created by Loritta!
                // See: https://github.com/discord/discord-api-docs/issues/3056
                it.type == WebhookType.Incoming && it.ownerAsUser?.idLong == selfUser.idLong
            }

        val webhooks = webhookList.filter { it.channel == textChannel }
        val webhook = if (webhooks.isEmpty()) {
            try { // try catch, já que pelo visto a verificação acima falha as vezes
                textChannel.createWebhook(name)
            } catch (e: KtorRequestException) {
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