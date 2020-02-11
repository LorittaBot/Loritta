package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message

/**
 * Wrapper de uma mensagem de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
class NashornMessage(private val message: Message) {
	@NashornCommand.NashornDocs()
	fun getContent(): String {
		return message.contentDisplay
	}

	@NashornCommand.NashornDocs()
	fun getRawContent(): String {
		return message.contentRaw
	}

	@NashornCommand.NashornDocs()
	fun getStrippedContent(): String {
		return message.contentStripped
	}

	@NashornCommand.NashornDocs()
	fun getId(): String {
		return message.id
	}

	@NashornCommand.NashornDocs()
	fun getAuthor(): NashornUser {
		return NashornUser(message.author)
	}

	@NashornCommand.NashornDocs()
	fun getChannelType(): ChannelType {
		return message.channelType
	}

	@NashornCommand.NashornDocs()
	fun isEdited(): Boolean {
		return message.isEdited
	}

	@NashornCommand.NashornDocs()
	fun isPinned(): Boolean {
		return message.isPinned
	}

	@NashornCommand.NashornDocs()
	fun isTTS(): Boolean {
		return message.isTTS
	}

	@NashornCommand.NashornDocs()
	fun isWebhookMessage(): Boolean {
		return message.isWebhookMessage
	}

	@NashornCommand.NashornDocs(arguments = "user")
	fun isMentioned(user: NashornUser): Boolean {
		return message.isMentioned(user.user)
	}

	@NashornCommand.NashornDocs()
	fun mentionsEveryone(): Boolean {
		return message.mentionsEveryone()
	}

	@NashornCommand.NashornDocs()
	fun getMember(): NashornMember {
		return NashornMember(message.member!!)
	}

	@NashornCommand.NashornDocs()
	fun getMentionedUsers(): MutableList<NashornUser> {
		val mentionedUsers = mutableListOf<NashornUser>()

		message.mentionedUsers.forEach {
			mentionedUsers.add(NashornUser(it))
		}

		return mentionedUsers
	}

	@NashornCommand.NashornDocs(arguments = "mensagem")
	fun editMessage(texto: String) {
		if (texto.contains(loritta.discordConfig.discord.clientToken, true))
			NashornContext.securityViolation(null)

		message.editMessage(texto).queue()
	}

	@NashornCommand.NashornDocs(arguments = "reação")
	fun addReaction(texto: String) {
		val emotes = message.guild.getEmotesByName(texto, false)
		if (!emotes.isEmpty()) {
			message.addReaction(emotes[0]).queue()
		} else {
			message.addReaction(texto).queue()
		}
	}

	@NashornCommand.NashornDocs()
	fun delete() {
		message.delete().queue()
	}

	@NashornCommand.NashornDocs()
	fun getTextChannel(): NashornTextChannel {
		return NashornTextChannel(message.textChannel)
	}
}