package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread

class QuemJogaCommand : CommandBase() {
    override fun getLabel(): String {
        return "quemjoga";
    }

    override fun getAliases(): MutableList<String> {
        return Arrays.asList("whoplays")
    }

    override fun getDescription(): String {
        return "Veja quem está jogando o jogo que você quer procurar!";
    }

    override fun getExample(): MutableList<String> {
        return Arrays.asList("Shantae: Half-Genie Hero")
    }

    override fun getUsage(): String {
        return "jogo";
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.SOCIAL;
    }

    override fun run(context: CommandContext) {
        if (context.args.size > 0) {
            var message = context.sendMessage(context.getAsMention(true) + "🔍 **Procurando...**")
            thread {
                search(context, message, 1);
            }
        } else {
            this.explain(context);
        }
    }

    fun search(context: CommandContext, message: Message, page: Int) {
        var list = ArrayList<EasyGameWrapper>();
        val game = context.args.joinToString(" ");
        val patt = Pattern.compile("(?i).*" + Pattern.quote(game) + ".*");
        val addedUsers = ArrayList<String>();

        var description = "";

        for (guild in LorittaLauncher.getInstance().lorittaShards.getGuilds()) {
            for (member in guild.members) {
                if (addedUsers.contains(member.user.id)) {
                    continue;
                }
                addedUsers.add(member.user.id)
                val name = member?.game?.name;
                if (name != null) {
                    if (patt.matcher(name).find()) {
                        list.add(EasyGameWrapper(member.user.name + "#" + member.user.discriminator, game, true))
                        continue;
                    }
                }
                val dbObjects = LorittaLauncher.getInstance().mongo.getDatabase("loritta").getCollection("users").find(Filters.eq("_id", member.user.id));
                val doc = dbObjects.first();

                if (doc != null) {
                    var profile = LorittaLauncher.getInstance().ds.get(LorittaProfile::class.java, doc.get("_id"));

                    for (entry in profile.games) {
                        if (patt.matcher(entry.key).find()) {
                            list.add(EasyGameWrapper(member.user.name + "#" + member.user.discriminator, entry.key, false))
                            break;
                        }
                    }
                }
            }
        }
        if (list.isEmpty()) {
            message.editMessage(context.getAsMention(true) + "Infelizmente ninguém joga `" + game + "`... 😢").complete() // Editar a mensagem, avisando que nada foi encontrado (triste...)
        } else {
            var sorted = list.sortedWith(compareBy({ it.playingNow })).reversed();
            var idx = 0;
            var cut = page * 15;
            if (cut > sorted.size) {
                cut = sorted.size;
            }
            sorted = sorted.subList(((page - 1) * 15), cut)
            for (wrapper in sorted) {
                if (description.length > 1900) {
                    continue;
                }
                if (wrapper.playingNow) {
                    description += "🎮 **" + wrapper.user + "** está atualmente jogando `" + wrapper.game + "`!\n";
                } else {
                    description += "💤 **" + wrapper.user + "** joga `" + wrapper.game + "`!\n";
                }
                idx++;
            }

            val embed = EmbedBuilder()
                    .setDescription(description)
                    .build()

            context.metadata.put("currentPage", 1); // Guardar a página atual
            message.editMessage(embed).complete() // Editar a mensagem
            message.addReaction("▶").complete() // Adicionar reactions
        }
    }

    override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
        if (e.member == context.handle) { // Somente quem executou o comando pode avançar
            var currentPage = context.metadata.get("currentPage") as Int + 1;
            context.metadata.put("currentPage", currentPage);
            msg.clearReactions().complete(); // Tirar todos os reactions
            var editedMsg = msg.editMessage(EmbedBuilder().setDescription("...").build()).complete() // "remover" o embed
            editedMsg = editedMsg.editMessage(context.getAsMention(true) + "🔍 **Procurando...**").complete() // Falar que nós estamos procurando...
            thread() {
                search(context, editedMsg, currentPage);
            }
        }
    }

    data class EasyGameWrapper(val user: String, val game: String, val playingNow: Boolean)
}