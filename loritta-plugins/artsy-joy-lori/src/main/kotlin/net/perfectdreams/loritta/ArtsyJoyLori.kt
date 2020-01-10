package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import net.perfectdreams.loritta.commands.actions.*
import net.perfectdreams.loritta.commands.administration.DashboardCommand
import net.perfectdreams.loritta.commands.administration.RenameChannelCommand
import net.perfectdreams.loritta.commands.administration.RoleColorCommand
import net.perfectdreams.loritta.commands.discord.GuildBannerCommand
import net.perfectdreams.loritta.commands.discord.RenameEmojiCommand
import net.perfectdreams.loritta.commands.discord.RoleInfoCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardCommand
import net.perfectdreams.loritta.commands.economy.ScratchCardTopCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.tables.Raspadinhas
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class ArtsyJoyLori : LorittaPlugin() {
    override fun onEnable() {
        // ADMIN
        registerCommand(DashboardCommand())
        registerCommand(RenameChannelCommand())
        if (loritta.config.loritta.environment == EnvironmentType.CANARY)
            registerCommand(RoleColorCommand())

        // DISCORD
        registerCommand(RoleInfoCommand())
        registerCommand(RenameEmojiCommand())
        registerCommand(GuildBannerCommand())

        // IMAGES
        registerCommand(AtendenteCommand())
        registerCommand(TristeRealidadeCommand())
        registerCommand(AsciiCommand())
        registerCommand(DrawnWordCommand())
        registerCommand(BriggsCapaCommand())
        registerCommand(BolsonaroCommand())
        registerCommand(SustoCommand())
        registerCommand(AtaCommand())
        registerCommand(LoriAtaCommand())
        registerCommand(StudiopolisTvCommand())
        registerCommand(RomeroBrittoCommand())
        registerCommand(QuadroCommand())
        registerCommand(PepeDreamCommand())
        registerCommand(LoriSignCommand())
        registerCommand(ArtCommand())
        registerCommand(ChicoAtaCommand())
        registerCommand(CanellaDvdCommand())
        registerCommand(GessyAtaCommand())
        registerCommand(ToBeContinuedCommand())
        registerCommand(EmojiMashupCommand())
        registerCommand(BuckShirtCommand())
        registerCommand(BobBurningPaperCommand())
        registerCommand(PassingPaperCommand())
        if (false)
            registerCommand(CoringaCommand())

        // ACTIONS
        registerCommand(AttackCommand())
        registerCommand(DanceCommand())
        registerCommand(HugCommand())
        registerCommand(KissCommand())
        registerCommand(SlapCommand())
        registerCommand(HeadPatCommand())
        if (false)
            registerCommand(HighFiveCommand())
        registerCommand(ScratchCardCommand())
        registerCommand(ScratchCardTopCommand())
        registerCommand(Bolsonaro2Command())

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Raspadinhas
            )
        }
    }
}
