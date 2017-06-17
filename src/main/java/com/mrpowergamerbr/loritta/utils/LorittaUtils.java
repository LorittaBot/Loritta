package com.mrpowergamerbr.loritta.utils;

import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class LorittaUtils {
    public static boolean canUploadFiles(CommandContext context) {
        if (!context.getGuild().getSelfMember().hasPermission(context.event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
        	context.sendMessage("❌ | Eu não tenho permissão para enviar arquivos aqui!");
        	return false;
		}
		return true;
    }

    public static boolean handleIfBanned(CommandContext context, LorittaProfile profile) {
        if (profile.isBanned()) {
            context.sendMessage("\uD83D\uDE45 | Você está **banido**\n\n**Motivo:** " + profile.getBanReason() + "\n\nEnvie uma mensagem privada para o MrPowerGamerBR#4185 caso queira ser desbanido.");
            return true;
        }
        return false;
    }

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

    public static byte[] fetchRemoteFile(String location) throws Exception {
        URL url = new URL(location);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = connection.getInputStream();
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
            //handle errors
        } finally {
            if (is != null) is.close();
        }
        return bytes;
    }
}
