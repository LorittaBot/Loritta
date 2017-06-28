package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DrakeCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "drake";
	}

	public String getDescription() {
		return "Cria um meme do Drake usando dois usuários da sua guild!";
	}

	public List<String> getExample() {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR");
	}

	public Map<String, String> getDetailedUsage() {
		return ImmutableMap.<String, String>builder()
				.put("usuário1", "*(Opcional)* Usuário sortudo")
				.put("usuário2", "*(Opcional)* Usuário sortudo")
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
			BufferedImage bi = ImageIO.read(new File(Loritta.FOLDER + "drake.png")); // Primeiro iremos carregar o nosso template
			Graphics graph = bi.getGraphics();

			{
				Image avatarImg = LorittaUtils.getImageFromContext(context, 0);
				avatarImg = avatarImg.getScaledInstance(248, 248, Image.SCALE_SMOOTH);
				graph.drawImage(avatarImg, 248, 0, null);

				if (!LorittaUtils.isValidImage(context, avatarImg)) {
					return;
				}
			}
			
			{
				Image avatarImg = LorittaUtils.getImageFromContext(context, 1);
				avatarImg = avatarImg.getScaledInstance(248, 248, Image.SCALE_SMOOTH);
				graph.drawImage(avatarImg, 248, 250, null);

				if (!LorittaUtils.isValidImage(context, avatarImg)) {
					return;
				}
			}
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			MessageBuilder builder = new MessageBuilder();
			builder.append(context.getAsMention(true));
			context.sendFile(is, "meme.png", builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
