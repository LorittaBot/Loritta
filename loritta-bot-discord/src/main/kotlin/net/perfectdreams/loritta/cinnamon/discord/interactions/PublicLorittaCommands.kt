package net.perfectdreams.loritta.cinnamon.discord.interactions

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.SoundboxCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.PredefinedReasonsCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.FansExplainingCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.GigaChadCommand
import net.perfectdreams.loritta.common.locale.LanguageManager

/**
 * Public Loritta Commands
 *
 * They are in a separate class instead of staying within the [InteractionsManager] because this is also used in Showtime's Backend module to
 * get Loritta's command list!
 */
class PublicLorittaCommands(val languageManager: LanguageManager) {
    fun commands(): List<CinnamonSlashCommandDeclarationWrapper> {
        val wrapper = RegistryWrapper()
        with(wrapper) {
            // ===[ DISCORD ]===
            // register(ServerCommand(languageManager))
            // register(InviteCommand(languageManager))
            // register(EmojiCommand(languageManager))

            // ===[ MODERATION ]===
            register(BanCommand(languageManager))
            register(PredefinedReasonsCommand(languageManager))

            // ===[ FUN ]===
            register(TextTransformCommand(languageManager))
            register(SoundboxCommand(languageManager))

            // ===[ IMAGES ]===
            // register(DrakeCommand(languageManager))
            // register(SonicCommand(languageManager))
            // register(ArtCommand(languageManager))
            // register(BobBurningPaperCommand(languageManager))
            // register(BRMemesCommand(languageManager))
            // register(BuckShirtCommand(languageManager))
            // register(LoriSignCommand(languageManager))
            // register(PassingPaperCommand(languageManager))
            // register(PepeDreamCommand(languageManager))
            // register(PetPetCommand(languageManager))
            // register(WolverineFrameCommand(languageManager))
            // register(RipTvCommand(languageManager))
            // register(SustoCommand(languageManager))
            // register(GetOverHereCommand(languageManager))
            // register(NichijouYuukoPaperCommand(languageManager))
            // register(TrumpCommand(languageManager))
            // register(TerminatorAnimeCommand(languageManager))
            // register(ToBeContinuedCommand(languageManager))
            // register(InvertColorsCommand(languageManager))
            // register(MemeMakerCommand(languageManager))
            // register(MarkMetaCommand(languageManager))
            // register(DrawnMaskCommand(languageManager))

            // ===[ VIDEOS ]===
            register(FansExplainingCommand(languageManager))
            register(GigaChadCommand(languageManager))

            // ===[ UTILS ]===
            register(MorseCommand(languageManager))
            register(ChooseCommand(languageManager))
            register(ColorInfoCommand(languageManager))
            register(NotificationsCommand(languageManager))
            register(TranslateCommand(languageManager))

            // ===[ ECONOMY ]===
            register(SonhosCommand(languageManager))
            // register(BrokerCommand(languageManager))
            // register(BetCommand(languageManager))

            // ===[ SOCIAL ]===
            register(AchievementsCommand(languageManager))

//            register(AfkCommand(languageManager))
            register(GenderCommand(languageManager))

            // ===[ UNDERTALE ]===
            // register(UndertaleCommand(languageManager))
        }
        return wrapper.commands
    }

    class RegistryWrapper {
        val commands = mutableListOf<CinnamonSlashCommandDeclarationWrapper>()

        fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
            commands.add(declarationWrapper)
        }
    }
}