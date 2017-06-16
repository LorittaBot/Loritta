package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.loritta.utils.TretaNewsGenerator;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class TretaNewsCommand extends CommandBase {
	public static final String HIDE_DISCORD_TAGS = "esconderTagsDoDiscord";
	public static final String MENTION_USERS = "mencionarUsuarios";

	@Override
	public String getLabel() {
		return "tretanews";
	}

	public String getDescription() {
		return "VOOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS ENTÃO VAMOS DIRETO PARA AS NOTÍCIAS";
	}

	public String getUsage() {
		return "[usuário1] [usuário2]";
	}

	public List<String> getExample() {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR");
	}

	public Map<String, String> getDetailedUsage() {
		return ImmutableMap.<String, String>builder()
				.put("usuário1", "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News")
				.put("usuário2", "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News")
				.build();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (!LorittaUtils.canUploadFiles(context)) { return; }
		try {
			User user1 = null;
			User user2 = null;

			if (context.getMessage().getMentionedUsers().size() >= 1) {
				user1 = context.getMessage().getMentionedUsers().get(0);
			}

			if (context.getMessage().getMentionedUsers().size() >= 2) {
				user2 = context.getMessage().getMentionedUsers().get(1);
			}

			if (user1 == null) {
				Member member1 = context.getGuild().getMembers().get(new SplittableRandom().nextInt(context.getGuild().getMembers().size()));

				while (member1.getOnlineStatus() == OnlineStatus.OFFLINE) {
					member1 = context.getGuild().getMembers().get(new SplittableRandom().nextInt(context.getGuild().getMembers().size()));
				}

				user1 = member1.getUser();
			}

			if (user2 == null) {
				Member member2 = context.getGuild().getMembers().get(new SplittableRandom().nextInt(context.getGuild().getMembers().size()));

				while (member2.getOnlineStatus() == OnlineStatus.OFFLINE) {
					member2 = context.getGuild().getMembers().get(new SplittableRandom().nextInt(context.getGuild().getMembers().size()));
				}

				user2 = member2.getUser();
			}

			BufferedImage base = TretaNewsGenerator.generate(context.getGuild(), context.getGuild().getMember(user1), context.getGuild().getMember(user2));

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(base, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			MessageBuilder builder = new MessageBuilder();
			builder.append(context.getAsMention(true) + "VOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS E VAMOS DIRETO PARA AS NOTÍCIAAAAAAAAS!");

			if (context.getConfig().getCommandOptionsFor(this).getAsBoolean(MENTION_USERS)) {
				builder.append(" ");
				builder.append(user1);
				builder.append(" ");
				builder.append(user2);
			} else {

			}

			context.sendFile(is, "tretanews.png", builder.build());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
