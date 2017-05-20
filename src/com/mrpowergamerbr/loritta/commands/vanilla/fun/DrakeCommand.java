package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class DrakeCommand extends CommandBase {
	public static final String HIDE_DISCORD_TAGS = "esconderTagsDoDiscord";
	public static final String MENTION_USERS = "mencionarUsuarios";

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
		// ok
		try {
			BufferedImage bi = ImageIO.read(new File(Loritta.FOLDER + "drake.png")); // Primeiro iremos carregar o nosso template
			Graphics graph = bi.getGraphics();

			List<User> users = new ArrayList<User>();

			users.addAll(context.getMessage().getMentionedUsers());

			while (2 > users.size()) {
				Member member = context.getGuild().getMembers().get(Loritta.getRandom().nextInt(context.getGuild().getMembers().size()));
				users.add(member.getUser());
			}

			User user1 = users.get(0);
			User user2 = users.get(1);
			
			{
				URL imageUrl = new URL(user1.getEffectiveAvatarUrl() + "?size=256");
				HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
				BufferedImage avatar = ImageIO.read(connection.getInputStream());

				Image avatarImg = avatar.getScaledInstance(248, 248, Image.SCALE_SMOOTH);
				graph.drawImage(avatarImg, 248, 0, null);
			}
			
			{
				URL imageUrl = new URL(user2.getEffectiveAvatarUrl() + "?size=256");
				HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
				BufferedImage avatar = ImageIO.read(connection.getInputStream());

				Image avatarImg = avatar.getScaledInstance(248, 248, Image.SCALE_SMOOTH);
				graph.drawImage(avatarImg, 248, 250, null);
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

	@Getter
	@Setter
	@Accessors(fluent = true)
	public static class TristeRealidadeCommandOptions extends CommandOptions {
		public boolean mentionEveryone = false; // Caso esteja ativado, todos que aparecerem serão mencionados
		public boolean hideDiscordTags = false; // Caso esteja ativado, todas as discord tags não irão aparecer na imagem
	}
}
