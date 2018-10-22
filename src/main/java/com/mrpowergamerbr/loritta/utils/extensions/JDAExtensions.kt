package com.mrpowergamerbr.loritta.utils.extensions

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