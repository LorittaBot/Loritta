package net.perfectdreams.loritta.cinnamon.discord.interactions

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.declarations.RobloxCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay.declarations.RoleplayCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.*

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
            register(UserCommand(languageManager))

            register(ServerCommand(languageManager))
            register(InviteCommand(languageManager))
            register(EmojiCommand(languageManager))
            register(WebhookCommand(languageManager))
            register(LorittaCommand(languageManager))

            // ===[ FUN ]===
            register(CoinFlipCommand(languageManager))
            register(RateCommand(languageManager))
            register(ShipCommand(languageManager))
            register(CancelledCommand(languageManager))
            register(SummonCommand(languageManager))
            register(VieirinhaCommand(languageManager))
            register(RollCommand(languageManager))
            register(MinecraftCommand(languageManager))
            register(TextTransformCommand(languageManager))
            register(JankenponCommand(languageManager))
            register(HungerGamesCommand(languageManager))

            register(SoundboxCommand(languageManager))

            // ===[ IMAGES ]===
            register(DrakeCommand(languageManager))
            register(SonicCommand(languageManager))
            register(ArtCommand(languageManager))
            register(BobBurningPaperCommand(languageManager))
            register(BRMemesCommand(languageManager))
            register(BuckShirtCommand(languageManager))
            register(LoriSignCommand(languageManager))
            register(PassingPaperCommand(languageManager))
            register(PepeDreamCommand(languageManager))
            register(PetPetCommand(languageManager))
            register(WolverineFrameCommand(languageManager))
            register(RipTvCommand(languageManager))
            register(SustoCommand(languageManager))
            register(GetOverHereCommand(languageManager))
            register(NichijouYuukoPaperCommand(languageManager))
            register(TrumpCommand(languageManager))
            register(TerminatorAnimeCommand(languageManager))
            register(ToBeContinuedCommand(languageManager))
            register(InvertColorsCommand(languageManager))
            register(MemeMakerCommand(languageManager))
            register(MarkMetaCommand(languageManager))
            register(DrawnMaskCommand(languageManager))
            register(SadRealityCommand(languageManager))

            // ===[ VIDEOS ]===
            register(CarlyAaahCommand(languageManager))
            register(AttackOnHeartCommand(languageManager))
            register(FansExplainingCommand(languageManager))
            register(GigaChadCommand(languageManager))
            register(ChavesCommand(languageManager))

            // ===[ UTILS ]===
            register(HelpCommand(languageManager))
            register(MoneyCommand(languageManager))
            register(MorseCommand(languageManager))
            register(DictionaryCommand(languageManager))
            register(CalculatorCommand(languageManager))
            register(AnagramCommand(languageManager))
            register(ChooseCommand(languageManager))

            register(PackageCommand(languageManager))

            register(ColorInfoCommand(languageManager))
            register(NotificationsCommand(languageManager))

            // ===[ ECONOMY ]===
            register(SonhosCommand(languageManager))
            register(DailyCommand(languageManager))
            register(BrokerCommand(languageManager))
            register(PayCommand(languageManager))
            register(TransactionsCommand(languageManager))
            register(BetCommand(languageManager))

            // ===[ SOCIAL ]===
            register(AchievementsCommand(languageManager))

            register(AfkCommand(languageManager))
            register(GenderCommand(languageManager))

            // ===[ UNDERTALE ]===
            register(UndertaleCommand(languageManager))

            // ===[ ROLEPLAY ]===
            register(RoleplayCommand(languageManager))

            // ===[ ROBLOX ]===
            register(RobloxCommand(languageManager))
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