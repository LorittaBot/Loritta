package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Emote;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class EmojiCommand extends CommandBase {
	public String getDescription() {
		return "Veja emojis em um tamanho que você não precise usar uma lupa para tentar entender eles!";
	}

	public CommandCategory getCategory() {
		return CommandCategory.DISCORD;
	}

	public String getUsage() {
		return "emoji";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("😏");
	}

	@Override
	public String getLabel() {
		return "emoji";
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 1) {
			String emoji = context.getArgs()[0];

			if (emoji.startsWith(":") && emoji.endsWith(":")) { // Emoji customizado?
				// Sim!
				emoji = emoji.substring(1, emoji.length() - 1);
				List<Emote> customEmotes = context.getMessage().getEmotes();
				if (!customEmotes.isEmpty()) {
					Emote emote = customEmotes.get(0);
					String emojiUrl = emote.getImageUrl();

					try {
						URL imageUrl = new URL(emojiUrl);
						HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
						connection.setRequestProperty(
								"User-Agent",
								"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
						BufferedImage emoteImage = ImageIO.read(connection.getInputStream());

						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write(emoteImage, "png", os);
						InputStream is = new ByteArrayInputStream(os.toByteArray());

						context.sendFile(is, "emoji.png", new MessageBuilder().append(" ").build());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				// Na verdade é um emoji padrão...
				String val = LorittaUtils.toUnicode(emoji.codePointAt(0)); // Vamos usar codepoints porque emojis
				val = val.substring(2); // Remover coisas desnecessárias
				try {
					URL imageUrl = new URL("https://twemoji.maxcdn.com/2/72x72/" + val + ".png");
					HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
					connection.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
					BufferedImage emoteImage = ImageIO.read(connection.getInputStream());

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(emoteImage, "png", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());

					context.sendFile(is, "emoji.png", new MessageBuilder().append(" ").build());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			context.explain();
		}
	}
}
