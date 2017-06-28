package com.mrpowergamerbr.loritta.commands.vanilla.undertale;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.ImageUtils;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import net.dv8tion.jda.core.entities.Member;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class UndertaleBoxCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "undertalebox";
	}

	public String getDescription() {
		return "Cria uma caixa de diálogo igual ao do Undertale";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("@Loritta Legendary being made of every SOUL in the underground.");
	}

	@Override
	public String getUsage() {
		return "usuário (caso queira) mensagem";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UNDERTALE;
	}

	@Override
	public void run(CommandContext context) {
		try {
			if (context.getArgs().length >= 1) {
				if (!LorittaUtils.canUploadFiles(context)) { return; }
				Member member = context.getHandle();
				if (context.getMessage().getMentionedUsers().size() == 1) {
					member = context.getGuild().getMember(context.getMessage().getMentionedUsers().get(0));
				}
				String str = String.join(" ", context.getArgs()); // Primeiro nós juntamos tudo
				// Mas ok, ainda tem uma coisa chamada "nome do usuário mencionado"
				// Sabe o que a gente faz com ele? Gambiarra!
				// TODO: Menos gambiarra
				str = str.replace("@" + member.getEffectiveName() + " ", "");
				BufferedImage bi = ImageIO.read(new File(Loritta.FOLDER + "undertale_dialogbox.png"));
				Graphics graph = bi.getGraphics();

				Font determinationMono = null;
				try {
					determinationMono = Font.createFont( Font.TRUETYPE_FONT,
							new FileInputStream(new File(Loritta.FOLDER + "DTM-Mono.otf")) );
				} catch (Exception e) {
					System.out.println(e);
				}

				graph.setFont(determinationMono.deriveFont(Font.PLAIN, 27)); 
				graph.setColor(Color.WHITE);

				// graph.getFontMetrics(determinationMono) tem problemas, a width do char é sempre 1 (bug?)
				ImageUtils.drawTextWrap(str, 180, 56 + determinationMono.getSize(), 578, 0, graph.getFontMetrics(), graph);

				URL imageUrl = new URL(member.getUser().getEffectiveAvatarUrl());
				HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
				BufferedImage avatar = ImageIO.read(connection.getInputStream());

				Image avatarImg = avatar.getScaledInstance(128, 128, Image.SCALE_SMOOTH);

				BufferedImage blackWhite = new BufferedImage(avatarImg.getWidth(null), avatarImg.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
				Graphics2D g2d = blackWhite.createGraphics();
				g2d.drawImage(avatarImg, 0, 0, null);
				blackWhite.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH);
				
				if (true) {
					// TODO: This
					Font minecraftia = null;
					try {
						minecraftia = Font.createFont( Font.TRUETYPE_FONT,
								new FileInputStream(new File(Loritta.FOLDER + "minecraftia.ttf")) );
					} catch(Exception e) {
						System.out.println(e);
					}
					
					graph.setFont(minecraftia.deriveFont(Font.PLAIN, 8)); 
					
					int x = 0;
					int y = 166;
					graph.setColor(Color.BLACK);
					graph.drawString(member.getUser().getName() + "#" + member.getUser().getDiscriminator(), x + 1, y + 12);
					graph.drawString(member.getUser().getName() + "#" + member.getUser().getDiscriminator(), x + 1, y + 14);
					graph.drawString(member.getUser().getName() + "#" + member.getUser().getDiscriminator(), x, y + 13);
					graph.drawString(member.getUser().getName() + "#" + member.getUser().getDiscriminator(), x + 2, y + 13);
					graph.setColor(Color.WHITE);
					graph.drawString(member.getUser().getName() + "#" + member.getUser().getDiscriminator(), x + 1, y + 13);
				}

				graph.drawImage(blackWhite, 20, 22, null);

				context.sendFile(bi, "undertale_box.png", context.getAsMention(true));
			} else {
				context.explain();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
