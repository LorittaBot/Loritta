package com.mrpowergamerbr.loritta.utils;

import com.github.kevinsawicki.http.HttpRequest;
import com.mrpowergamerbr.loritta.LorittaLauncher;
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
import java.awt.*;
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
		if (!context.isPrivateChannel() && !context.getGuild().getSelfMember().hasPermission(context.event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
			context.sendMessage(ERROR + " | " + context.getAsMention(true) + "Eu não tenho permissão para enviar arquivos neste canal! \uD83D\uDE22");
			return false;
		}
		return true;
	}

	public static boolean handleIfBanned(CommandContext context, LorittaProfile profile) {
		if (profile.isBanned()) {
			context.sendMessage("\uD83D\uDE45 | " + context.getAsMention(true) + "Você está **banido**\n\n**Motivo:** " + profile.getBanReason() + "\n\nSe você acha que o ban foi injusto (duvido que foi injusto) ou se você se arrependeu, envie uma mensagem privada para `MrPowerGamerBR#4185` pedindo para ser desbanido!\n\nDesculpe pela inconveniência... \uD83D\uDE1E");
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
	public static boolean isValidImage(CommandContext context, Image image) {
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
		return getImageFromContext(context, argument, search, 2048);
	}

	/**
	 * Retorna uma imagem dependendo do contexto
	 *
	 * @param context
	 * @param argument
	 * @param search
	 * @param avatarSize
	 * @return uma BufferedImage com a imagem
	 */
	public static BufferedImage getImageFromContext(CommandContext context, int argument, int search, int avatarSize) {
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
						toBeDownloaded = user.getEffectiveAvatarUrl() + "?size=" + avatarSize;
						break;
					}
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (!context.isPrivateChannel() && toBeDownloaded == null) {
				List<Member> matchedMembers = context.getGuild().getMembersByEffectiveName(link, true);

				if (!matchedMembers.isEmpty()) {
					toBeDownloaded = matchedMembers.get(0).getUser().getEffectiveAvatarUrl() + "?size=" + avatarSize;
				}
			}

			// Ainda não?!? Vamos verificar se é um emoji.
			if (toBeDownloaded == null) {
				// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
				for (Emote emote : context.getMessage().getEmotes()) {
					if (link.equalsIgnoreCase(emote.getAsMention())) {
						toBeDownloaded = emote.getImageUrl();
						break;
					}
				}
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			if (toBeDownloaded == null) {
				try {
					String val = toUnicode(context.getRawArgs()[argument].codePointAt(0)); // Vamos usar codepoints porque emojis
					val = val.substring(2); // Remover coisas desnecessárias
					toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/" + val + ".png";
					if (HttpRequest.get(toBeDownloaded).code() != 200) {
						toBeDownloaded = null;
					}
				} catch (Exception e) {}
			}

			// Ok, então só pode ser um ID do Discord!
			if (toBeDownloaded == null) {
				try {
					User user = LorittaLauncher.getInstance().getLorittaShards().getUserById(link);

					if (user != null) { // Pelo visto é!
						toBeDownloaded = user.getEffectiveAvatarUrl() + "?size=" + avatarSize;
					}
				} catch (Exception e) {}
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (search > 0 && toBeDownloaded == null) {
			List<Message> message = context.getMessage().getChannel().getHistory().retrievePast(search).complete();

			attach:
			for (Message msg : message) {
				for (MessageEmbed embed : msg.getEmbeds()) {
					if (embed.getImage() != null) {
						toBeDownloaded = embed.getImage().getUrl();
						break attach;
					}
				}
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
	 * Retorna uma URL dependendo do contexto
	 *
	 * @param context
	 * @param argument
	 * @param search
	 * @param avatarSize
	 * @return uma URL com a imagem
	 */
	public static String getURLFromContext(CommandContext context, int argument, int search, int avatarSize) {
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
						toBeDownloaded = user.getEffectiveAvatarUrl() + "?size=" + avatarSize;
						break;
					}
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (!context.isPrivateChannel() && toBeDownloaded == null) {
				List<Member> matchedMembers = context.getGuild().getMembersByEffectiveName(link, true);

				if (!matchedMembers.isEmpty()) {
					toBeDownloaded = matchedMembers.get(0).getUser().getEffectiveAvatarUrl() + "?size=" + avatarSize;
				}
			}

			// Ainda não?!? Vamos verificar se é um emoji.
			if (toBeDownloaded == null) {
				// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
				for (Emote emote : context.getMessage().getEmotes()) {
					if (link.equalsIgnoreCase(emote.getAsMention())) {
						toBeDownloaded = emote.getImageUrl();
						break;
					}
				}
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			if (toBeDownloaded == null) {
				try {
					String val = toUnicode(context.getRawArgs()[argument].codePointAt(0)); // Vamos usar codepoints porque emojis
					val = val.substring(2); // Remover coisas desnecessárias
					toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/" + val + ".png";
					if (HttpRequest.get(toBeDownloaded).code() != 200) {
						toBeDownloaded = null;
					}
				} catch (Exception e) {}
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (search > 0 && toBeDownloaded == null) {
			List<Message> message = context.getMessage().getChannel().getHistory().retrievePast(search).complete();

			attach:
			for (Message msg : message) {
				for (MessageEmbed embed : msg.getEmbeds()) {
					if (embed.getImage() != null) {
						toBeDownloaded = embed.getImage().getUrl();
						break attach;
					}
				}
				for (Attachment attachment : msg.getAttachments()) {
					if (attachment.isImage()) {
						toBeDownloaded = attachment.getUrl();
						break attach;
					}
				}
			}
		}

		return toBeDownloaded;
	}

	/**
	 * Faz download de uma imagem e retorna ela como um BufferedImage
	 * @param url
	 * @return
	 */
	public static BufferedImage downloadImage(String url) {
		return downloadImage(url, -1);
	}

	/**
	 * Faz download de uma imagem e retorna ela como um BufferedImage
	 * @param url
	 * @param timeout
	 * @return
	 */
	public static BufferedImage downloadImage(String url, int timeout) {
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
			if (timeout != -1) {
				connection.setReadTimeout(timeout);
				connection.setConnectTimeout(timeout);
			}
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

	public static double evalMath(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) x = Math.sqrt(x);
					else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
					else throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
				if (eat('%')) x = x % parseFactor(); // mod

				return x;
			}
		}.parse();
	}
}
