package net.perfectdreams.loritta.commands.administration

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class RenameChannelCommand : LorittaDiscordCommand(arrayOf("renamechannel", "renomearcanal"), CommandCategory.ADMIN) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.moderation.renamechannel.description"]
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.TEXT) {}
            argument(ArgumentType.TEXT) {}
        }
    }

    override val botPermissions: List<Permission> = listOf(Permission.MANAGE_CHANNEL)
    override val discordPermissions: List<Permission> = listOf(Permission.MANAGE_CHANNEL)

    override fun getExamples(locale: BaseLocale): List<String> {
        return listOf(
                "#lori-é-fofis lori é fofis",
                "297732013006389252 bate-papo",
                "lorota-pantufa lorota & pantufa",
                "bate-papo \uD83D\uDE0E | bate-papo"
        )
    }

    override val canUseInPrivateChannel: Boolean = false

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (context.args.isEmpty() || context.args.size < 2) {
            context.explain()
            return
        }
        val textChannel: TextChannel? = if (context.discordMessage.mentionedChannels.firstOrNull() != null) {
            context.discordMessage.mentionedChannels.firstOrNull()
        } else if (context.args[0].isValidSnowflake() && context.event.guild!!.getTextChannelById(context.args[0]) != null) {
            context.event.guild!!.getTextChannelById(context.args[0])
        } else if (context.event.guild!!.getTextChannelsByName(context.args[0], true).isNotEmpty()) {
            context.event.guild!!.getTextChannelsByName(context.args[0], true).first()
        } else if (context.event.guild!!.textChannels.filter { it.name.contains(context.args[0], true) }.isNotEmpty()) {
            context.event.guild!!.textChannels.filter { it.name.contains(context.args[0], true) }.first()
        } else {
            null
        }
        val voiceChannel: VoiceChannel? = if (context.args[0].isValidSnowflake() && context.event.guild!!.getVoiceChannelById(context.args[0]) != null) {
            context.event.guild!!.getVoiceChannelById(context.args[0])
        } else if (context.event.guild!!.getVoiceChannelsByName(context.args[0], true).isNotEmpty()) {
            context.event.guild!!.getVoiceChannelsByName(context.args[0], true).first()
        } else if (context.event.guild!!.voiceChannels.filter { it.name.contains(context.args[0], true) }.isNotEmpty()) {
            context.event.guild!!.voiceChannels.filter { it.name.contains(context.args[0], true) }.first()
        } else {
            null
        }

        if (textChannel == null && voiceChannel == null) {
            context.reply(locale["commands.moderation.renamechannel.channelNotFound"], Constants.ERROR)
            return
        }

        val newNameArgs = context.args.drop(1)

        val toRename = newNameArgs.joinToString(" ")
                .trim()
                .replace("(\\s\\|\\s|\\|)".toRegex(), "│")
                .replace("(\\s&\\s|&)".toRegex(), "＆")
                .replace("[\\s]".toRegex(), "-")

        try {
            if (textChannel != null && voiceChannel == null) {
                val manager = textChannel.manager
                        .setName(toRename)
                        .queue()

                val replies = mutableListOf(
                        LorittaReply(
                                locale["commands.moderation.renamechannel.successfullyRenamed"],
                                "\uD83C\uDF89"
                        )
                )

                context.reply(*replies.toTypedArray())
            } else if (voiceChannel != null && textChannel == null) {
                voiceChannel.manager.setName(context.args.drop(1).joinToString(" ").trim()).queue()
                context.reply(locale["commands.moderation.renamechannel.successfullyRenamed"], "\uD83C\uDF89")
            } else {
                context.reply(locale["commands.moderation.renamechannel.channelConflict"], Constants.ERROR)
            }
        } catch(e: Exception) {
            context.reply(locale["commands.moderation.renamechannel.cantRename"], Constants.ERROR)
        }
    }
}
