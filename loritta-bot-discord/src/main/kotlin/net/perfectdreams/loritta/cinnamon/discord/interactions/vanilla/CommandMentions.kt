package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla

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

    val brokerInfo = commandMention("broker info")
    val brokerBuy = commandMention("broker buy")
    val brokerSell = commandMention("broker sell")
    val brokerPortfolio = commandMention("broker portfolio")

    val coinflipBetGlobal = commandMention("coinflipbetglobal")

    val webhookSendSimple = commandMention("webhook send simple")
    val webhookSendRepost = commandMention("webhook send repost")

    val eventJoin = commandMention("evento entrar")
    val eventStats = commandMention("evento stats")
    val eventInventory = commandMention("evento inventário")
    val eventLeave = commandMention("evento sair")

    val profileView = commandMention("profile view")

    val raffleStatus = commandMention("raffle status")
    val raffleBuy = commandMention("raffle buy")

    val verifyMessageUrl = commandMention("verificarmensagem url")
    val verifyMessageFile = commandMention("verificarmensagem arquivo")

    /**
     * Creates a command mention of [path]. If the command doesn't exist, an error will be thrown.
     */
    private fun commandMention(path: String): String {
        val rootCommandLabel = path.substringBefore(" ")

        val registeredCommand = registeredCommands.firstOrNull { it.name == rootCommandLabel } ?: error("Couldn't find a command with label $rootCommandLabel!")

        return "</${path}:${registeredCommand.id}>"
    }
}