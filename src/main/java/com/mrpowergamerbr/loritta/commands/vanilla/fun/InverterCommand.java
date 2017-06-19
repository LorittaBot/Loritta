package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InverterCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "inverter";
	}

	public String getDescription() {
		return "Inverte a cor de uma imagem";
	}

	public List<String> getExample() {
		return Arrays.asList("http://i.imgur.com/KbHXmKO.png", "@Loritta", "\uD83D\uDC4C");
	}

	public Map<String, String> getDetailedUsage() {
		return ImmutableMap.<String, String>builder()
				.put("mensagem", "Usuário sortudo")
				.build();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (!LorittaUtils.canUploadFiles(context)) { return; }
		// ok
			try {
				BufferedImage image = LorittaUtils.getImageFromContext(context, 0);

				for (int x = 0; x < image.getWidth(); x++) {
					for (int y = 0; y < image.getHeight(); y++) {
						int rgba = image.getRGB(x, y);
						Color col = new Color(rgba, true);
						col = new Color(255 - col.getRed(),
								255 - col.getGreen(),
								255 - col.getBlue());
						image.setRGB(x, y, col.getRGB());
					}
				}

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, "png", os);
				InputStream is = new ByteArrayInputStream(os.toByteArray());

				MessageBuilder builder = new MessageBuilder();
				builder.append(context.getAsMention(true));
				context.sendFile(is, "invertido.png", builder.build());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
