package net.perfectdreams.loritta.premium.utils.extensions

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await() : T {
	return suspendCoroutine { cont ->
		this.queue({ cont.resume(it)}, { cont.resumeWithException(it) })
	}
}

suspend fun MessageChannel.sendMessageAsync(text: String) = this.sendMessage(text).await()
suspend fun MessageChannel.sendMessageAsync(message: Message) = this.sendMessage(message).await()
suspend fun MessageChannel.sendMessageAsync(embed: MessageEmbed) = this.sendMessage(embed).await()

suspend fun Message.edit(message: String, embed: MessageEmbed, clearReactions: Boolean = true): Message {
	return this.edit(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build(), clearReactions)
}

suspend fun Message.edit(content: Message, clearReactions: Boolean = true): Message {
	if (this.isFromType(ChannelType.PRIVATE) || !this.guild.selfMember.hasPermission(this.textChannel, Permission.MESSAGE_MANAGE)) {
		// Nós não podemos limpar as reações das mensagens caso a gente esteja em uma DM ou se a Lori não tem permissão para gerenciar mensagens
		// Nestes casos, iremos apenas deletar a mensagem e reenviar
		this.delete().queue()
		return this.channel.sendMessage(content).await()
	}

	// Se não, vamos apagar as reações e editar a mensagem atual!
	if (clearReactions)
		this.clearReactions().await()
	return this.editMessage(content).await()
}

/**
 * Adds the [emotes] to the [message] if needed, this avoids a lot of unnecessary API requests
 */
suspend fun Message.doReactions(vararg emotes: String): Message {
	var message = this

	var clearAll = false

	// Vamos pegar todas as reações que não deveriam estar aqui

	val invalidReactions = this.reactions.filterNot {
		if (it.reactionEmote.isEmote)
			emotes.contains(it.reactionEmote.name + ":" + it.reactionEmote.id)
		else
			emotes.contains(it.reactionEmote.name)
	}

	if (invalidReactions.isNotEmpty())
		clearAll = true

	// Se o número de reações for diferente das reações na mensagem, então algo está errado ;w;
	if (this.reactions.size != emotes.size)
		clearAll = true

	if (clearAll) { // Pelo visto tem alguns emojis que não deveriam estar aqui, vamos limpar!
		this.clearReactions().await() // Vamos limpar todas as reações
		message = this.refresh().await() // E pegar o novo obj da mensagem
	}

	emotes.forEach {
		// E agora vamos readicionar os emotes!
		message.addReaction(it).await()
	}
	return message
}

/**
 * Hacky workaround for JDA v4 support
 */
fun MessageReaction.ReactionEmote.isEmote(id: String): Boolean {
	return if (this.isEmote)
		this.id == id || this.name == id
	else
		this.name == id
}

fun Message.refresh(): RestAction<Message> {
	return this.channel.retrieveMessageById(this.idLong)
}

fun Guild.getTextChannelByNullableId(id: String?): TextChannel? {
	if (id == null)
		return null

	return this.getTextChannelById(id)
}

fun Guild.getVoiceChannelByNullableId(id: String?): VoiceChannel? {
	if (id == null)
		return null

	return this.getVoiceChannelById(id)
}