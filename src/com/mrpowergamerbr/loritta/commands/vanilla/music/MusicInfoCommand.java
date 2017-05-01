package com.mrpowergamerbr.loritta.commands.vanilla.music;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper;
import com.mrpowergamerbr.loritta.utils.music.GuildMusicManager;

public class MusicInfoCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "tocando";
	}

	@Override
	public String getDescription() {
		return "Fala a música que está tocando agora.";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("", "playlist", "todos");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		GuildMusicManager manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.getGuild());
		if (context.getArgs().length == 1) {	
			if (context.getArgs()[0].equalsIgnoreCase("playlist")) {
				List<AudioTrackWrapper> songs = manager.scheduler.getQueue().stream().collect(Collectors.toList()); // Para não remover tudo da nossa BlockingQueue
				String txt = "Na fila...\n";
				if (manager.player.getPlayingTrack() == null) {
					txt = "Não tem nenhuma música na fila...";
				} else {
					txt += "▶ " + manager.player.getPlayingTrack().getInfo().title + "\n";
				}
				for (AudioTrackWrapper song : songs) {
					txt += "⏸ " + song.getTrack().getInfo().title + "\n";
				}
				context.sendMessage(context.getAsMention(true) + txt);
			}
			if (context.getArgs()[0].equalsIgnoreCase("todos")) {
				String txt = "Em outras quebradas por aí...\n";
				for (GuildMusicManager mm : LorittaLauncher.getInstance().getMusicManagers().values()) {
					if (mm.player.getPlayingTrack() != null) {
						txt += "**" + mm.scheduler.getGuild().getName() + "** ▶ " + mm.player.getPlayingTrack().getInfo().title + "\n";
					}
				}
				context.sendMessage(context.getAsMention(true) + txt);
			}
		} else {
			if (manager.player.getPlayingTrack() == null) {
				context.sendMessage(context.getAsMention(true) + "Nenhuma música está tocando... Que tal tocar uma? `+tocar música`");
			} else {
				context.sendMessage(context.getAsMention(true) + "Atualmente estou tocando " + manager.player.getPlayingTrack().getInfo().title + " [" + ((manager.player.getPlayingTrack().getDuration() - manager.player.getPlayingTrack().getPosition()) / 1000) + "s]!");
			}
		}
	}
}
