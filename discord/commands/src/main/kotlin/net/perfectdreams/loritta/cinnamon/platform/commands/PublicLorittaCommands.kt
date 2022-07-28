package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.SwitchToGlobalAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.SwitchToGuildProfileAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.ShowGuildMemberPermissionsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.bet.StartCoinFlipGlobalBetMatchmakingButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.ChangeTransactionFilterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.ChangeTransactionPageButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.PlayAudioClipButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roblox.declarations.RobloxCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations.RoleplayCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.*
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source.SourcePictureExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.*
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker.*
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker.ActivateInviteBlockerBypassButtonClickExecutor

/**
 * Public Loritta Commands
 *
 * They are in a separate class instead of staying within the [CommandManager] because this is also used in Showtime's Backend module to
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