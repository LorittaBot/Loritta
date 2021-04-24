package net.perfectdreams.loritta.platform.discord.utils

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.DateUtils
import net.perfectdreams.loritta.common.utils.embed.LorittaColor
import net.perfectdreams.loritta.discord.command.DiscordCommandContext
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel
import net.perfectdreams.loritta.discord.util.optionalChannel
import net.perfectdreams.loritta.platform.discord.DiscordCommandExecutor
import net.perfectdreams.loritta.platform.discord.utils.declarations.ChannelInfoCommand

class ChannelInfoExecutor(val emotes: Emotes) : DiscordCommandExecutor() {
    companion object : CommandExecutorDeclaration(ChannelInfoExecutor::class) {
        object Options : CommandOptions() {
            val channelId = optionalChannel("channel", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun executeDiscord(context: DiscordCommandContext, args: CommandArguments) {
        val channel = args[options.channelId] ?: context.channel

        if (channel as? LorittaDiscordMessageChannel == null || channel.guildId == null) {
            return context.sendReply("Eu não consegui identificar o canal de texto que você está procurando!", ":no_entry:") {
                isEphemeral = true
            }
        }

        context.sendEmbed {
            body {
                title = "\uD83D\uDC81 ${context.locale["${ChannelInfoCommand.LOCALE_PREFIX}.channelInfo", "#${channel.name}"]}"
                description = if (channel.topic != null) "```${channel.topic}```" else "Tópico não definido!"
                color = LorittaColor.DISCORD_BLURPLE
            }
            field("\uD83D\uDD39 ${context.locale["${ChannelInfoCommand.LOCALE_PREFIX}.channelMention"]}", "`${channel.asMention}`") {
                inline = true
            }
            field("\uD83D\uDCBB ${context.locale["commands.command.userinfo.discordId"]}", "`${channel.id}`") {
                inline = true
            }
            field("\uD83D\uDD1E NSFW", if (channel.nsfw == true) context.locale["loritta.fancyBoolean.true"] else context.locale["loritta.fancyBoolean.false"]) {
                inline = true
            }
            field("\uD83D\uDCC5 ${context.locale["${ChannelInfoCommand.LOCALE_PREFIX}.channelCreated"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(channel.creation.toEpochMilliseconds(), context.locale)) {
                inline = true
            }
            field("\uD83D\uDD39 Guild", "`${channel.guildId}`") {
                inline = true
            }
        }
    }
}