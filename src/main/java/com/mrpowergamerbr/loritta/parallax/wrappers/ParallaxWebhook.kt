package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.entities.Webhook

class ParallaxWebhook(private val webhook: Webhook) {
	fun send(content: String) {
		webhook.newClient().build().send(content).get()
	}

	fun send(embed: ParallaxEmbed) {
		webhook.newClient().build().send(embed.toDiscordEmbed()).get()
	}

	fun delete() {
		webhook.delete().complete()
	}
}