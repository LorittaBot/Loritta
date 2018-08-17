package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import jdk.nashorn.api.scripting.ScriptObjectMirror
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel

class ParallaxTextChannel(private val textChannel: TextChannel) {
	val calculatedPosition get() = textChannel.positionRaw
	val deletable get() = textChannel.guild.selfMember.hasPermission(Permission.MANAGE_CHANNEL)
	val guild = ParallaxGuild(textChannel.guild)
	val lastMessageId get() = textChannel.latestMessageId
	val members = textChannel.members.map(::ParallaxMember)
	// TODO: messages
	val name get() = textChannel.name
	val nsfw get() = textChannel.isNSFW
	// TODO: permission overrides
	val position get() = textChannel.position
	val topic get() = textChannel.topic
	// TODO: typing
	// TODO: typingCount

	// TODO: bulkDelete
	// TODO: clone
	// TODO: createInvite
	// TODO: .createMessageCollector(filteroptions)

	fun createWebhook(name: String, reason: String? = null): ParallaxWebhook {
		return ParallaxWebhook(textChannel.createWebhook(name)
				.reason(reason)
				.complete())
	}

	fun delete() {
		textChannel.delete().complete()
	}

	// TODO: edit
	// TODO: fetchMessage
	// TODO: fetchMessages
	// TODO: fetchPinnedMessages
	// TODO: fetchWebhooks
	// TODO: overwritePermissions
	// TODO: permissionsFor

	fun send(content: Any) {
		if (content is ScriptObjectMirror) {
			send(ParallaxUtils.toParallaxEmbed(content))
			return
		}
		if (content is ParallaxEmbed) {
			textChannel.sendMessage(content.toDiscordEmbed()).complete()
		} else {
			textChannel.sendMessage(content.toString()).complete()
		}
	}

	fun setName(name: String, reason: String? = null) {
		textChannel.manager.setName(name)
				.reason(reason)
				.complete()
	}

	fun setPosition(position: Int) {
		textChannel.manager.setPosition(position).complete()
	}

	fun setTopic(topic: String, reason: String? = null) {
		textChannel.manager.setTopic(topic)
				.reason(reason)
				.complete()
	}


	fun startTyping() {
		textChannel.sendTyping().complete()
	}

	// TODO: stopTyping

	override fun toString(): String {
		return textChannel.asMention
	}
}