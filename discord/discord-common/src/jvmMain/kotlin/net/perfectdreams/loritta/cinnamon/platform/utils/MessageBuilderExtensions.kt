package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder

fun (MessageCreateBuilder.() -> Unit).toKordUserMessageCreateBuilder(): UserMessageCreateBuilder.() -> Unit = {
    // This is a hack! Maybe implement this in Discord InteraKTions somehow?
    val b = InteractionOrFollowupMessageCreateBuilder(false).apply(this@toKordUserMessageCreateBuilder)

    this.content = b.content
    this.allowedMentions = b.allowedMentions
    b.embeds?.let {
        this.embeds.addAll(it)
    }
    b.components?.let {
        this.components.addAll(it)
    }
    b.files?.let {
        this.files.addAll(it)
    }
}