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
            val userid = args.getOrNull(0) ?: explainAndExit()
            val authorAsMention = this.member!!.asMention
            val banInformations = userid.toLong().let { guild.retrieveBanById(it).await() }

            if (banInformations == null) {
                reply(
                        LorittaReply(
                                locale["commands.userDoesNotExist", args[0]]
                        )
                )
            } else {
                val embed = EmbedBuilder()
                        .setTitle(locale["commands.moderation.baninfo.title"])
                        .setThumbnail(banInformations.user.avatarUrl)
                        .addField(locale["commands.moderation.baninfo.user"], banInformations.user.asTag, false)
                        .addField(locale["commands.moderation.baninfo.reason"], "${banInformations.reason}", false)
                        .setColor(Constants.DISCORD_BLURPLE)
                sendMessage(authorAsMention, embed.build())
            }
        }
    }
}