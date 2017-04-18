package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.MessageBuilder;

public class NyanCatCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "nyan";
	}

	@Override
	public String getDescription() {
		return "Nyan Cat, diretamente no seu servidor! E você pode pedir o tamanho do Nyan Cat igual quando você pede algum sanduíche no Subway!";
	}
	
	@Override
	public String getUsage() {
		return "cat";
	}
	
	@Override
	public List<String> getExample() {
		return Arrays.asList("", "cat", "caaaaaaat", "caaaaaaaaaaaaat");
	}
	
	@Override
	public void run(CommandContext context) {
		int times = 0;
		if (context.getArgs().length == 1) {
			times = StringUtils.countMatches(context.getArgs()[0], "a");
		}
		
		try {
			Image catLeft = ImageIO.read(new File(Loritta.FOLDER + "cat_left.png"));
			Image catRight = ImageIO.read(new File(Loritta.FOLDER + "cat_right.png"));
			Image catMiddle = ImageIO.read(new File(Loritta.FOLDER + "cat_middle.png"));
			Image catMiddle2 = ImageIO.read(new File(Loritta.FOLDER + "cat_middle2.png"));
			Image catMiddle3 = ImageIO.read(new File(Loritta.FOLDER + "cat_middle3.png"));
			Image catMiddle4 = ImageIO.read(new File(Loritta.FOLDER + "cat_middle4.png"));
			Image catMiddle5 = ImageIO.read(new File(Loritta.FOLDER + "cat_middle5.png"));

			BufferedImage bi = new BufferedImage(catLeft.getWidth(null) + catRight.getWidth(null) + (catMiddle.getWidth(null) * times), catLeft.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			int x = 0;

			bi.getGraphics().drawImage(catLeft, x, 0, null);

			x += catLeft.getWidth(null);

			int idx = 0;

			while (times > idx) {
				int c = Loritta.getRandom().nextInt(0, 5);
				if (c == 0) {
					bi.getGraphics().drawImage(catMiddle, x, 0, null);
				}
				if (c == 1) {
					bi.getGraphics().drawImage(catMiddle2, x, 0, null);
				}
				if (c == 2) {
					bi.getGraphics().drawImage(catMiddle3, x, 0, null);
				}
				if (c == 3) {
					bi.getGraphics().drawImage(catMiddle4, x, 0, null);
				}
				if (c == 4) {
					bi.getGraphics().drawImage(catMiddle5, x, 0, null);
				}
				x += catMiddle.getWidth(null);
				idx++;
			}

			bi.getGraphics().drawImage(catRight, x, 0, null);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			MessageBuilder builder = new MessageBuilder();
			builder.append(context.getAsMention(true));
			context.sendFile(is, "nyan_cat.png", builder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
