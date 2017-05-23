package com.mrpowergamerbr.loritta.utils;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public class LorittaUtils {
	public static void warnOwnerNoPermission(Guild guild, TextChannel textChannel, ServerConfig serverConf) {
		for (Member member : guild.getMembers()) {
			if (member.isOwner()) {
				member.getUser().openPrivateChannel().complete().sendMessage("Hey, eu estou sem permissão no **" + textChannel.getName() + "** na guild **" + guild.getName() + "**! Você pode configurar o meu grupo para poder falar lá? Obrigada! 😊").complete();
			}
		}
	}
}
