package com.mrpowergamerbr.loritta.commands.vanilla.music;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.response.SearchResponse;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.utils.YouTubeItem;

import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.core.Permission;

public class TocarCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "tocar";
	}

	@Override
	public String getDescription() {
		return "Toca uma música, experimental.";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("https://youtu.be/wn4Ju5-vMQ4");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {	
			String music = String.join(" ", context.getArgs());
			
			if (music.equalsIgnoreCase("pular") && context.getHandle().hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().skipTrack(context.getEvent().getTextChannel());
				return;
			}

			LorittaLauncher.getInstance().loadAndPlay(context, context.getConfig(), context.getEvent().getTextChannel(), music);
		} else {
			context.explain();
		}
	}
	
	@Getter
	@Setter
	@Accessors(fluent = true)
	public static class YouTubeCommandOptions extends CommandOptions {
		public boolean doNotEmbed; // Caso esteja ativado, os vídeos não ficarão em "embeds"
	}
}
