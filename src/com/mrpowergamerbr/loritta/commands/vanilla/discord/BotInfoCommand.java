package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import java.awt.Color;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.EmbedBuilder;

public class BotInfoCommand extends CommandBase {

	@Override
	public String getLabel() {
		return "botinfo";
	}

	@Override
	public void run(CommandContext context) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Loritta", null, "http://i.imgur.com/LUHLEs9.png");
		embed.setColor(new Color(186, 0, 239));
		embed.addField("Nome", "Loritta#" + LorittaLauncher.getInstance().getJda().getSelfUser().getDiscriminator(), true);
		embed.addField("Servidores", String.valueOf(LorittaLauncher.getInstance().getJda().getGuilds().size()), true);
		embed.addField("Usuários", String.valueOf(LorittaLauncher.getInstance().getJda().getUsers().size()), true);
		embed.addField("Website", "https://loritta.website", true);
		embed.addField("Bibiloteca", "JDA", true);
		embed.addField("Menções Honrosas", "`DaPorkchop_#2459` Ter criado o PorkBot\n"
				+ "`official-papyrus-amiibo` Ter feito a incrível arte que a Loritta usa [Veja o tumblr!](http://official-papyrus-amiibo.tumblr.com/post/158758445671/youve-been-blessed-by-the-angel-katy)", false);
		embed.setFooter("Loritta foi criado por MrPowerGamerBR - http://mrpowergamerbr.com/", "http://i.imgur.com/nhBZ8i4.png");
		context.sendMessage(embed.build());
	}

}
