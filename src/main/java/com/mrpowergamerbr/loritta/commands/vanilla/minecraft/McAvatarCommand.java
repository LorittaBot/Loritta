package com.mrpowergamerbr.loritta.commands.vanilla.minecraft;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

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

			BufferedImage bufferedImage = LorittaUtils.downloadImage("https://crafatar.com/avatars/" + nickname + "?size=128&overlay");
	        EmbedBuilder builder = new EmbedBuilder();

	        builder.setColor(Color.DARK_GRAY);
	        builder.setImage("attachment://avatar.png");

	        builder.addField("Avatar de " + nickname, "", false);
			
	        Message message = new MessageBuilder().append(context.getAsMention(true)).setEmbed(builder.build()).build();

			context.sendFile(bufferedImage, "avatar.png", message);
		} else {
			context.explain();
		}
	}

}