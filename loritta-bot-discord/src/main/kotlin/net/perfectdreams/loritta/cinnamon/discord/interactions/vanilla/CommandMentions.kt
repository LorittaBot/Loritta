package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.perfectdreams.loritta.morenitta.interactions.commands.DiscordCommand

/**
 * Holds Discord style command mentions of Loritta's commands.
 *
 * Mentions are automatically validated based on the commands registered. If a command doesn't exist, the class will fail to initialize!
 */
class
CommandMentions(private val registeredCommands: List<DiscordCommand>) {
    val achievements = commandMention("conquistas")

    val daily = commandMention("daily")

    val sonhosAtm = commandMention("sonhos atm")
    val sonhosRank = commandMention("sonhos rank")

    val ban = commandMention("ban")

    val brokerInfo = commandMention("corretora info")
    val brokerBuy = commandMention("corretora comprar")
    val brokerSell = commandMention("corretora vender")
    val brokerPortfolio = commandMention("corretora portfólio")

    val coinflipBet = commandMention("coinflipbet")
    val coinflipBetGlobal = commandMention("coinflipbetglobal")
    val emojiFightStart = commandMention("emojifight iniciar")

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
    val loriCoolCardsOpen = commandMention("figurittas abrir")
    val loriCoolCardsView = commandMention("figurittas ver")
    val loriCoolCardsTrade = commandMention("figurittas trocar")
    val loriCoolCardsGive = commandMention("figurittas dar")
    val loriCoolCardsCompare = commandMention("figurittas comparar")

    val roleplayKiss = commandMention("roleplay beijar")
    val roleplayHug = commandMention("roleplay abraçar")
    val roleplayHeadPat = commandMention("roleplay cafuné")

    val marriageMarry = commandMention("casamento casar")
    val marriageView = commandMention("casamento ver")
    val marriageLetter = commandMention("casamento cartinha")
    val marriageRank = commandMention("casamento rank")

    /**
     * Creates a command mention of [path]. If the command doesn't exist, an error will be thrown.
     */
    private fun commandMention(path: String): String {
        // This seems weird, but hear me out:
        // In the past we did use the command label here, which is english
        // However, this is bad because all of the strings are first created in portuguese, then translated to english
        // So a command that was not translated yet to english WILL cause issues after it is translated
        // (ESPECIALLY if it is being used as a command mention!)
        //
        // However, because we are relying on the portuguese version of the commands, we need to get the original canonical english name
        val commandParts = path.split(" ")
        when (commandParts.size) {
            1 -> {
                val rootLabel = commandParts[0]
                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                return "</$rootLabel:${registeredCommand.id}>"
            }
            2 -> {
                val rootLabel = commandParts[0]
                val subcommandLabel = commandParts[1]

                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                val subcommand = registeredCommand.subcommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandLabel } ?: error("Couldn't find a subcommand with label $subcommandLabel!")

                return "</${registeredCommand.name} ${subcommand.name}:${registeredCommand.id}>"
            }
            3 -> {
                val rootLabel = commandParts[0]
                val subcommandGroupLabel = commandParts[1]
                val subcommandLabel = commandParts[2]

                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                val subcommandGroup = registeredCommand.subcommandGroups.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandGroupLabel } ?: error("Couldn't find a subcommand group with label $subcommandGroupLabel!")
                val subcommand = subcommandGroup.subcommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandLabel } ?: error("Couldn't find a subcommand with label $subcommandLabel!")

                return "</${registeredCommand.name} ${subcommandGroup.name} ${subcommand.name}:${registeredCommand.id}>"
            }
            else -> error("Unsupported command format for $path, there are ${commandParts.size} parts, only 1, 2 or 3 are supported!")
        }
    }
}