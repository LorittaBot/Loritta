package net.perfectdreams.loritta.morenitta.utils.extensions

import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.api.entities.Message
import net.perfectdreams.loritta.morenitta.api.entities.User
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordMessage
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser

/**
 * Converts a Loritta API [Message] to a JDA [net.perfectdreams.loritta.deviousfun.entities.Message]
 *
 * **Attention!** This will make the code not multiplatform! Do not use it if you don't know what you are doing!
 */
fun Message.toJDA() = (this as DiscordMessage).handle

/**
 * Converts a Loritta API [User] to a JDA [net.perfectdreams.loritta.deviousfun.entities.User]
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
fun LorittaReply.build(user: net.perfectdreams.loritta.deviousfun.entities.User) = this.build(JDAUser(user))