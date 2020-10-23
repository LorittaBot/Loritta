package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.actions.modern.impl.*
import net.perfectdreams.loritta.commands.administration.DashboardCommand
import net.perfectdreams.loritta.commands.administration.RenameChannelCommand
import net.perfectdreams.loritta.commands.discord.GuildBannerCommand
import net.perfectdreams.loritta.commands.discord.RenameEmojiCommand
import net.perfectdreams.loritta.commands.discord.RoleInfoCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardTopCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Raspadinhas
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("deprecation")
class ArtsyJoyLori(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {

    override fun onEnable() {
        loritta as LorittaDiscord

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
        registerCommand(AttackCommand(loritta))
        registerCommand(DanceCommand(loritta))
        registerCommand(HugCommand(loritta))
        registerCommand(KissCommand(loritta))
        registerCommand(SlapCommand(loritta))
        registerCommand(HeadPatCommand(loritta))
        // registerCommand(HighFiveCommand(loritta))
        registerCommand(ScratchCardCommand())
        registerCommand(ScratchCardTopCommand())

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Raspadinhas
            )
        }
    }
}
