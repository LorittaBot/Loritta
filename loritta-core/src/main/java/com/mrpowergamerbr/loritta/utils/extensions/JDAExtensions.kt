package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Permission.*
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
 * Edits the message, but only if the content was changed
 *
 * This reduces the number of API requests needed
 */
suspend fun Message.editMessageIfContentWasChanged(message: String): Message {
	if (this.contentRaw == message)
		return this

	return this.editMessage(message).await()
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

fun Permission.localized(locale: BaseLocale): String {
	return when (this) {
		CREATE_INSTANT_INVITE -> locale["discord.permissions.createInstantInvite"]
		KICK_MEMBERS -> locale["discord.permissions.kickMembers"]
		BAN_MEMBERS -> locale["discord.permissions.banMembers"]
		ADMINISTRATOR -> locale["discord.permissions.administrator"]
		MANAGE_CHANNEL -> locale["discord.permissions.manageChannel"]
		MANAGE_SERVER -> locale["discord.permissions.manageServer"]
		MESSAGE_ADD_REACTION -> locale["discord.permissions.addReactions"]
		VIEW_AUDIT_LOGS -> locale["discord.permissions.viewAuditLogs"]
		PRIORITY_SPEAKER -> locale["discord.permissions.prioritySpeaker"]
		VIEW_CHANNEL -> locale["discord.permissions.viewChannel"]
		MESSAGE_READ -> locale["discord.permissions.messageRead"]
		MESSAGE_WRITE -> locale["discord.permissions.messageWrite"]
		MESSAGE_TTS -> locale["discord.permissions.attachFiles"]
		MESSAGE_MANAGE -> locale["discord.permissions.messageManage"]
		MESSAGE_EMBED_LINKS -> locale["discord.permissions.messageEmbedLinks"]
		MESSAGE_ATTACH_FILES -> locale["discord.permissions.attachFiles"]
		MESSAGE_HISTORY -> locale["discord.permissions.messageHistory"]
		MESSAGE_MENTION_EVERYONE -> locale["discord.permissions.mentionEveryone"]
		MESSAGE_EXT_EMOJI -> locale["discord.permissions.messageExtEmoji"]
		VOICE_CONNECT -> locale["discord.permissions.connect"]
		VOICE_SPEAK -> locale["discord.permissions.speak"]
		VOICE_MUTE_OTHERS -> locale["discord.permissions.muteVoiceMembers"]
		VOICE_DEAF_OTHERS -> locale["discord.permissions.disableVoiceAudio"]
		VOICE_MOVE_OTHERS -> locale["discord.permissions.moveVoiceMembers"]
		VOICE_USE_VAD -> locale["discord.permissions.useVoiceDetection"]
		NICKNAME_CHANGE -> locale["discord.permissions.changeNickname"]
		NICKNAME_MANAGE -> locale["discord.permissions.manageNicknames"]
		MANAGE_ROLES -> locale["discord.permissions.manageRoles"]
		MANAGE_PERMISSIONS -> locale["discord.permissions.managePermissions"]
		MANAGE_WEBHOOKS -> locale["discord.permissions.manageWebhooks"]
		MANAGE_EMOTES -> locale["discord.permissions.manageEmotes"]
		VOICE_STREAM -> locale["discord.permissions.voiceStream"]
		UNKNOWN -> "This should never, ever happen!"
	}
}