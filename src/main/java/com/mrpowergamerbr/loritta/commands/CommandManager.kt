package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.*
import com.mrpowergamerbr.loritta.commands.vanilla.administration.*
import com.mrpowergamerbr.loritta.commands.vanilla.discord.*
import com.mrpowergamerbr.loritta.commands.vanilla.economy.*
import com.mrpowergamerbr.loritta.commands.vanilla.images.*
import com.mrpowergamerbr.loritta.commands.vanilla.magic.*
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.*
import com.mrpowergamerbr.loritta.commands.vanilla.misc.*
import com.mrpowergamerbr.loritta.commands.vanilla.music.*
import com.mrpowergamerbr.loritta.commands.vanilla.pokemon.PokedexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbGameCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbUserCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.*
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBattleCommand
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBoxCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.*
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import java.util.*

class CommandManager {
	var commandMap: MutableList<AbstractCommand> = ArrayList()
	var defaultCmdOptions: MutableMap<String, Class<*>> = HashMap()

	init {
		commandMap.add(RollCommand())
		commandMap.add(FaustaoCommand())
		commandMap.add(CaraCoroaCommand())
		commandMap.add(PedraPapelTesouraCommand())
		commandMap.add(VaporondaCommand())
		commandMap.add(QualidadeCommand())
		commandMap.add(VaporQualidadeCommand())
		commandMap.add(TristeRealidadeCommand())
		commandMap.add(TretaNewsCommand())
		commandMap.add(MagicBallCommand())
		commandMap.add(SAMCommand())
		commandMap.add(NyanCatCommand())
		commandMap.add(WikiaCommand())
		commandMap.add(PrimeirasPalavrasCommand())
		commandMap.add(DrakeCommand())
		commandMap.add(InverterCommand())
		commandMap.add(SpinnerCommand())
		commandMap.add(LavaCommand())
		commandMap.add(LavaReversoCommand())
		commandMap.add(ShipCommand())
		commandMap.add(AvaliarWaifuCommand())
		commandMap.add(RazoesCommand())
		commandMap.add(QuadroCommand())
		commandMap.add(DeusCommand())
		commandMap.add(PerfeitoCommand())
		commandMap.add(TrumpCommand())
		commandMap.add(CepoCommand())
		commandMap.add(DeusesCommand())
		commandMap.add(GangueCommand())
		commandMap.add(RandomSAMCommand())
		commandMap.add(AmigosCommand())
		commandMap.add(DiscordiaCommand())
		commandMap.add(AmizadeCommand())
		commandMap.add(PerdaoCommand())
		commandMap.add(RipVidaCommand())
		commandMap.add(AtaCommand())
		commandMap.add(JoojCommand())
		commandMap.add(OjjoCommand())
		commandMap.add(HojeCommand())
		commandMap.add(AkinatorCommand())
		commandMap.add(GameJoltCommand())
		commandMap.add(TwitchCommand())

		// =======[ IMAGENS ]======
		commandMap.add(GetOverHereCommand())
		commandMap.add(RomeroBrittoCommand())
		commandMap.add(StudiopolisTvCommand())
		commandMap.add(ManiaTitleCardCommand())
		commandMap.add(LaranjoCommand())
		commandMap.add(SustoCommand())
		commandMap.add(TriggeredCommand())
		commandMap.add(GumballCommand())
		commandMap.add(ContentAwareScaleCommand())
		commandMap.add(ArtCommand())
		commandMap.add(PepeDreamCommand())
		commandMap.add(SwingCommand())
		commandMap.add(DemonCommand())
		commandMap.add(KnuxThrowCommand())
		commandMap.add(LoriSignCommand())
		commandMap.add(TextCraftCommand())

		// =======[ DIVERSÃO ]======
		commandMap.add(SimsimiCommand())
		commandMap.add(CongaParrotCommand())
		commandMap.add(GabrielaCommand())
		commandMap.add(BemBoladaCommand())
		commandMap.add(RandomNaoEntreAkiCommand())
		commandMap.add(TodoGrupoTemCommand())
		commandMap.add(TioDoPaveCommand())
		commandMap.add(VemDeZapCommand())

		// =======[ MISC ]======
		commandMap.add(AjudaCommand())
		commandMap.add(PingCommand())
		commandMap.add(QuoteCommand())
		commandMap.add(SayCommand())
		commandMap.add(EscolherCommand())
		commandMap.add(LanguageCommand())
		commandMap.add(PatreonCommand())
		commandMap.add(FanArtsCommand())
		commandMap.add(DiscordBotListCommand())
		commandMap.add(ActivateKeyCommand())
		commandMap.add(VotarCommand())

		// =======[ SOCIAL ]======
		commandMap.add(PerfilCommand())
		commandMap.add(BackgroundCommand())
		commandMap.add(SobreMimCommand())
		commandMap.add(DiscriminatorCommand())
		commandMap.add(RepCommand())
		commandMap.add(RankCommand())
		commandMap.add(EditarXPCommand())
		commandMap.add(AfkCommand())

		// =======[ UTILS ]=======
		commandMap.add(TranslateCommand())
		commandMap.add(EncurtarCommand())
		commandMap.add(WikipediaCommand())
		commandMap.add(MoneyCommand())
		commandMap.add(ColorInfoCommand())
		commandMap.add(LembrarCommand())
		commandMap.add(DicioCommand())
		commandMap.add(TempoCommand())
		commandMap.add(AminoCommand())
		commandMap.add(PackageInfoCommand())
		commandMap.add(IsUpCommand())
		commandMap.add(KnowYourMemeCommand())
		commandMap.add(AnagramaCommand())
		commandMap.add(CalculadoraCommand())
		commandMap.add(MorseCommand())
		commandMap.add(OCRCommand())
		commandMap.add(EmojiSearchCommand())
		commandMap.add(ReceitasCommand())
		commandMap.add(EncodeCommand())
		commandMap.add(LyricsCommand())

		// =======[ DISCORD ]=======
		commandMap.add(BotInfoCommand())
		commandMap.add(AvatarCommand())
		commandMap.add(EmojiCommand())
		commandMap.add(ServerInfoCommand())
		commandMap.add(InviteCommand())
		commandMap.add(UserInfoCommand())
		commandMap.add(ChatLogCommand())
		commandMap.add(InviteInfoCommand())
		commandMap.add(ChannelInfoCommand())

		// =======[ MINECRAFT ]========
		commandMap.add(OfflineUUIDCommand())
		commandMap.add(McAvatarCommand())
		commandMap.add(McQueryCommand())
		commandMap.add(McUUIDCommand())
		commandMap.add(McStatusCommand())
		commandMap.add(McHeadCommand())
		commandMap.add(McBodyCommand())
		commandMap.add(McSignCommand())
		commandMap.add(SpigotMcCommand())
		commandMap.add(McConquistaCommand())
		commandMap.add(PeQueryCommand())
		commandMap.add(McSkinCommand())
		commandMap.add(McMoletomCommand())

		// =======[ ROBLOX ]========
		commandMap.add(RbUserCommand())
		commandMap.add(RbGameCommand())

		// =======[ UNDERTALE ]========
		commandMap.add(UndertaleBoxCommand())
		commandMap.add(UndertaleBattleCommand())

		// =======[ POKÉMON ]========
		commandMap.add(PokedexCommand())

		// =======[ ANIME ]========
		// commandMap.add(MALAnimeCommand())
		// commandMap.add(MALMangaCommand())

		// =======[ ADMIN ]========
		commandMap.add(LimparCommand())
		commandMap.add(RoleIdCommand())
		commandMap.add(SoftBanCommand())
		commandMap.add(MuteCommand())
		commandMap.add(UnmuteCommand())
		commandMap.add(SlowModeCommand())
		// commandMap.add(TempBanCommand())
		commandMap.add(TempRoleCommand())
		commandMap.add(KickCommand())
		commandMap.add(BanCommand())
		commandMap.add(WarnCommand())
		commandMap.add(UnwarnCommand())
		commandMap.add(WarnListCommand())

		// =======[ MAGIC ]========
		commandMap.add(ReloadCommand())
		commandMap.add(EvalCommand())
		commandMap.add(NashornTestCommand())
		commandMap.add(ServerInvitesCommand())
		commandMap.add(LorittaBanCommand())
		commandMap.add(LorittaUnbanCommand())
		commandMap.add(LoriServerListConfigCommand())
		commandMap.add(TicTacToeCommand())
		commandMap.add(EvalKotlinCommand())

		// =======[ MÚSICA ]========
		commandMap.add(TocarCommand())
		commandMap.add(MusicInfoCommand())
		commandMap.add(VolumeCommand())
		commandMap.add(PlaylistCommand())
		commandMap.add(PularCommand())
		commandMap.add(PausarCommand())
		commandMap.add(ResumirCommand())
		commandMap.add(SeekCommand())
		commandMap.add(YouTubeCommand())
		commandMap.add(RestartSongCommand())
		commandMap.add(TocarAgoraCommand())
		commandMap.add(ShuffleCommand())
		commandMap.add(PararCommand())

		// =======[ ECONOMIA ]========
		commandMap.add(LoraffleCommand())
		commandMap.add(DailyCommand())
		commandMap.add(PagarCommand())
		commandMap.add(SonhosCommand())
		commandMap.add(LigarCommand())
		commandMap.add(SonhosTopCommand())
		commandMap.add(ExchangeCommand())

		for (cmdBase in this.commandMap) {
			defaultCmdOptions.put(cmdBase.javaClass.simpleName, CommandOptions::class.java)
		}

		// Custom Options
		defaultCmdOptions.put(TristeRealidadeCommand::class.java.simpleName, TristeRealidadeCommand.TristeRealidadeCommandOptions::class.java)
	}

	fun getCommandsDisabledIn(conf: ServerConfig): List<AbstractCommand> {
		return commandMap.filter { conf.disabledCommands.contains(it.javaClass.simpleName) }
	}
}
