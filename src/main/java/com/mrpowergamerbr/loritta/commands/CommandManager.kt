package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.AkinatorCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.AvaliarWaifuCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CaraCoroaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.FaustaoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.FraseToscaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.GameJoltCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.MagicBallCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.PedraPapelTesouraCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.QualidadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.RandomMemeguy1997
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.RandomSAMCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.RollCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.ShipCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.SpinnerCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.TwitchCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.VaporQualidadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.VaporondaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.YouTubeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.HackBanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.LimparCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.RoleIdCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.SlowModeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.SoftBanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.TempBanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.AvatarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.BotInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.EmojiCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.ServerInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.discord.UserInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.AmigosCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.AmizadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.AtaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.CepoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.DeusCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.DeusesCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.DiscordiaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.DrakeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.GangueCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.GetOverHereCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.InverterCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.JoojCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.LaranjoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.LavaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.LavaReversoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.ManiaTitleCardCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.NyanCatCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.OjjoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.PerdaoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.PerfeitoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.PrimeirasPalavrasCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.QuadroCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.RazoesCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.RipVidaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.RomeroBrittoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.SAMCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.StudiopolisTvCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.SustoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.TretaNewsCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.TristeRealidadeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.images.TrumpCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.EvalCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.LorittaBanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.LorittaUnbanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.NashornTestCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.ReloadCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.ServerInvitesCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McAvatarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McBodyCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McConquistaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McHeadCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McQueryCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McSignCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McSkinCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McStatusCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.McUUIDCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.OfflineUUIDCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.PeQueryCommand
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.SpigotMcCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AngelCommand
import com.mrpowergamerbr.loritta.commands.vanilla.misc.EscolherCommand
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
import com.mrpowergamerbr.loritta.commands.vanilla.music.VolumeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.pokemon.PokedexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbUserCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.BackgroundCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.DiscriminatorCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.EditarXPCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.InviteCommand
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
import com.mrpowergamerbr.loritta.commands.vanilla.utils.DicioCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.EncurtarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.GoogleCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.HexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.HojeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.IsUpCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.KnowYourMemeCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.LembrarCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.Md5Command
import com.mrpowergamerbr.loritta.commands.vanilla.utils.MoneyCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.MorseCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.OCRCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.PackageInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.RgbCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.TempoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.TranslateCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.WikiaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.WikipediaCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.YoutubeMp3Command
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import java.util.*

class CommandManager(isMusicOnly: Boolean) {
	var commandMap: MutableList<CommandBase> = ArrayList()
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
		commandMap.add(FraseToscaCommand())
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
		commandMap.add(RandomMemeguy1997())
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

		// =======[ MISC ]======
		commandMap.add(AjudaCommand())
		commandMap.add(PingCommand())
		commandMap.add(AngelCommand())
		commandMap.add(QuoteCommand())
		commandMap.add(SayCommand())
		commandMap.add(EscolherCommand())
		commandMap.add(LanguageCommand())
		commandMap.add(PatreonCommand())

		// =======[ SOCIAL ]======
		commandMap.add(PerfilCommand())
		commandMap.add(BackgroundCommand())
		commandMap.add(SobreMimCommand())
		commandMap.add(DiscriminatorCommand())
		commandMap.add(RepCommand())
		commandMap.add(RankCommand())
		commandMap.add(EditarXPCommand())

		// =======[ UTILS ]=======
		commandMap.add(TranslateCommand())
		commandMap.add(EncurtarCommand())
		commandMap.add(WikipediaCommand())
		commandMap.add(MoneyCommand())
		commandMap.add(HexCommand())
		commandMap.add(LembrarCommand())
		commandMap.add(RgbCommand())
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
		commandMap.add(GoogleCommand())

		// =======[ DISCORD ]=======
		commandMap.add(BotInfoCommand())
		commandMap.add(AvatarCommand())
		commandMap.add(EmojiCommand())
		commandMap.add(ServerInfoCommand())
		commandMap.add(InviteCommand())
		commandMap.add(UserInfoCommand())

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

		// =======[ ROBLOX ]========
		commandMap.add(RbUserCommand())

		// =======[ UNDERTALE ]========
		commandMap.add(UndertaleBoxCommand())
		commandMap.add(UndertaleBattleCommand())

		// =======[ POKÉMON ]========
		commandMap.add(PokedexCommand())

		// =======[ ADMIN ]========
		commandMap.add(LimparCommand())
		commandMap.add(RoleIdCommand())
		commandMap.add(HackBanCommand())
		commandMap.add(SoftBanCommand())
		commandMap.add(MuteCommand())
		commandMap.add(SlowModeCommand())
		commandMap.add(TempBanCommand())

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
		commandMap.add(VolumeCommand())
		commandMap.add(PlaylistCommand())
		commandMap.add(PularCommand())
		commandMap.add(PausarCommand())
		commandMap.add(ResumirCommand())
		commandMap.add(SeekCommand())
		commandMap.add(YouTubeCommand())

		commandMap.addAll(loritta.pluginManager.getExternalCommands())
		for (cmdBase in this.commandMap) {
			defaultCmdOptions.put(cmdBase.javaClass.getSimpleName(), CommandOptions::class.java)
		}

		// Custom Options
		defaultCmdOptions.put(TristeRealidadeCommand::class.java.simpleName, TristeRealidadeCommand.TristeRealidadeCommandOptions::class.java)
	}

	fun getCommandsDisabledIn(conf: ServerConfig): List<CommandBase> {
		return commandMap.filter { conf.disabledCommands.contains(it.javaClass.simpleName) }
	}
}
