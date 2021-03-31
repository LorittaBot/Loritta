package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class TristeRealidadeCommand extends CommandBase {
	public static final String HIDE_DISCORD_TAGS = "esconderTagsDoDiscord";
	public static final String MENTION_USERS = "mencionarUsuarios";

	@Override
	public String getLabel() {
		return "tristerealidade";
	}

	public String getDescription() {
		return "Cria uma triste realidade no seu servidor";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		// ok
		try {
			TristeRealidadeCommandOptions cmdOpti = (TristeRealidadeCommandOptions) context.getConfig().getCommandOptionsFor(this);
			
			BufferedImage bi = ImageIO.read(new File(Loritta.FOLDER + "meme_1.png")); // Primeiro iremos carregar o nosso template
			int x = 0;
			int y = 0;

			Image seloSouthAmericaMemes = ImageIO.read(new File(Loritta.FOLDER + "selo_sam.png")); // Primeiro iremos carregar o nosso template

			seloSouthAmericaMemes = seloSouthAmericaMemes.getScaledInstance(256, 256, 0);
			BufferedImage base = new BufferedImage(384, 256, BufferedImage.TYPE_INT_ARGB); // Iremos criar uma imagem 384x256 (tamanho do template)
			Graphics baseGraph = base.getGraphics();

			Font minecraftia = null;

			try {
				minecraftia = Font.createFont( Font.TRUETYPE_FONT,
						new FileInputStream(new File(Loritta.FOLDER + "minecraftia.ttf")) ); // A fonte para colocar os discriminators
			} catch (Exception e) {
				System.out.println(e);
			}
			baseGraph.setFont(minecraftia.deriveFont(Font.PLAIN, 8)); 

			List<User> users = new ArrayList<User>();

			users.addAll(context.getMessage().getMentionedUsers());

			while (6 > users.size()) {
				Member member = context.getGuild().getMembers().get(Loritta.getRandom().nextInt(context.getGuild().getMembers().size()));

				while (member.getOnlineStatus() == OnlineStatus.OFFLINE || member.getUser().getAvatarUrl() == null) {
					member = context.getGuild().getMembers().get(Loritta.getRandom().nextInt(context.getGuild().getMembers().size()));
				}

				users.add(member.getUser());
			}

			List<User> clonedUserList = new ArrayList<User>(users); // É necessário clonar já que nós iremos mexer nela depois
			
			int val = 0;
			while (6 > val) {
				User member = users.get(0);

				URL imageUrl = new URL(member.getEffectiveAvatarUrl());
				HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
				BufferedImage avatar = ImageIO.read(connection.getInputStream());

				Image avatarImg = avatar.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
				baseGraph.drawImage(avatarImg, x, y, null);

				if (!cmdOpti.hideDiscordTags()) {
					baseGraph.setColor(Color.BLACK);
					baseGraph.drawString(member.getName() + "#" + member.getDiscriminator(), x + 1, y + 12);
					baseGraph.drawString(member.getName() + "#" + member.getDiscriminator(), x + 1, y + 14);
					baseGraph.drawString(member.getName() + "#" + member.getDiscriminator(), x, y + 13);
					baseGraph.drawString(member.getName() + "#" + member.getDiscriminator(), x + 2, y + 13);
					baseGraph.setColor(Color.WHITE);
					baseGraph.drawString(member.getName() + "#" + member.getDiscriminator(), x + 1, y + 13);
				}

				x = x + 128;
				if (x > 256) {
					x = 0;
					y = 128;
				}
				val++;
				users.remove(0);
			}

			baseGraph.drawImage(bi, 0, 0, null);

			if (context.getMessage().getContentDisplay().contains("Mas capricha, vou colocar no grupo do SAM!") || Loritta.getRandom().nextInt(0, 200) == 199) { // Easter Egg: Colocar o selo do South America Memes
				baseGraph.drawImage(seloSouthAmericaMemes, (384 / 2) - (seloSouthAmericaMemes.getWidth(null) / 2), (256 / 2) - (seloSouthAmericaMemes.getHeight(null) / 2), null);
			}
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(base, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			MessageBuilder builder = new MessageBuilder();

			if (cmdOpti.mentionEveryone()) {
				for (User usr : clonedUserList) {
					builder.append(usr);
					builder.append(" ");
				}
			} else {
				builder.append(" ");
			}

			context.sendFile(is, "meme.png", builder.build());
		} catch (IOException e) {
			e.printStackTrace();
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
