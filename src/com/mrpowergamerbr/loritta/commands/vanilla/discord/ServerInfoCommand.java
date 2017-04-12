package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import java.util.stream.Collectors;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Role;

public class ServerInfoCommand extends CommandBase {
	public String getDescription() {
		return "Veja as informações do servidor do Discord atual!";
	}

	public CommandCategory getCategory() {
		return CommandCategory.DISCORD;
	}

	@Override
	public String getLabel() {
		return "serverinfo";
	}

	@Override
	public void run(CommandContext context) {
		EmbedBuilder embed = new EmbedBuilder();

		// Baseado no comando ?serverinfo do Dyno

		embed.setThumbnail(context.getGuild().getIconUrl()); // Ícone da Guild
		embed.setTitle(context.getGuild().getName(), null); // Nome da Guild
		embed.addField("ID", context.getGuild().getId(), true); // ID da Guild
		embed.addField("Nome", context.getGuild().getName(), true); // Nome da Guild (de novo)
		embed.addField("Dono", context.getGuild().getOwner().getUser().getName() + "#" + context.getGuild().getOwner().getUser().getDiscriminator(), true); // Dono da Guild
		embed.addField("Região", context.getGuild().getRegion().getName(), true); // Região da Guild
		embed.addField("Canais", String.valueOf(context.getGuild().getTextChannels().size()), true); // Canais de Texto da Guild
		embed.addField("Membros", String.valueOf(context.getGuild().getMembers().size()), true); // Membros da Guild
		embed.addField("Pessoas", String.valueOf(context.getGuild().getMembers().stream().filter((member) -> !member.getUser().isBot()).collect(Collectors.toList()).size()), true); // Humanos na Guild
		embed.addField("Bots", String.valueOf(context.getGuild().getMembers().stream().filter((member) -> member.getUser().isBot()).collect(Collectors.toList()).size()), true); // Bots na Guild
		embed.addField("Online", String.valueOf(context.getGuild().getMembers().stream().filter((member) -> member.getOnlineStatus() != OnlineStatus.OFFLINE).collect(Collectors.toList()).size()), true); // Pessoas online na Guild
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Role r : context.getGuild().getRoles()) {
			if (first) {
				sb.append(r.getName());
				first = false;
			} else {
				sb.append(", ");
				sb.append(r.getName());
			}
		}
		embed.addField("Cargos", sb.toString(), true); // Cargos da Guild
		
		embed.setFooter("Criado em " + context.getGuild().getCreationTime().toString(), null); // Quando a Guild foi criada
		
		context.sendMessage(embed.build()); // phew, agora finalmente poderemos enviar o embed!
	}
}
