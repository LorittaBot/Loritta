package net.perfectdreams.loritta.cinnamon.platform.twitter

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PetPetExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.QuadroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.AtaCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BolsonaroCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BriggsCoverCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.CanellaDvdCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.CepoDeMadeiraCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.DrakeCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.EdnaldoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.InvertColorsCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.KnuxThrowCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.QuadroCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.RomeroBrittoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.StudiopolisTvCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ToBeContinuedCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.cinnamon.common.LorittaBot
import net.perfectdreams.loritta.cinnamon.common.locale.LocaleManager
import net.perfectdreams.loritta.cinnamon.common.memory.services.MemoryServices
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.common.utils.config.GabrielaImageServerConfig
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.twitter.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.twitter.utils.config.TwitterConfig

class LorittaTwitter(
    config: LorittaConfig,
    twitterConfig: TwitterConfig,
    gabrielaImageServerConfig: GabrielaImageServerConfig
): LorittaBot(config) {
    companion object {
        private const val LORITTA_MENTION = "@LorittaEdit"
    }

    val client = PenicillinClient {
        account {
            application(twitterConfig.consumerKey, twitterConfig.consumerSecret)
            token(twitterConfig.accessToken, twitterConfig.accessTokenSecret)
        }
    }

    val commandManager = CommandManager(this)
    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    val http = HttpClient {
        expectSuccess = false
    }

    override val services = MemoryServices()
    val gabrielaImageServerClient = GabrielaImageServerClient(gabrielaImageServerConfig.url, http)

    fun start() {
        localeManager.loadLocales()

        // ===[ IMAGES ]===
        commandManager.register(AtaCommand, MonicaAtaExecutor(emotes, gabrielaImageServerClient), ChicoAtaExecutor(emotes, gabrielaImageServerClient), LoriAtaExecutor(emotes, gabrielaImageServerClient), GessyAtaExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(DrakeCommand, DrakeExecutor(emotes, gabrielaImageServerClient), BolsoDrakeExecutor(emotes, gabrielaImageServerClient), LoriDrakeExecutor(emotes, gabrielaImageServerClient))
        // commandManager.register(ManiaTitleCardCommand, ManiaTitleCardExecutor(http))
        commandManager.register(ArtCommand, ArtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BolsonaroCommand, BolsonaroExecutor(emotes, gabrielaImageServerClient), Bolsonaro2Executor(emotes, gabrielaImageServerClient), BolsoFrameExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BriggsCoverCommand, BriggsCoverExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BuckShirtCommand, BuckShirtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CanellaDvdCommand, CanellaDvdExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(EdnaldoCommand, EdnaldoBandeiraExecutor(emotes, gabrielaImageServerClient), EdnaldoTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(LoriSignCommand, LoriSignExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PetPetCommand, PetPetExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(QuadroCommand, QuadroExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(RipTvCommand, RipTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(RomeroBrittoCommand, RomeroBrittoExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(StudiopolisTvCommand, StudiopolisTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(SustoCommand, SustoExecutor(emotes, gabrielaImageServerClient))
        // commandManager.register(CortesFlowCommand, CortesFlowExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(KnuxThrowCommand, KnuxThrowExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CepoDeMadeiraCommand, CepoDeMadeiraExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(TrumpCommand, TrumpExecutor(emotes, gabrielaImageServerClient))
        // commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(http))
        // commandManager.register(SAMCommand, SAMExecutor(http))
        commandManager.register(ToBeContinuedCommand, ToBeContinuedExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(InvertColorsCommand, InvertColorsExecutor(emotes, gabrielaImageServerClient))
        // commandManager.register(MemeMakerCommand, MemeMakerExecutor(http))

        // ===[ VIDEOS ]===
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(emotes, gabrielaImageServerClient))
        // commandManager.register(FansExplainingCommand, FansExplainingExecutor(http))

        runBlocking {
            client.stream.filter(track = listOf(LORITTA_MENTION)).listen(
                object : FilterStreamListener {
                    override suspend fun onStatus(status: Status) {
                        try {
                            // Ignore retweets
                            if (status.retweeted)
                                return

                            // Ignore possibly sensitive tweets to avoid issues with Twitter
                            /* if (status.possiblySensitive)
                                return */

                            println("Text: ${status.text}")
                            println("Text Raw: ${status.textRaw}")
                            println("Full Text Raw: ${status.fullTextRaw}")

                            // So if the status is
                            // Hello Hello @LorittaEdit canelladvd
                            // The text will be "canelladvd"
                            // We will use "substringAfterLast" because Twitter gives this when replying to a conversation where
                            // Loritta is already included
                            // @LorittaEdit Hello Hello @LorittaEdit canelladvd
                            val textAfterMention = status.text.substringAfterLast(LORITTA_MENTION).trim()

                            commandManager.matches(status, textAfterMention)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
}