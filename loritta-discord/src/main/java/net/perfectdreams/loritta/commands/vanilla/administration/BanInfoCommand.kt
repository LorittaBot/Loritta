package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorResponseException
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
            val userId = args.getOrNull(0) ?: explainAndExit()
            val authorAsMention = this.member!!.asMention


            if (userId.isValidSnowflake()) {
                try {
                    val banInformation = userId.let { guild.retrieveBanById(it.toLong()).await() }
                    val embed = EmbedBuilder()
                            .setTitle(locale["commands.moderation.baninfo.title"])
                            .setThumbnail(banInformation.user.avatarUrl)
                            .addField(locale["commands.moderation.baninfo.user"], banInformation.user.asTag, false)
                            .addField(locale["commands.moderation.baninfo.reason"], "${banInformation.reason}", false)
                            .setColor(Constants.DISCORD_BLURPLE)
                    sendMessage(authorAsMention, embed.build())
                } catch(e: ErrorResponseException) {
                    if (e.errorCode == 10026) {
                        fail(locale["commands.moderation.baninfo.banDoesNotExist"])
                    }
                }
            } else {
                fail(locale["commands.userDoesNotExist", userId])
            }
        }
    }
}