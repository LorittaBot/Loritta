package net.perfectdreams.loritta.utils.extensions

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser

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

/**
 * Builds a reply based on the current [context]
 *
 * Shouldn't be used! [CommandContext] is deprecated! Use this only if you know what you are doing!!
 *
 * @param context the current context
 * @return        the reply as a string
 */
fun LorittaReply.build(context: CommandContext) = this.build(JDAUser(context.userHandle))

/**
 * Builds a reply based on the user [user]
 *
 * @param user the user that will be replied to
 * @return     the reply as a string
 */
fun LorittaReply.build(user: net.dv8tion.jda.api.entities.User) = this.build(JDAUser(user))