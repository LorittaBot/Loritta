package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.ImageUtils;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PrimeirasPalavrasCommand extends CommandBase {
	public static final String HIDE_DISCORD_TAGS = "esconderTagsDoDiscord";
	public static final String MENTION_USERS = "mencionarUsuarios";

	@Override
	public String getLabel() {
		return "primeiraspalavras";
	}

	public String getDescription() {
		return "Ai meu deus... as primeiras palavras do bebê! Ideia original por: @SMix#9658";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {
			if (!LorittaUtils.canUploadFiles(context)) { return; }
			try {
				String str = String.join(" ", context.getArgs());

				BufferedImage bi = ImageIO.read(new File(Loritta.FOLDER + "tirinha_baby.png")); // Primeiro iremos carregar o nosso template

				Graphics baseGraph = bi.getGraphics();

				((Graphics2D) baseGraph).setRenderingHint(
						RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				baseGraph.setColor(new Color(0, 0, 0, 255));

				Font font = new Font("Arial", Font.BOLD, 32);

				baseGraph.setFont(font);

				String quaseFalando = str.charAt(0) + "... " + str.charAt(0) + "...";

				ImageUtils.drawTextWrap(quaseFalando, 4, 5 + font.getSize(), 236, 0, baseGraph.getFontMetrics(), baseGraph);

				ImageUtils.drawTextWrap(str, 4, 277 + font.getSize(), 342, 0, baseGraph.getFontMetrics(), baseGraph);

				MessageBuilder builder = new MessageBuilder().append(context.getAsMention(true));

				context.sendFile(bi, "tirinha_baby.png", builder.build());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.explain(context.getConfig(), context.getEvent());
		}
	}
}
