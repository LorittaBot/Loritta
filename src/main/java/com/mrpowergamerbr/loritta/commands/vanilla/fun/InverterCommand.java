package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.core.MessageBuilder;

public class InverterCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "inverter";
	}

	public String getDescription() {
		return "Inverte a cor de um avatar";
	}

	public List<String> getExample() {
		return Arrays.asList("@Loritta");
	}

	public Map<String, String> getDetailedUsage() {
		return ImmutableMap.<String, String>builder()
				.put("usuário1", "Usuário sortudo")
				.build();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		// ok
		if (context.getMessage().getMentionedUsers().size() > 0) {
		try {
			URL imageUrl = new URL(context.getEvent().getMessage().getMentionedUsers().get(0).getEffectiveAvatarUrl() + "?size=256");
			HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
			connection.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
			BufferedImage avatar = ImageIO.read(connection.getInputStream());

	        for (int x = 0; x < avatar.getWidth(); x++) {
	            for (int y = 0; y < avatar.getHeight(); y++) {
	                int rgba = avatar.getRGB(x, y);
	                Color col = new Color(rgba, true);
	                col = new Color(255 - col.getRed(),
	                                255 - col.getGreen(),
	                                255 - col.getBlue());
	                avatar.setRGB(x, y, col.getRGB());
	            }
	        }

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(avatar, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			MessageBuilder builder = new MessageBuilder();
			builder.append(context.getAsMention(true));
			context.sendFile(is, "invertido.png", builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
		} else {
			this.explain(context.getConfig(), context.getEvent());
		}
	}

	@Getter
	@Setter
	@Accessors(fluent = true)
	public static class TristeRealidadeCommandOptions extends CommandOptions {
		public boolean mentionEveryone = false; // Caso esteja ativado, todos que aparecerem serão mencionados
		public boolean hideDiscordTags = false; // Caso esteja ativado, todas as discord tags não irão aparecer na imagem
	}
}
