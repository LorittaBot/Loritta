package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.Permission.*
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.requests.RestAction
import java.io.File
import java.io.InputStream
import java.lang.ref.Reference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await() : T {
	return suspendCoroutine { cont ->
		this.queue({ cont.resume(it) }, { cont.resumeWithException(it) })
	}
}

suspend fun MessageChannel.sendMessageAsync(text: String) = this.sendMessage(text).await()
suspend fun MessageChannel.sendMessageAsync(message: Message) = this.sendMessage(message).await()
suspend fun MessageChannel.sendMessageAsync(embed: MessageEmbed) = this.sendMessage(embed).await()

suspend fun MessageChannel.sendFileAsync(file: File) = this.sendFile(file).await()
suspend fun MessageChannel.sendFileAsync(file: File, fileName: String) = this.sendFile(file, fileName).await()
suspend fun MessageChannel.sendFileAsync(file: File, message: Message) = this.sendFile(file, message).await()
suspend fun MessageChannel.sendFileAsync(file: File, fileName: String, message: Message) = this.sendFile(file, fileName, message).await()
suspend fun MessageChannel.sendFileAsync(data: ByteArray, fileName: String) = this.sendFile(data, fileName).await()
suspend fun MessageChannel.sendFileAsync(data: ByteArray, fileName: String, message: Message) = this.sendFile(data, fileName).await()
suspend fun MessageChannel.sendFileAsync(data: InputStream, fileName: String, message: Message) = this.sendFile(data, fileName).await()

suspend fun MessageChannel.sendFileAsyncHoldReference(data: InputStream, fileName: String, message: Message) : Message {
	Reference.reachabilityFence(data) // https://cdn.discordapp.com/attachments/358774895850815488/554480010363273217/unknown.png
	return suspendCoroutine { cont ->
		Reference.reachabilityFence(data) // https://cdn.discordapp.com/attachments/358774895850815488/554480010363273217/unknown.png
		this.sendFile(data, fileName, message).queue({
			Reference.reachabilityFence(data) // https://cdn.discordapp.com/attachments/358774895850815488/554480010363273217/unknown.png
			cont.resume(it)
		}, { cont.resumeWithException(it) })
	}
}

suspend fun Message.edit(message: String, embed: MessageEmbed): Message {
	return this.edit(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build())
}

suspend fun Message.edit(content: Message): Message {
	if (this.isFromType(ChannelType.PRIVATE) || !this.guild.selfMember.hasPermission(this.textChannel, Permission.MESSAGE_MANAGE)) {
		// Nós não podemos limpar as reações das mensagens caso a gente esteja em uma DM ou se a Lori não tem permissão para gerenciar mensagens
		// Nestes casos, iremos apenas deletar a mensagem e reenviar
		this.delete().queue()
		return this.channel.sendMessage(content).await()
	}

	// Se não, vamos apagar as reações e editar a mensagem atual!
	this.clearReactions().await()
	this.editMessage(content).await()
	return this
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
		UNKNOWN -> "This should never, ever happen!"
	}
}