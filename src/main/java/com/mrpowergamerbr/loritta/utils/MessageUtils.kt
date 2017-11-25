package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent

fun Message.onReactionAdd(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionAdd = function
	return this
}

fun Message.onReactionRemove(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionRemove = function
	return this
}

fun Message.onReactionAddByAuthor(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionAddByAuthor = function
	return this
}

fun Message.onReactionRemoveByAuthor(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionRemoveByAuthor = function
	return this
}

fun Message.onResponse(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponse = function
	return this
}

fun Message.onResponseByAuthor(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponseByAuthor = function
	return this
}

fun Message.onMessageReceived(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onMessageReceived = function
	return this
}

class MessageInteractionFunctions(val guild: String, val originalAuthor: String) {
	var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionAddByAuthor: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemoveByAuthor: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onResponse: ((MessageReceivedEvent) -> Unit)? = null
	var onResponseByAuthor: ((MessageReceivedEvent) -> Unit)? = null
	var onMessageReceived: ((MessageReceivedEvent) -> Unit)? = null
}