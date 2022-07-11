package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.actions.modern.impl.AttackCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.DanceCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.HeadPatCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.HugCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.KissCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.SlapCommand
import net.perfectdreams.loritta.commands.administration.DashboardCommand
import net.perfectdreams.loritta.commands.administration.RenameChannelCommand
import net.perfectdreams.loritta.commands.administration.RenameEmojiCommand
import net.perfectdreams.loritta.commands.discord.GuildBannerCommand
import net.perfectdreams.loritta.commands.discord.RoleInfoCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardTopCommand
import net.perfectdreams.loritta.commands.images.AsciiCommand
import net.perfectdreams.loritta.commands.images.AtendenteCommand
import net.perfectdreams.loritta.commands.images.DrawnWordCommand
import net.perfectdreams.loritta.commands.images.EmojiMashupCommand
import net.perfectdreams.loritta.commands.images.TristeRealidadeCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Raspadinhas
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("deprecation")
class ArtsyJoyLori(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {

    override fun onEnable() {
        val loritta = loritta as LorittaDiscord

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

        if (System.getenv("LORITTA_CREATE_TABLES") != null) {
            transaction(Databases.loritta) {
                SchemaUtils.createMissingTablesAndColumns(
                    Raspadinhas
                )
            }
        }
    }
}
