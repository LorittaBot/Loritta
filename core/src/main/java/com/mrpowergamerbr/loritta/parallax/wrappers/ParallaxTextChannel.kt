package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import org.graalvm.polyglot.Value
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.function.Function
import javax.imageio.ImageIO

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

	fun send(content: Any, attachment: ParallaxAttachment): ParallaxPromise<ParallaxMessage> {
		return send(content, ParallaxMessageOptions(attachment))
	}

	@JvmOverloads
	fun send(content: Any, options: ParallaxMessageOptions? = null): ParallaxPromise<ParallaxMessage> {
		if (content is Value) { // Conteúdo passado diretamente pelo GraalJS (ao usar eval)
			return send(ParallaxUtils.toParallaxEmbed(content), options)
		}
		if (content is Map<*, *>) { // PolyglotMap do GraalJS (chamar a função pelo JS)
			return send(ParallaxUtils.toParallaxMessage(content), options)
		}

		if (content is Message) { // Mensagem do Discord
			if (options?.attachment != null) {
				val outputStream = ByteArrayOutputStream()
				outputStream.use {
					ImageIO.write(options.attachment.image?.image, "png", it)
				}

				val inputStream = ByteArrayInputStream(outputStream.toByteArray())

				return object: ParallaxPromise<ParallaxMessage>() {
					override fun queue(success: Function<ParallaxMessage, Any?>?, failure: Function<Any?, Any?>?) {
						textChannel.sendMessage(content).addFile(inputStream, "image.png").queue(
								{
									success?.apply(ParallaxMessage(it))
								},
								{
									if (failure == null)
										ParallaxPromise.DEFAULT_CHANNEL_FAILURE_CALLBACK.invoke(textChannel, it)
									else
										failure.apply(null)
								}
						)
					}
				}
			}
			return object: ParallaxPromise<ParallaxMessage>() {
				override fun queue(success: Function<ParallaxMessage, Any?>?, failure: Function<Any?, Any?>?) {
					textChannel.sendMessage(content).queue(
							{
								success?.apply(ParallaxMessage(it))
							},
							{
								if (failure == null)
									ParallaxPromise.DEFAULT_CHANNEL_FAILURE_CALLBACK.invoke(textChannel, it)
								else
									failure.apply(null)
							}
					)
				}
			}
		}

		val message = MessageBuilder()

		if (content is ParallaxEmbed) {
			message.setContent(" ")
			message.setEmbed(content.toDiscordEmbed())
		} else {
			message.setContent(content.toString())
		}

		return send(message.build(), options)
	}

	fun setName(name: String, reason: String? = null) {
		textChannel.manager.setName(name)
				.reason(reason)
				.complete()
	}

	fun setPosition(position: Int) {
		textChannel.manager.setPosition(position).complete()
	}

	@JvmOverloads
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