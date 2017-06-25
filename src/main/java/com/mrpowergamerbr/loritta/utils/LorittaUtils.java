package com.mrpowergamerbr.loritta.utils;

import com.github.kevinsawicki.http.HttpRequest;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class LorittaUtils {
	public static final String ERROR = "<:erro:326509900115083266>";

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

	public static String replaceTokens(String message, GuildMemberJoinEvent e) {
		message = message.replace("{@user}", e.getMember().getAsMention());
		message = message.replace("{user}", e.getMember().getUser().getName());
		message = message.replace("{nickname}", e.getMember().getEffectiveName());
		message = message.replace("{guild}", e.getGuild().getName());
		return message;
	}

	public static String replaceTokens(String message, GuildMemberLeaveEvent e) {
		message = message.replace("{@user}", e.getMember().getAsMention());
		message = message.replace("{user}", e.getMember().getUser().getName());
		message = message.replace("{nickname}", e.getMember().getEffectiveName());
		message = message.replace("{guild}", e.getGuild().getName());
		return message;
	}

	/**
	 * Verifica se uma imagem é válida (ou seja, diferente de null), caso seja null, a Loritta irá avisar ao usuário que ela não tem nenhuma imagem "utilizável"
	 * @param context
	 * @param image
	 * @return
	 */
	public static boolean isValidImage(CommandContext context, BufferedImage image) {
		if (image == null) {
			context.sendMessage(ERROR + " | " + context.getAsMention(true) + " Eu não encontrei nenhuma imagem válida para eu usar! (Eu tento pegar imagens em links, upload de imagens, avatares de usuários mencionados, emojis... mas eu encontrei nada nessa sua mensagem!)");
			return false;
		}
		return true;
	}

	/**
	 * Retorna uma imagem dependendo do contexto
	 *
	 * @param context
	 * @param argument
	 * @return uma BufferedImage com a imagem
	 */
	public static BufferedImage getImageFromContext(CommandContext context, int argument) {
		return getImageFromContext(context, argument, 25);
	}

	/**
	 * Retorna uma imagem dependendo do contexto
	 *
	 * @param context
	 * @param argument
	 * @param search
	 * @return uma BufferedImage com a imagem
	 */
	public static BufferedImage getImageFromContext(CommandContext context, int argument, int search) {
		String toBeDownloaded = null; // Imagem para ser baixada
		BufferedImage image = null;
		if (context.getRawArgs().length > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			String link = context.getRawArgs()[argument]; // Ok, será que isto é uma URL?

			if (isValidUrl(link)) {
				toBeDownloaded = link; // Vamos salvar para depois então ;)
			}

			// Vamos verificar por menções
			if (toBeDownloaded == null) {
				// Uma menção do Discord é + ou - assim: <@123170274651668480>
				for (User user : context.getMessage().getMentionedUsers()) {
					if (user.getAsMention().equals(link.replace("!", ""))) { // O replace é necessário já que usuários com nick tem ! no mention (?)
						// Diferente de null? Então vamos usar o avatar do usuário!
						toBeDownloaded = user.getEffectiveAvatarUrl();
					}
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (toBeDownloaded == null) {
				List<Member> matchedMembers = context.getGuild().getMembersByEffectiveName(link, true);

				if (!matchedMembers.isEmpty()) {
					toBeDownloaded = matchedMembers.get(0).getUser().getEffectiveAvatarUrl();
				}
			}

			// Ainda não?!? Vamos verificar se é um emoji.
			if (toBeDownloaded == null) {
				// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
				for (Emote emote : context.getMessage().getEmotes()) {
					if (link.equals(emote.getAsMention())) {
						toBeDownloaded = emote.getImageUrl();
					}
				}
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			if (toBeDownloaded == null) {
				try {
					String val = toUnicode(context.getArgs()[argument].codePointAt(0)); // Vamos usar codepoints porque emojis
					val = val.substring(2); // Remover coisas desnecessárias
					toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/" + val + ".png";
					if (HttpRequest.get(toBeDownloaded).code() == 404) {
						toBeDownloaded = null;
					}
				} catch (Exception e) {}
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor então para encontrar attachments...
		if (search > 0 && toBeDownloaded == null) {
			List<Message> message = context.getMessage().getTextChannel().getHistory().retrievePast(search).complete();

			attach:
			for (Message msg : message) {
				for (Attachment attachment : msg.getAttachments()) {
					if (attachment.isImage()) {
						toBeDownloaded = attachment.getUrl();
						break attach;
					}
				}
			}
		}

		if (toBeDownloaded != null) {
			// Vamos baixar a imagem!
			try {
				image = downloadImage(toBeDownloaded);
			} catch (Exception e) {}
		}
		return image;
	}

	/**
	 * Faz download de uma imagem e retorna ela como um BufferedImage
	 * @param url
	 * @return
	 */
	public static BufferedImage downloadImage(String url) {
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
			return ImageIO.read(connection.getInputStream());
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Verifica se um link é uma URL válida
	 *
	 * @param link
	 * @return se a URL é válida ou não
	 */
	public static boolean isValidUrl(String link) {
		try {
			URL url = new URL(link);
			return true;
		} catch (MalformedURLException e) {
			return false;
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

	public static String toUnicode(int ch) {
		return String.format("\\u%04x", (int) ch);
	}
}
