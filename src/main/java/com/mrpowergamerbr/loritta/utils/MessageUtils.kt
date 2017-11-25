package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent

fun Message.onReactionAdd(context: CommandContext, function: (MessageReactionAddEvent) -> Unit) {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionAdd = function
}

fun Message.onReactionRemove(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit) {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionRemove = function
}

fun Message.onResponse(context: CommandContext, function: (MessageReceivedEvent) -> Unit) {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponse = function
}

fun Message.onResponseByAuthor(context: CommandContext, function: (MessageReceivedEvent) -> Unit) {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponseByAuthor = function
}

fun Message.onMessageReceived(context: CommandContext, function: (MessageReceivedEvent) -> Unit) {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onMessageReceived = function
}

class MessageInteractionFunctions(val guild: String, val originalAuthor: String) {
	var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onResponse: ((MessageReceivedEvent) -> Unit)? = null
	var onResponseByAuthor: ((MessageReceivedEvent) -> Unit)? = null
	var onMessageReceived: ((MessageReceivedEvent) -> Unit)? = null
}