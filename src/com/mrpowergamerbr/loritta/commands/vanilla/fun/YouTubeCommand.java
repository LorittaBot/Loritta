package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.utils.YouTubeUtils;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.utils.YouTubeItem;

import lombok.*;
import lombok.experimental.Accessors;

public class YouTubeCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "youtube";
	}

	@Override
	public String getDescription() {
		return "Procura um vídeo no YouTube";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("shantae tassel town");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {	
			List<YouTubeItem> item = YouTubeUtils.searchVideosOnYouTube(String.join(" ", context.getArgs()));

			if (!item.isEmpty()) {
				boolean doNotEmbed = false;
				CommandOptions cmdOptions = context.getConfig().getCommandOptionsFor(this);
				if (cmdOptions instanceof YouTubeCommandOptions) {
					doNotEmbed = ((YouTubeCommandOptions) cmdOptions).doNotEmbed();
				}
				context.sendMessage(context.getAsMention(true) + (doNotEmbed ? "<" : "") + "https://youtu.be/" + item.get(0).getId().getVideoId() + (doNotEmbed ? ">" : ""));
				return;
			}
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
