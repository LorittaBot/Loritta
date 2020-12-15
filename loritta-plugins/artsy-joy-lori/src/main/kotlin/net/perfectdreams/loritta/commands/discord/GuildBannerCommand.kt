package net.perfectdreams.loritta.commands.discord

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class GuildBannerCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("guildbanner", "serverbanner"), CommandCategory.ADMIN) {
    override fun command() = create {
        localizedDescription("commands.discord.guildbanner.description")

        arguments {
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        canUseInPrivateChannel = false

        executesDiscord {
            val context = this

            var guild: Guild? = null

            if (context.args.isNotEmpty()) {
                val id = context.args.first()
                if (id.isValidSnowflake()) {
                    guild = lorittaShards.getGuildById(context.args[0])
                }
            } else {
                guild = lorittaShards.getGuildById(context.guild.idLong)
            }
            // Verificar se a guild existe
            if (guild == null) {
                context.reply(
                        LorittaReply(
                                message = context.locale["commands.discord.serverinfo.unknownGuild", context.args[0]],
                                prefix = Constants.ERROR
                        )
                )
            }

            // Verificar se a guild tem banner
            val guildBanner = guild!!.bannerUrl ?: fail(locale["commands.discord.guildbanner.noBanner"])

            val embed = EmbedBuilder()

            embed.setTitle("🖼 ${guild.name}")
            embed.setColor(Constants.DISCORD_BLURPLE)
            embed.setDescription(locale["loritta.clickHere", "$guildBanner?size=2048"])
            embed.setImage("$guildBanner?size=2048")

            sendMessage(embed.build())
        }
    }
}
