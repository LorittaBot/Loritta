package com.mrpowergamerbr.loritta.utils;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static com.mrpowergamerbr.loritta.utils.TextUtilsKt.f;

public final class LorittaUtils {
	private LorittaUtils() {
	}

	public static boolean canUploadFiles(CommandContext context) {
		if (!context.isPrivateChannel() && !context.getGuild().getSelfMember().hasPermission(context.getEvent().getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + f(context.getLocale().get("IMAGE_UPLOAD_NO_PERM")) + " \uD83D\uDE22");
			return false;
		}
		return true;
	}

	public static void warnOwnerNoPermission(Guild guild, TextChannel textChannel, ServerConfig serverConf) {
		if (serverConf.getWarnOnMissingPermission()) {
			for (Member member : guild.getMembers()) {
				if (!member.getUser().isBot() && (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_PERMISSIONS))) {
					try {
						BaseLocale locale = LorittaLauncher.loritta.getLocaleById(serverConf.getLocaleId());
						member.getUser().openPrivateChannel().complete().sendMessage(locale.get("LORITTA_HeyIDontHavePermission", textChannel.getAsMention(), guild.getName())).complete();
					} catch (ErrorResponseException e){
						if (e.getErrorResponse().getCode() == 50007) { // Usuário tem as DMs desativadas
							continue;
						}
					}
				}
			}
		}
	}

	/**
	 * Faz download de uma imagem e retorna ela como um BufferedImage
	 * @param url
	 * @return
	 */
	public static BufferedImage downloadImage(String url) {
		return downloadImage(url, 15);
	}

	/**
	 * Faz download de uma imagem e retorna ela como um BufferedImage
	 * @param url
	 * @param timeout
	 * @return
	 */
	public static BufferedImage downloadImage(String url, int timeout) {
		return downloadImage(url, timeout, 10000000);
	}

	public static BufferedImage downloadImage(String url, int timeout, int maxSize) {
		return downloadImage(url, timeout, maxSize, 512);
	}

	public static BufferedImage downloadImage(String url, int timeout, int maxSize, int maxWidthHeight) {
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
			connection.setRequestProperty("User-Agent",
					Constants.USER_AGENT);

			if (connection.getHeaderFieldInt("Content-Length", 0) > maxSize) {
				return null;
			}

			if (timeout != -1) {
				connection.setReadTimeout(timeout);
				connection.setConnectTimeout(timeout);
			}

			BufferedImage bi = ImageIO.read(connection.getInputStream());

			if (maxWidthHeight != -1) {
				if (bi.getWidth() > maxWidthHeight || bi.getHeight() > maxWidthHeight) {
					// Espero que isto não vá gastar tanto processamento...
					LorittaImage img = new LorittaImage(bi);
					img.resize(maxWidthHeight, maxWidthHeight, true);
					return img.getBufferedImage();
				}
			}

			return bi;
		} catch (Exception e) {}
		return null;
	}

	public static InputStream downloadFile(String url, int timeout) {
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
			connection.setRequestProperty("User-Agent",
					Constants.USER_AGENT);

			if (timeout != -1) {
				connection.setReadTimeout(timeout);
				connection.setConnectTimeout(timeout);
			}

			return connection.getInputStream();
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
			new URL(link);
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
			//matches errors
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
				if (eat('%')) x %= parseFactor(); // mod

				return x;
			}
		}.parse();
	}
}