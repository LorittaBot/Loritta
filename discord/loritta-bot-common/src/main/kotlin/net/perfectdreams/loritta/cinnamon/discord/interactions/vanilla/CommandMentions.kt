package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.perfectdreams.loritta.morenitta.interactions.commands.DiscordCommand

/**
 * Holds Discord style command mentions of Loritta's commands.
 *
 * Mentions are automatically validated based on the commands registered. If a command doesn't exist, the class will fail to initialize!
 */
class CommandMentions(private val registeredCommands: List<DiscordCommand>) {
    val achievements = commandMention("achievements")

    val daily = commandMention("daily")

    val sonhosRank = commandMention("sonhos rank")

    val ban = commandMention("ban")

    val packageTrack = commandMention("package track")
    val packageList = commandMention("package list")

    val brokerInfo = commandMention("broker info")
    val brokerBuy = commandMention("broker buy")
    val brokerSell = commandMention("broker sell")
    val brokerPortfolio = commandMention("broker portfolio")

    val betCoinflipGlobal = commandMention("bet coinflip global")

    val webhookSendSimple = commandMention("webhook send simple")
    val webhookSendRepost = commandMention("webhook send repost")

    val eventJoin = commandMention("evento entrar")
    val eventStats = commandMention("evento stats")

    val profileView = commandMention("profile view")

    /**
     * Creates a command mention of [path]. If the command doesn't exist, an error will be thrown.
     */
    private fun commandMention(path: String): String {
        val rootCommandLabel = path.substringBefore(" ")

        val registeredCommand = registeredCommands.firstOrNull { it.name == rootCommandLabel } ?: error("Couldn't find a command with label $rootCommandLabel!")

        return "</${path}:${registeredCommand.id}>"
    }
}