package com.mrpowergamerbr.loritta.utils;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.UUID;

public class LorittaUtils {
    public static void warnOwnerNoPermission(Guild guild, TextChannel textChannel, ServerConfig serverConf) {
        if (serverConf.warnOnMissingPermission()) {
            for (Member member : guild.getMembers()) {
                if (member.isOwner()) {
                    member.getUser().openPrivateChannel().complete().sendMessage("Hey, eu estou sem permissão no **" + textChannel.getName() + "** na guild **" + guild.getName() + "**! Você pode configurar o meu grupo para poder falar lá? Obrigada! 😊").complete();
                }
            }
        }
    }

    public static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }
}
