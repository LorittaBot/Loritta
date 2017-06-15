package com.mrpowergamerbr.loritta.commands.vanilla.music;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import java.util.Arrays;
import java.util.List;

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
        /* GuildMusicManager manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.getGuild());
        if (context.getArgs().length == 1) {
			if (context.getArgs()[0].equalsIgnoreCase("playlist")) {
				List<AudioTrackWrapper> songs = manager.scheduler.getQueue().stream().collect(Collectors.toList()); // Para não remover tudo da nossa BlockingQueue
				String txt = "Na fila...\n";
				if (manager.player.getPlayingTrack() == null) {
					txt = "Não tem nenhuma música na fila...";
				} else {
					txt += "▶ " + manager.player.getPlayingTrack().getInfo().title + " (" + manager.scheduler.getCurrentTrack().getUser().getName() + ")\n";
				}
				for (AudioTrackWrapper song : songs) {
					txt += "⏸ " + song.getTrack().getInfo().title + " (" + song.getUser().getName() + ")\n";
				}
				context.sendMessage(context.getAsMention(true) + txt);
			}
			if (context.getArgs()[0].equalsIgnoreCase("todos")) {
				String txt = "Em outras quebradas por aí...\n";
				for (GuildMusicManager mm : LorittaLauncher.getInstance().getMusicManagers().values()) {
					if (mm.player.getPlayingTrack() != null) {
						txt += "**" + mm.scheduler.getGuild().getName() + "** ▶ " + mm.player.getPlayingTrack().getInfo().title + " (pedido por " + mm.scheduler.getCurrentTrack().getUser().getName() + ")\n";
					}
				}
				context.sendMessage(context.getAsMention(true) + txt);
			}
		} else {
			if (manager.player.getPlayingTrack() == null) {
				context.sendMessage(context.getAsMention(true) + "Nenhuma música está tocando... Que tal tocar uma? `+tocar música`");
			} else {
				Message message = context.sendMessage(context.getAsMention(true) + "Atualmente estou tocando " + manager.player.getPlayingTrack().getInfo().title + " [" + ((manager.player.getPlayingTrack().getDuration() - manager.player.getPlayingTrack().getPosition()) / 1000) + "s]! (pedido por " + manager.scheduler.getCurrentTrack().getUser().getName() + ")" + (context.getConfig().musicConfig().isVoteToSkip() ? "\n\uD83D\uDCAB **Quer pular a música? Então use \uD83E\uDD26 nesta mensagem!** (Se 75% das pessoas no canal de música reagirem com \uD83E\uDD26, eu irei pular a música!)" : ""));
                LorittaLauncher.getInstance().getMusicMessagesCache().put(message.getId(), manager.scheduler.getCurrentTrack());
				message.addReaction("\uD83E\uDD26").complete();
			}
		} */
    }
}
