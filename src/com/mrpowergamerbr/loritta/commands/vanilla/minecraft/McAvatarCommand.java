package com.mrpowergamerbr.loritta.commands.vanilla.minecraft;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class McAvatarCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "mcavatar";
	}

	public String getDescription() {
		return "Mostra o avatar de uma conta do Minecraft, caso a conta não exista ele irá mostrar a skin padrão (Steve)";
	}
	
	public CommandCategory getCategory() {
		return CommandCategory.MINECRAFT;
	}
	
	public String getUsage() {
		return "nickname";
	}

	public List<String> getExample() {
		return Arrays.asList("Monerk");
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 1) {		
			String nickname = context.getArgs()[0];
			
	        EmbedBuilder builder = new EmbedBuilder();

	        builder.setColor(Color.DARK_GRAY);
	        builder.setImage("https://crafatar.com/avatars/" + nickname + "?size=128&overlay");

	        builder.addField("Avatar de " + nickname, "", false);
			
	        Message message = new MessageBuilder().append(context.getAsMention(true)).setEmbed(builder.build()).build();
	        
			context.sendMessage(message);
		} else {
			context.explain();
		}
	}

}