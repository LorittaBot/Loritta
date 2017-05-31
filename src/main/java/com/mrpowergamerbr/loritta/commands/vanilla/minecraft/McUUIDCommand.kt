package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.util.*

class McUUIDCommand : CommandBase() {
    override fun getLabel(): String {
        return "mcuuid";
    }

    override fun getDescription(): String {
        return "Pega a UUID de um usuário";
    }

    override fun getUsage(): String {
        return "nickname"
    }

    override fun getExample(): List<String> {
        return Arrays.asList("Monerk")
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.MINECRAFT;
    }

    override fun run(context: CommandContext) {
        if (context.args.size > 0) {
            var player = context.args[0];

            var data = HttpRequest.get("https://api.mojang.com/users/profiles/minecraft/" + player).body();


            try {
                var json = JsonParser().parse(data).asJsonObject;

                context.sendMessage(context.getAsMention(true) + "A UUID de " + player + ": `" + LorittaUtils.getUUID(json.get("id").asString) + "`");
            } catch (e: IllegalStateException) {
                context.sendMessage(context.getAsMention(true) + "Player não encontrado! Tem certeza que `" + player + "` é uma conta válida?");
            }
        } else {
            this.explain(context);
        }
    }
}