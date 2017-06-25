package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.io.File
import java.util.stream.Collectors

class AjudaCommand : CommandBase() {

    override fun getLabel(): String {
        return "ajuda"
    }

    override fun getDescription(): String {
        return "Mostra todos os comandos disponíveis que eu posso executar, lembrando que isto só irá mostrar os comandos habilitados no servidor que você executou a ajuda!"
    }

    override fun getAliases(): List<String> {
        return listOf("help", "comandos")
    }

    override fun run(context: CommandContext) {
        if (true /* cmdOptions.getAsBoolean(TELL_SENT_IN_PRIVATE) */) {
            context.event.textChannel.sendMessage(context.getAsMention(true) + "Enviei para você no privado! ;)").complete()
        }

        var description = "Olá " + context.userHandle.asMention + ", eu me chamo Loritta (ou, para amigos(as) mais próximos(as), \"Lori\") e eu sou apenas um simples bot para o Discord!\n\nO meu objetivo é ser um bot com várias funções, extremamente modular, fácil de usar e super customizável para qualquer servidor/guild brasileiro poder usar! (Quer me adicionar no seu servidor? Então clique [aqui](https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975))!\n\nAtualmente você está vendo a ajuda do **" + context.guild.name + "**!"

        var builder = EmbedBuilder()
                .setColor(Color(39, 153, 201))
                .setTitle("💁 Ajuda da Loritta")
                .setDescription(description)
                .setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")

        var firstMsgSent = fastEmbedSend(context, listOf(builder.build()))[0] // Nós iremos dar pin nela

        val embed = EmbedBuilder()
        embed.setThumbnail("http://i.imgur.com/LUHLEs9.png")
        embed.setColor(Color(186, 0, 239))

        val disabledCommands = LorittaLauncher.getInstance().commandManager.getCommandsDisabledIn(context.config)

        val socialCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.SOCIAL, "http://i.imgur.com/Ql6EiAw.png")
        val discordCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.DISCORD, "https://lh3.googleusercontent.com/_4zBNFjA8S9yjNB_ONwqBvxTvyXYdC7Nh1jYZ2x6YEcldBr2fyijdjM2J5EoVdTpnkA=w300")
        val minecraftCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.MINECRAFT, "http://i.imgur.com/gKBHNzL.png")
        val undertaleCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.UNDERTALE, "http://vignette2.wikia.nocookie.net/animal-jam-clans-1/images/0/08/Annoying_dog_101.gif/revision/latest?cb=20151231033006")
        val pokemonCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.POKEMON, "http://i.imgur.com/2l5kKCp.png")
        val funCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.FUN, "http://i.imgur.com/ssNe7dx.png")
        val miscCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.MISC, "http://i.imgur.com/Qs8MyFy.png")
        val utilsCmds = getCommandsFor(context.config, disabledCommands, CommandCategory.UTILS, "http://i.imgur.com/eksGMGw.png")

        val aboutMe = EmbedBuilder()
        aboutMe.setTitle("Sobre o Criador", null)
        aboutMe.setThumbnail("http://i.imgur.com/nhBZ8i4.png")
        aboutMe.setDescription("Loritta foi criado pelo MrPowerGamerBR. :wink:")
        aboutMe.addField("Website", "http://mrpowergamerbr.com/", true)
        aboutMe.addField("Discord", "MrPowerGamerBR#4185", true)
        aboutMe.addField("Twitter", "@mrpowergamerbr", true)

        val sparklyPower = EmbedBuilder()
                .setColor(Color(0, 255, 168))
        sparklyPower.setTitle("Reclames do Plim Plim #1", null)
        sparklyPower.setThumbnail("http://sparklypower.net/SparklyPower_Logo_250.png")
        sparklyPower.setDescription("Gostou da qualidade do Loritta? Gosta de Minecraft? Survival? Que tal jogar no SparklyPower então? :slight_smile:")
        sparklyPower.addField("Website", "https://sparklypower.net/", true)
        sparklyPower.addField("IP", "jogar.sparklypower.net (Versão 1.11.2)", true)

        val additionalInfoEmbed = EmbedBuilder()
        additionalInfoEmbed.setTitle("Informações Adicionais", null)
                .setColor(Color(39, 153, 201))
        additionalInfoEmbed.setDescription("[Todos os comandos da Loritta](https://loritta.website/comandos)\n"
                + "[Discord da nossa querida Loritta](https://discord.gg/3rXgN8x)\n"
                + "[Adicione a Loritta no seu servidor!](https://loritta.website/auth)\n"
                + "[Amou o Loritta? Tem dinheirinho de sobra? Então doe!](https://loritta.website/doar)\n"
                + "[Website do MrPowerGamerBR](https://mrpowergamerbr.com/)")

        val cmdOptions = context.config.getCommandOptionsFor(this)

        if (socialCmds != null) {
            fastEmbedSend(context, socialCmds);
        }
        if (discordCmds != null) {
            fastEmbedSend(context, discordCmds);
        }
        if (minecraftCmds != null) {
            fastEmbedSend(context, minecraftCmds);
        }
        if (undertaleCmds != null) {
            fastEmbedSend(context, undertaleCmds);
        }
        if (pokemonCmds != null) {
            fastEmbedSend(context, pokemonCmds);
        }
        if (funCmds != null) {
            fastEmbedSend(context, funCmds);
        }
        if (miscCmds != null) {
            fastEmbedSend(context, miscCmds);
        }
        if (utilsCmds != null) {
            fastEmbedSend(context, utilsCmds);
        }

        context.sendMessage(sparklyPower.build())
        context.sendMessage(additionalInfoEmbed.build())

        firstMsgSent.pin().complete();

        // E agora vamos enviar o aviso do pin
        context.sendFile(File(Loritta.FOLDER + "pinned.png"), "aviso.png", "**Se você quiser voltar para o topo das mensagens de ajuda do " + context.guild.name + ", então clique nas mensagens fixadas!**")
    }

    /**
     * Envia uma embed com imagens de uma maneira mais rápido
     *
     * Para fazer isto, nós enviamos uma embed sem imagens e depois editamos com as imagens, já que o Discord "escaneia" as
     * imagens antes de enviar para o destinatário... usando o "truque" o usuário irá receber sem as imagens e depois irá receber
     * a versão editada com imagens, economizando tempo ao tentar enviar várias embeds de uma só vez
     */
    fun fastEmbedSend(context: CommandContext, embeds: List<MessageEmbed>): List<Message> {
        var messages = ArrayList<Message>();
        for (embed in embeds) {
            var clone = EmbedBuilder(embed)
            clone.setImage(null)
            clone.setThumbnail(null)
            var sentMsg = context.sendMessage(clone.build())
            sentMsg.editMessage(embed).queue(); // Vamos enviar em uma queue para não atrasar o envio
            messages.add(sentMsg);
        }
        return messages;
    }

    fun getCommandsFor(conf: ServerConfig, availableCommands: List<CommandBase>, cat: CommandCategory, image: String): MutableList<MessageEmbed> {
        val embeds = ArrayList<MessageEmbed>();
        var embed = EmbedBuilder()
        embed.setTitle(cat.fancyTitle, null)
        embed.setThumbnail(image)

        var color = Color(255, 255, 255);

        if (cat == CommandCategory.DISCORD) {
            color = Color(121, 141, 207);
        } else if (cat == CommandCategory.SOCIAL) {
            color = Color(231, 150, 90);
        } else if (cat == CommandCategory.UNDERTALE) {
            color = Color(250, 250, 250);
        } else if (cat == CommandCategory.POKEMON) {
            color = Color(255, 13, 0);
        } else if (cat == CommandCategory.MINECRAFT) {
            color = Color(50, 141, 145);
        } else if (cat == CommandCategory.MISC) {
            color = Color(255, 176, 0);
        } else if (cat == CommandCategory.UTILS) {
            color = Color(176, 146, 209);
        } else {
            color = Color(186, 0, 239);
        }

        embed.setColor(color)

        var description = "*" + cat.description + "*\n\n";
        val categoryCmds = LorittaLauncher.getInstance().commandManager.commandMap.stream().filter { cmd -> cmd.category == cat }.collect(Collectors.toList<CommandBase>())

        if (!categoryCmds.isEmpty()) {
            for (cmd in categoryCmds) {
                if (!conf.disabledCommands.contains(cmd.javaClass.simpleName)) {
                    var toBeAdded = "[" + conf.commandPrefix + cmd.label + "]()" + (if (cmd.usage != null) " `" + cmd.usage + "`" else "") + " - " + cmd.description + "\n";
                    if ((description + toBeAdded).length > 2048) {
                        embed.setDescription(description);
                        embeds.add(embed.build());
                        embed = EmbedBuilder();
                        embed.setColor(color)
                        description = "";
                    }
                    description += "[" + conf.commandPrefix + cmd.label + "]()" + (if (cmd.usage != null) " `" + cmd.usage + "`" else "") + " - " + cmd.description + "\n";
                }
            }
            embed.setDescription(description)
            embeds.add(embed.build());
            return embeds
        } else {
            return embeds
        }
    }

    companion object {
        val SEND_IN_PRIVATE = "enviarNoPrivado"
        val TELL_SENT_IN_PRIVATE = "avisarQueFoiEnviadoNoPrivado"
    }
}
