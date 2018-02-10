package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.AkinatorCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.AvaliarWaifuCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CaraCoroaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CongaParrotCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.FaustaoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.GameJoltCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.MagicBallCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.PedraPapelTesouraCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.QualidadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.RandomSAMCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.RollCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.ShipCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.SpinnerCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.TwitchCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.VaporQualidadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.VaporondaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.YouTubeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.*
import com.mrpowergamerbr.loritta.commands.vanilla.anime.MALAnimeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.anime.MALMangaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.AvatarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.BotInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.ChatLogCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.DiscriminatorCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.EmojiCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.InviteCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.ServerInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.UserInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.*
import com.mrpowergamerbr.loritta.commands.vanilla.magic.EvalCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.LorittaBanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.LorittaUnbanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.NashornTestCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.ReloadCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.ServerInvitesCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.*
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.EscolherCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.FanArtsCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.LanguageCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.PatreonCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.PingCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.QuoteCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.MusicInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.PausarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.PlaylistCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.PularCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.ResumirCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.SeekCommand
import com.mrpowergamerbr.loritta.commands.vanilla.music.TocarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.pokemon.PokedexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbGameCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbUserCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.AfkCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.BackgroundCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.EditarXPCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.RankCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.RepCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.SayCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.SobreMimCommand
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBattleCommand
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBoxCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.AminoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.AnagramaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.CalculadoraCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.ColorInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.DicioCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.EmojiSearchCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.EncurtarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.HojeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.IsUpCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.KnowYourMemeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.LembrarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.Md5Command
import com.mrpowergamerbr.loritta.commands.vanilla.utils.MoneyCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.MorseCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.OCRCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.PackageInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.TempoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.TranslateCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.WikiaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.WikipediaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.YoutubeMp3Command
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.pocketdreams.loriplugins.cleverbot.commands.CleverbotCommand
import net.pocketdreams.loriplugins.simsimi.commands.SimsimiCommand
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
		commandMap.add(SwingCommand())
		commandMap.add(DemonCommand())

		// =======[ DIVERSÃO ]======
		commandMap.add(SimsimiCommand())
		commandMap.add(CleverbotCommand())
		// commandMap.add(TamagotchiCommand())
		// commandMap.add(GiveawayCommand())
		commandMap.add(CongaParrotCommand())

		// =======[ IMAGENS ]======
		commandMap.add(GetOverHereCommand())
		commandMap.add(RomeroBrittoCommand())
		commandMap.add(StudiopolisTvCommand())
		commandMap.add(ManiaTitleCardCommand())
		commandMap.add(LaranjoCommand())
		commandMap.add(SustoCommand())
		commandMap.add(TriggeredCommand())
		commandMap.add(GumballCommand())

		// =======[ MISC ]======
		commandMap.add(AjudaCommand())
		commandMap.add(PingCommand())
		commandMap.add(QuoteCommand())
		commandMap.add(SayCommand())
		commandMap.add(EscolherCommand())
		commandMap.add(LanguageCommand())
		commandMap.add(PatreonCommand())
		commandMap.add(FanArtsCommand())

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
		commandMap.add(YoutubeMp3Command())
		commandMap.add(DicioCommand())
		commandMap.add(TempoCommand())
		commandMap.add(AminoCommand())
		commandMap.add(PackageInfoCommand())
		commandMap.add(IsUpCommand())
		commandMap.add(KnowYourMemeCommand())
		commandMap.add(Md5Command())
		commandMap.add(AnagramaCommand())
		commandMap.add(CalculadoraCommand())
		commandMap.add(MorseCommand())
		commandMap.add(OCRCommand())
		// commandMap.add(GoogleCommand())
		commandMap.add(EmojiSearchCommand())
		// commandMap.add(UnicodeCommand())

		// =======[ DISCORD ]=======
		commandMap.add(BotInfoCommand())
		commandMap.add(AvatarCommand())
		commandMap.add(EmojiCommand())
		commandMap.add(ServerInfoCommand())
		commandMap.add(InviteCommand())
		commandMap.add(UserInfoCommand())
		commandMap.add(ChatLogCommand())

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
		commandMap.add(MALAnimeCommand())
		commandMap.add(MALMangaCommand())

		// =======[ ADMIN ]========
		commandMap.add(LimparCommand())
		commandMap.add(RoleIdCommand())
		commandMap.add(SoftBanCommand())
		// commandMap.add(MuteCommand())
		commandMap.add(SlowModeCommand())
		// commandMap.add(TempBanCommand())
		commandMap.add(TempRoleCommand())
		commandMap.add(KickCommand())
		commandMap.add(BanCommand())

		// =======[ MAGIC ]========
		commandMap.add(ReloadCommand())
		commandMap.add(EvalCommand())
		commandMap.add(NashornTestCommand())
		commandMap.add(ServerInvitesCommand())
		commandMap.add(LorittaBanCommand())
		commandMap.add(LorittaUnbanCommand())

		// =======[ MÚSICA ]========
		commandMap.add(TocarCommand())
		commandMap.add(MusicInfoCommand())
		// commandMap.add(VolumeCommand())
		commandMap.add(PlaylistCommand())
		commandMap.add(PularCommand())
		commandMap.add(PausarCommand())
		commandMap.add(ResumirCommand())
		commandMap.add(SeekCommand())
		commandMap.add(YouTubeCommand())

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
