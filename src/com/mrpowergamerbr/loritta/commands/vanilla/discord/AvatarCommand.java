package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;

import net.dv8tion.jda.core.entities.User;

public class AvatarCommand extends CommandBase {
	public static final String ONLY_ONE_PER_MESSAGE = "onlyOnePerMessage";
	public static final String HIDE_IMAGE = "hideImage";
	
	public String getDescription() {
		return "Pega o avatar de um usuário do Discord";
	}
	
	public CommandCategory getCategory() {
		return CommandCategory.DISCORD;
	}
	
	public String getUsage() {
		return "nome do usuário";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("@Loritta");
	}
	
	@Override
	public String getLabel() {
		return "avatar";
	}

	@Override
	public void run(CommandContext context) {
		if (!context.getMessage().getMentionedUsers().isEmpty()) {
			List<User> list = new ArrayList<User>(context.getMessage().getMentionedUsers());

			CommandOptions cmdOptions = context.getConfig().getCommandOptionsFor(this);
			
			if (cmdOptions.getAsBoolean(ONLY_ONE_PER_MESSAGE)) {
				list = Arrays.asList(context.getMessage().getMentionedUsers().get(0));
			}
			
			StringBuilder sb = new StringBuilder(context.getAsMention(true) + "\n");
			for (User usr : list) { 
				sb.append((cmdOptions.getAsBoolean(HIDE_IMAGE) ? "<" : "") + usr.getEffectiveAvatarUrl() + (cmdOptions.getAsBoolean(HIDE_IMAGE) ? ">" : "") + "\n");
			}
			context.sendMessage(sb.toString());
		} else {
			context.explain();
		}
	}
}
