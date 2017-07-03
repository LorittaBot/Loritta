package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class InviteCommand : CommandBase() {
    override fun getLabel(): String {
        return "convidar";
    }

    override fun getAliases(): MutableList<String> {
        return Arrays.asList("invite");
    }

    override fun getDescription(): String {
        return "Envia o link do convite para adicionar a Loritta em outros servidores!";
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.SOCIAL;
    }

    override fun run(context: CommandContext) {
        var embed = EmbedBuilder()
                .setDescription("Você quer me adicionar em outros servidores/guilds do Discord? Então clique [aqui](https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975) para me adicionar em outro servidor!\n\nSe você quiser configurar algumas coisas (como o meu prefixo, comandos ativados, etc) então acesse o painel de administração clicando [aqui](http://loritta.website/auth)!\n\nE, é claro, entre na guild da Loritta para dar sugestões, reportar bugs e muito mais! https://discord.gg/3rXgN8x")
                .setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")
                .setColor(Color(0, 193, 223))
                .build()

        context.sendMessage(embed)
    }
}