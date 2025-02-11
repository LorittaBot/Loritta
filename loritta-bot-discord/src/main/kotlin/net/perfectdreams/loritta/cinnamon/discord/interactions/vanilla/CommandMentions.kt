package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.perfectdreams.loritta.morenitta.interactions.commands.DiscordCommand

/**
 * Holds Discord style command mentions of Loritta's commands.
 *
 * Mentions are automatically validated based on the commands registered. If a command doesn't exist, the class will fail to initialize!
 */
class CommandMentions(private val registeredCommands: List<DiscordCommand>) {
    val achievements = commandMention("conquistas")

    val daily = commandMention("daily")

    val sonhosAtm = commandMention("sonhos atm")
    val sonhosRank = commandMention("sonhos rank")

    val ban = commandMention("ban")

    val brokerInfo = commandMention("corretora info")
    val brokerBuy = commandMention("corretora buy")
    val brokerSell = commandMention("corretora sell")
    val brokerPortfolio = commandMention("corretora portfolio")

    val coinflipBetGlobal = commandMention("coinflipbetglobal")

    val webhookSendSimple = commandMention("webhook enviar simples")
    val webhookSendRepost = commandMention("webhook enviar repostar")

    val eventJoin = commandMention("evento entrar")
    val eventStats = commandMention("evento stats")
    val eventInventory = commandMention("evento inventário")
    val eventLeave = commandMention("evento sair")

    val profileView = commandMention("perfil ver")

    val raffleStatus = commandMention("rifa status")
    val raffleBuy = commandMention("rifa comprar")

    val verifyMessageUrl = commandMention("verificarmensagem url")
    val verifyMessageFile = commandMention("verificarmensagem arquivo")

    val loriCoolCardsBuy = commandMention("figurittas comprar")
    val loriCoolCardsStick = commandMention("figurittas colar")

    /**
     * Creates a command mention of [path]. If the command doesn't exist, an error will be thrown.
     */
    private fun commandMention(path: String): String {
        val rootCommandLabel = path.substringBefore(" ")

        // This seems weird, but hear me out:
        // In the past we did use the command label here, which is english
        // However, this is bad because all of the strings are first created in portuguese, then translated to english
        // So a command that was not translated yet to english WILL cause issues after it is translated
        // (ESPECIALLY if it is being used as a command mention!)
        // So now we use the localized portuguese label :3
        val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootCommandLabel } ?: error("Couldn't find a command with label $rootCommandLabel!")

        return "</${path}:${registeredCommand.id}>"
    }
}