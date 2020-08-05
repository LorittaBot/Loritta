package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object BanInfoCommand {
    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("baninfo", "infoban"), CommandCategory.ADMIN) {
        description { it["commands.moderation.baninfo.description"] }

        arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
        }

        userRequiredPermissions = listOf(Permission.BAN_MEMBERS)
        botRequiredPermissions = listOf(Permission.BAN_MEMBERS)

        executesDiscord {
            val userID = args.getOrNull(0)
            val getAuthorAsMention = this.member!!.asMention

            if (userID == null || userID.isEmpty()) {
                reply(
                        LorittaReply(
                                locale["commands.userDoesNotExist", "`${userID}`"]
                        )
                )
            } else {
                val getBanInformations = userID.toLong().let { guild.retrieveBanById(it).await() }
                if (getBanInformations == null) {
                    reply(
                            LorittaReply(
                                    locale["commands.userDoesNotExist", args[0]]
                            )
                    )
                } else {
                    val embed = EmbedBuilder()
                            .setTitle(locale["commands.moderation.baninfo.title"])
                            .setThumbnail(getBanInformations.user.avatarUrl)
                            .addField(locale["commands.moderation.baninfo.user"], getBanInformations.user.asTag, false)
                            .addField(locale["commands.moderation.baninfo.reason"], "${getBanInformations.reason}", false)
                            .setColor(Constants.DISCORD_BLURPLE)
                    sendMessage(getAuthorAsMention, embed.build())
                }
            }
        }
    }
}