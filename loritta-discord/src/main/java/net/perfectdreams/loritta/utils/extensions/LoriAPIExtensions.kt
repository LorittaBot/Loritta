package net.perfectdreams.loritta.utils.extensions

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser

/**
 * Converts a Loritta API [Message] to a JDA [net.dv8tion.jda.api.entities.Message]
 *
 * **Attention!** This will make the code not multiplatform! Do not use it if you don't know what you are doing!
 */
fun Message.toJDA() = (this as DiscordMessage).handle

/**
 * Converts a Loritta API [User] to a JDA [net.dv8tion.jda.api.entities.User]
 *
 * **Attention!** This will make the code not multiplatform! Do not use it if you don't know what you are doing!
 */
fun User.toJDA() = (this as JDAUser).handle