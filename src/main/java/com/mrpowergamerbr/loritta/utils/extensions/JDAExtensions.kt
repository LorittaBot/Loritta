package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.Permission.*
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.requests.RestAction
import java.io.File
import java.io.InputStream
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

suspend fun MessageChannel.sendFileAsync(file: File) = this.sendFile(file).await()
suspend fun MessageChannel.sendFileAsync(file: File, fileName: String) = this.sendFile(file, fileName).await()
suspend fun MessageChannel.sendFileAsync(file: File, message: Message) = this.sendFile(file, message).await()
suspend fun MessageChannel.sendFileAsync(file: File, fileName: String, message: Message) = this.sendFile(file, fileName, message).await()
suspend fun MessageChannel.sendFileAsync(data: ByteArray, fileName: String) = this.sendFile(data, fileName).await()
suspend fun MessageChannel.sendFileAsync(data: ByteArray, fileName: String, message: Message) = this.sendFile(data, fileName).await()
suspend fun MessageChannel.sendFileAsync(data: InputStream, fileName: String, message: Message) = this.sendFile(data, fileName).await()

fun Permission.localized(locale: BaseLocale): String {
	return when (this) {
		CREATE_INSTANT_INVITE -> locale.format { discord.permissions.createInstantInvite }
		KICK_MEMBERS -> locale.format { discord.permissions.kickMembers }
		BAN_MEMBERS -> locale.format { discord.permissions.banMembers }
		ADMINISTRATOR -> locale.format { discord.permissions.administrator }
		MANAGE_CHANNEL -> locale.format { discord.permissions.manageChannel }
		MANAGE_SERVER -> locale.format { discord.permissions.manageServer }
		MESSAGE_ADD_REACTION -> locale.format { discord.permissions.addReactions }
		VIEW_AUDIT_LOGS -> locale.format { discord.permissions.viewAuditLogs }
		PRIORITY_SPEAKER -> TODO()
		VIEW_CHANNEL -> TODO()
		MESSAGE_READ -> locale.format { discord.permissions.messageRead }
		MESSAGE_WRITE -> locale.format { discord.permissions.messageWrite }
		MESSAGE_TTS -> TODO()
		MESSAGE_MANAGE -> locale.format { discord.permissions.messageManage }
		MESSAGE_EMBED_LINKS -> locale.format { discord.permissions.messageEmbedLinks }
		MESSAGE_ATTACH_FILES -> TODO()
		MESSAGE_HISTORY -> locale.format { discord.permissions.messageHistory }
		MESSAGE_MENTION_EVERYONE -> TODO()
		MESSAGE_EXT_EMOJI -> locale.format { discord.permissions.messageExtEmoji }
		VOICE_CONNECT -> TODO()
		VOICE_SPEAK -> TODO()
		VOICE_MUTE_OTHERS -> TODO()
		VOICE_DEAF_OTHERS -> TODO()
		VOICE_MOVE_OTHERS -> TODO()
		VOICE_USE_VAD -> TODO()
		NICKNAME_CHANGE -> TODO()
		NICKNAME_MANAGE -> TODO()
		MANAGE_ROLES -> locale.format { discord.permissions.manageRoles }
		MANAGE_PERMISSIONS -> locale.format { discord.permissions.managePermissions }
		MANAGE_WEBHOOKS -> locale.format { discord.permissions.manageWebhooks }
		MANAGE_EMOTES -> locale.format { discord.permissions.manageEmotes }
		UNKNOWN -> "This should never, ever happen!"
	}
}