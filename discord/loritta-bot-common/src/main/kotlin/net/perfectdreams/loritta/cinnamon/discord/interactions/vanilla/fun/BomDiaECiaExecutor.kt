package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatches
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val text = string("text", TodoFixThisData)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val text = args[options.text]

        val currentActiveBomDiaECia = context.loritta.pudding.transaction {
            BomDiaECiaMatches.selectAll()
                .orderBy(BomDiaECiaMatches.id, SortOrder.DESC)
                .limit(1)
                .first()
        }

        // Wrong
        if (text != currentActiveBomDiaECia[BomDiaECiaMatches.text]) {
            context.sendEphemeralMessage {
                content = "Texto errado!"
            }
            return
        }

        // Correct
        context.sendEphemeralMessage {
            content = "oloco você ganhou"
        }

        // TODO: Get current active bom dia & cia
        // TODO: Trigger win/lose scenario, if needed
        // TODO:
        context.sendEphemeralMessage {
            content = "oloco"
        }
    }
}