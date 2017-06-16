package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.mrpowergamerbr.loritta.Loritta;
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
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SAMCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "sam";
	}

	@Override
	public String getDescription() {
		return "Adiciona uma marca da água do South America Memes em uma imagem";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("https://cdn.discordapp.com/attachments/265632341530116097/297440837871206420/meme.png");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {
			if (!LorittaUtils.canUploadFiles(context)) { return; }
			String link = context.getArgs()[0];

			double div = 1.5;
			if (context.getArgs().length >= 2) {
				try {
					div = Double.parseDouble(context.getArgs()[1]);
				} catch (Exception e) {
					
				}
			}
			try {
				URL imageUrl = new URL(link);
				HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
				BufferedImage image = ImageIO.read(connection.getInputStream());
				
				Image seloSouthAmericaMemes = null;
				seloSouthAmericaMemes =  ImageIO.read(new File(Loritta.FOLDER + "selo_sam.png"));
				
				int height = (int) (image.getHeight() / div); // Baseando na altura
				seloSouthAmericaMemes = seloSouthAmericaMemes.getScaledInstance(height, height, Image.SCALE_SMOOTH);

				int x = Loritta.getRandom().nextInt(0, Math.max(1, image.getWidth() - seloSouthAmericaMemes.getWidth(null)));
				int y = Loritta.getRandom().nextInt(0, Math.max(1, image.getHeight() - seloSouthAmericaMemes.getHeight(null)));
				
				image.getGraphics().drawImage(seloSouthAmericaMemes, x, y, null);
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, "png", os);
				InputStream is = new ByteArrayInputStream(os.toByteArray());
				
				MessageBuilder builder = new MessageBuilder();
				builder.append(context.getAsMention(true));
				context.sendFile(is, "south_america_memes.png", builder.build());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			context.explain();
		}
	}
}
