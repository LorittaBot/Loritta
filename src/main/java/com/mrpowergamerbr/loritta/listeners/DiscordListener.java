package com.mrpowergamerbr.loritta.listeners;

import com.mongodb.client.model.Filters;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {
    Loritta loritta;

    public DiscordListener(Loritta loritta) {
        this.loritta = loritta;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.isFromType(ChannelType.TEXT)) {
            loritta.getExecutor().execute(() -> {
                try {
                    ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());
                    LorittaProfile profile = loritta.getLorittaProfileForUser(event.getMember().getUser().getId());

                    if (event.getMessage().getRawContent().replace("!", "").equals("<@297153970613387264>")) {
                        event.getTextChannel().sendMessage("Olá " + event.getMessage().getAuthor().getAsMention() + "! Meu prefixo neste servidor é `" + conf.commandPrefix() + "` Para ver o que eu posso fazer, use `" + conf.commandPrefix() + "ajuda`!").complete();
                    }

                    for (Role r : event.getMember().getRoles()) {
                        if (r.getName().equalsIgnoreCase("Inimigo da Loritta")) {
                            return;
                        }
                    }

                    LorittaProfile lorittaProfile = loritta.getLorittaProfileForUser(event.getAuthor().getId());
                    lorittaProfile.setXp(lorittaProfile.getXp() + 1);
                    loritta.getDs().save(lorittaProfile);

                    LorittaServerUserData userData = conf.userData.getOrDefault(event.getMember().getUser().getId(), new LorittaServerUserData());
                    userData.setXp(userData.getXp() + 1);
                    conf.userData.put(event.getMember().getUser().getId(), userData);
                    loritta.getDs().save(conf);

                    if (conf.aminoConfig.getFixAminoImages()) {
                        for (Attachment attachments : event.getMessage().getAttachments()) {
                            if (attachments.getFileName().endsWith(".Amino")) {
                                BufferedImage bufferedImage = LorittaUtils.downloadImage(attachments.getUrl());

                                ByteArrayOutputStream os = new ByteArrayOutputStream();
                                try {
                                    ImageIO.write(bufferedImage, "png", os);
                                } catch (Exception e) {}
                                InputStream is = new ByteArrayInputStream(os.toByteArray());

                                event.getTextChannel().sendFile(is, "amino.png", new MessageBuilder().append("(Por " + event.getMember().getAsMention() + ") **Link para o \".Amino\":** " + attachments.getUrl()).build()).complete();
                                event.getMessage().delete().complete();
                            }
                        }
                    }
                    // Primeiro os comandos vanilla da Loritta(tm)
                    for (CommandBase cmd : loritta.getCommandManager().getCommandMap()) {
                        if (conf.debugOptions().enableAllModules() || !conf.disabledCommands().contains(cmd.getClass().getSimpleName())) {
                            if (cmd.handle(event, conf, profile)) {
                                // event.getChannel().sendTyping().queue();
                                CommandOptions cmdOpti = conf.getCommandOptionsFor(cmd);
                                if (conf.deleteMessageAfterCommand() || cmdOpti.deleteMessageAfterCommand()) {
                                    event.getMessage().delete().complete();
                                }
                                return;
                            }
                        }
                    }

                    // E depois os comandos usando JavaScript (Nashorn)
                    for (NashornCommand cmd : conf.nashornCommands()) {
                        if (cmd.handle(event, conf, profile)) {

                        }
                    }

                    loritta.getHal().add(event.getMessage().getContent().toLowerCase()); // TODO: Filtrar links
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent e) {
        if (e.getUser().isBot()) { return; } // Ignorar reactions de bots

        if (LorittaLauncher.getInstance().messageContextCache.containsKey(e.getMessageId())) {
            CommandContext context = (CommandContext) LorittaLauncher.getInstance().messageContextCache.get(e.getMessageId());
            Thread t = new Thread() {
                public void run() {
                    Message msg = e.getTextChannel().getMessageById(e.getMessageId()).complete();
                    if (msg != null) {
                        context.cmd.onCommandReactionFeedback(context, e, msg);
                    }
                }
            };
            t.start();
        }
        if (LorittaLauncher.getInstance().getMusicMessagesCache().containsKey(e.getMessageId())) {
            AudioTrackWrapper atw = (AudioTrackWrapper) LorittaLauncher.getInstance().getMusicMessagesCache().get(e.getMessageId());

            int count = e.getReaction().getUsers().complete().stream().filter((user) -> !user.isBot()).collect(Collectors.toList()).size();
            ServerConfig conf = LorittaLauncher.getInstance().getServerConfigForGuild(e.getGuild().getId());

            if (count > 0 && conf.musicConfig().isVoteToSkip() && LorittaLauncher.getInstance().getGuildAudioPlayer(e.getGuild()).scheduler.getCurrentTrack() == atw) {
                VoiceChannel vc = e.getGuild().getVoiceChannelById(conf.musicConfig().getMusicGuildId());

                if (e.getMember().getVoiceState().getChannel() != vc) {
                    e.getReaction().removeReaction(e.getUser()).complete();
                    return;
                }

                if (vc != null) {
                    int inChannel = vc.getMembers().stream().filter((member) -> !member.getUser().isBot()).collect(Collectors.toList()).size();
                    long required = Math.round((double) inChannel * ((double) conf.musicConfig().getRequired() / 100));

                    if (count >= required) {
                        LorittaLauncher.getInstance().skipTrack(e.getGuild());
                        e.getTextChannel().sendMessage("🤹 Música pulada!").complete();
                        LorittaLauncher.getInstance().getMusicMessagesCache().remove(e.getMessageId());
                    }
                }
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent e) {
        // Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor

        LorittaLauncher.getInstance().getMongo()
                .getDatabase("loritta")
                .getCollection("servers")
                .deleteMany(Filters.eq("_id", e.getGuild().getId())); // Tchau! :(
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        loritta.getExecutor().execute(() -> {
            try {
                ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());

                if (conf.joinLeaveConfig().isEnabled()) {
                    if (conf.joinLeaveConfig().isTellOnJoin()) {
                        Guild guild = event.getGuild();

                        TextChannel textChannel = guild.getTextChannelById(conf.joinLeaveConfig().getCanalJoinId());

                        if (textChannel != null) {
                            if (textChannel.canTalk()) {
                                String msg = LorittaUtils.replaceTokens(conf.joinLeaveConfig().getJoinMessage(), event);
                                textChannel.sendMessage(msg).complete();
                            } else {
                                LorittaUtils.warnOwnerNoPermission(guild, textChannel, conf);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        loritta.getExecutor().execute(() -> {
            try {
                ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());

                if (conf.joinLeaveConfig().isEnabled()) {
                    if (conf.joinLeaveConfig().isTellOnLeave()) {
                        Guild guild = event.getGuild();

                        TextChannel textChannel = guild.getTextChannelById(conf.joinLeaveConfig().getCanalLeaveId());

                        if (textChannel != null) {
                            if (textChannel.canTalk()) {
                                String msg = LorittaUtils.replaceTokens(conf.joinLeaveConfig().getLeaveMessage(), event);
                                textChannel.sendMessage(msg).complete();
                            } else {
                                LorittaUtils.warnOwnerNoPermission(guild, textChannel, conf);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
