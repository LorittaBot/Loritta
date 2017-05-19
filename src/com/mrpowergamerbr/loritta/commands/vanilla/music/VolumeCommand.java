package com.mrpowergamerbr.loritta.commands.vanilla.music;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.music.GuildMusicManager;

import net.dv8tion.jda.core.Permission;

public class VolumeCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "volume";
	}

	@Override
	public String getDescription() {
		return "Altera o volume da música";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("5");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		GuildMusicManager manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.getGuild());
		if (context.getHandle().hasPermission(Permission.MANAGE_SERVER)) {
			if (context.getArgs().length == 1) {	
				try {
					int vol = Integer.valueOf(context.getArgs()[0]);
					if (manager.player.getVolume() > vol) {
						context.sendMessage(context.getAsMention(true) + " irei diminuir o volume do batidão! Desculpe se eu te incomodei com a música alta...");
					} else {
						context.sendMessage(context.getAsMention(true) + " irei aumenta o volume do batidão! Se segura aí que agora você vai sentir as ondas sonoras!");
					}
					manager.player.setVolume(Integer.valueOf(context.getArgs()[0]));
				} catch (Exception e) {
					context.sendMessage(context.getAsMention(true) + " Ok, vamos alterar o volume para 💩 então... coloque um número válido por favor!");
				}
			} else {
				this.explain(context.getConfig(), context.getEvent());
			}
		}
	}
}
