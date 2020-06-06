package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.actions.*
import net.perfectdreams.loritta.commands.administration.DashboardCommand
import net.perfectdreams.loritta.commands.administration.RenameChannelCommand
import net.perfectdreams.loritta.commands.discord.GuildBannerCommand
import net.perfectdreams.loritta.commands.discord.RenameEmojiCommand
import net.perfectdreams.loritta.commands.discord.RoleInfoCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardTopCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.tables.Raspadinhas
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class ArtsyJoyLori(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
    override fun onEnable() {
        // ADMIN
        registerCommand(DashboardCommand())
        registerCommand(RenameChannelCommand())

        // DISCORD
        registerCommand(RoleInfoCommand())
        registerCommand(RenameEmojiCommand())
        registerCommand(GuildBannerCommand())

        // IMAGES
        registerCommand(AtendenteCommand())
        registerCommand(TristeRealidadeCommand())
        registerCommand(AsciiCommand())
        registerCommand(DrawnWordCommand())
        registerCommand(EmojiMashupCommand())

        // ACTIONS
        registerCommand(AttackCommand())
        registerCommand(BiteCommand())
        registerCommand(DanceCommand())
        registerCommand(HugCommand())
        registerCommand(KissCommand())
        registerCommand(SlapCommand())
        registerCommand(HeadPatCommand())
        if (false)
            registerCommand(HighFiveCommand())
        registerCommand(ScratchCardCommand())
        registerCommand(ScratchCardTopCommand())

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Raspadinhas
            )
        }
    }
}
