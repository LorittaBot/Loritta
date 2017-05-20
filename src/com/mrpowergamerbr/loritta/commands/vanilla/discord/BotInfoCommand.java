package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
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
	public String getDescription() {
		return "Mostra informações interessantes (e algumas bem inúteis) sobre a Loritta.";
	}
	
	@Override
	public void run(CommandContext context) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Olá, eu sou a Loritta! 💁", null, "http://i.imgur.com/LUHLEs9.png");
		embed.setColor(new Color(186, 0, 239));
		embed.addField("📝 Nome", "Loritta#" + LorittaLauncher.getInstance().getJda().getSelfUser().getDiscriminator(), true);
		embed.addField("🌎 Servidores", String.valueOf(LorittaLauncher.getInstance().getJda().getGuilds().size()) + " servidores", true);
		embed.addField("👥 Usuários", String.valueOf(LorittaLauncher.getInstance().getJda().getUsers().size()) + " usuários", true);
		embed.addField("👾 Website", "https://loritta.website", true);
		embed.addField("📚 Bibiloteca", "JDA (Java)", true);
		embed.addField("💻 Quantidade de Comandos", LorittaLauncher.getInstance().getCommandManager().getCommandMap().size() + " comandos", true);
		embed.addField("🏋️‍ Comandos executados desde o último restart", String.valueOf(LorittaLauncher.getInstance().getExecutedCommands()), true);
		embed.addField("Menções Honrosas", "`DaPorkchop_#2459` Ter criado o PorkBot\n"
				+ "`official-papyrus-amiibo` Ter feito a incrível arte que a Loritta usa [Veja o tumblr!](http://official-papyrus-amiibo.tumblr.com/post/158758445671/youve-been-blessed-by-the-angel-katy)", false);
		embed.setFooter("Loritta foi criada por MrPowerGamerBR - https://mrpowergamerbr.com/", "https://mrpowergamerbr.com/assets/img/avatar.png");
		embed.setThumbnail("https://cdn.discordapp.com/avatars/297153970613387264/62f928b967905d38730e3810632eae77.png");
		context.sendMessage(embed.build());
	}

}
