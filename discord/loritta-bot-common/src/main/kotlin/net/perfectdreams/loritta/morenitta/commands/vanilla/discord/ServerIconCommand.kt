package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class ServerIconCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "servericon",
    listOf(
        "guildicon",
        "iconeserver",
        "iconeguild",
        "iconedoserver",
        "iconedaguild",
        "íconedoserver",
        "iconedoservidor",
        "íconeguild",
        "íconedoserver",
        "íconedaguild",
        "íconedoservidor"
    ),
    category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD
) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.servericon"
    }

    override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
    override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "server icon")

        var guild: Guild? = null

        var guildId = context.guild.idLong

        if (context.rawArgs.isNotEmpty()) {
            val id = context.rawArgs.first()
            if (id.isValidSnowflake()) {
                guildId = id.toLong()
                // TODO: This must retrieve from other clusters... Maybe readd the get guild endpoint?
                guild = loritta.deviousShards.getGuildById(Snowflake(context.args[0]))
            }
        } else {
            guild = loritta.deviousShards.getGuildById(context.guild.idSnowflake)
        }

        if (guild == null) {
            context.reply(
                LorittaReply(
                    context.locale["commands.guildDoesNotExist", guildId],
                    Emotes.LORI_HM
                )
            )
            return
        }

        val name = guild.name
        val iconUrl = guild.iconUrl

        if (iconUrl == null) {
            context.reply(
                LorittaReply(
                    message = context.locale["$LOCALE_PREFIX.noIcon", Emotes.LORI_PAT],
                    prefix = Constants.ERROR
                )
            )
            return
        }

        val embed = EmbedBuilder()
        embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
        val description = "**${context.locale["${AvatarCommand.LOCALE_PREFIX}.clickHere", "$iconUrl?size=2048"]}**"

        val guildIconUrl = iconUrl

        embed.setDescription(description)
        embed.setImage(iconUrl) // Ícone da Guild
        embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
        embed.setTitle("<:discord:314003252830011395> ${name}", null) // Nome da Guild
        embed.setImage("${guildIconUrl.replace("jpg", "png")}?size=2048")

        context.sendMessage(
            context.getAsMention(true),
            embed.build()
        ) // phew, agora finalmente poderemos enviar o embed!
    }
}