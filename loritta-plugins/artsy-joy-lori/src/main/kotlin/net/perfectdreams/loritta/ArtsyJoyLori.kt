package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.actions.modern.impl.*
import net.perfectdreams.loritta.commands.administration.*
import net.perfectdreams.loritta.commands.discord.*
import net.perfectdreams.loritta.commands.economy.*
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
        registerCommand(DashboardCommand(loritta))
        registerCommand(RenameChannelCommand(loritta))

        // DISCORD
        registerCommand(RoleInfoCommand(loritta))
        registerCommand(RenameEmojiCommand(loritta))
        registerCommand(GuildBannerCommand(loritta))

        // IMAGES
        registerCommand(AtendenteCommand(loritta))
        registerCommand(TristeRealidadeCommand(loritta))
        registerCommand(AsciiCommand(loritta))
        registerCommand(DrawnWordCommand(loritta))
        registerCommand(EmojiMashupCommand(loritta))

        // ACTIONS
        registerCommand(AttackCommand(loritta))
        registerCommand(DanceCommand(loritta))
        registerCommand(HugCommand(loritta))
        registerCommand(KissCommand(loritta))
        registerCommand(SlapCommand(loritta))
        registerCommand(HeadPatCommand(loritta))
        registerCommand(ScratchCardCommand(loritta))
        registerCommand(ScratchCardTopCommand(loritta))

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Raspadinhas
            )
        }
    }
}
