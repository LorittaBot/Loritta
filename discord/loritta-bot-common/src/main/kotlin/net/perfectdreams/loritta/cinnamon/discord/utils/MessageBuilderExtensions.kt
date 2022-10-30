package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder

fun (MessageCreateBuilder.() -> Unit).toKordUserMessageCreateBuilder() =
    InteractionOrFollowupMessageCreateBuilder(false)
        .apply(this@toKordUserMessageCreateBuilder)
        .toKordUserMessageCreateBuilder()

fun MessageCreateBuilder.toKordUserMessageCreateBuilder(): UserMessageCreateBuilder.() -> Unit = {
    // This is a hack! Maybe implement this in Discord InteraKTions somehow?
    this.content = this@toKordUserMessageCreateBuilder.content
    this.allowedMentions = this@toKordUserMessageCreateBuilder.allowedMentions
    this@toKordUserMessageCreateBuilder.embeds?.let {
        this.embeds.addAll(it)
    }
    this@toKordUserMessageCreateBuilder.components?.let {
        this.components.addAll(it)
    }
    this@toKordUserMessageCreateBuilder.files?.let {
        this.files.addAll(it)
    }
}