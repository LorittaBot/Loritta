package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils.fetchRemoteFile
import com.mrpowergamerbr.temmiesc2kparser.TemmieSC2KParser
import com.mrpowergamerbr.temmiesc2kparser.utils.MiscIndex

class SC2000ParserCommand : CommandBase() {
    override fun getLabel(): String {
        return "sc2000info"
    }

    override fun getDescription(): String {
        return "Veja as estatísticas de uma cidade do SimCity 2000! (Você precisa fazer o upload dela no discord e usar este comando!)"
    }

    override fun run(context: CommandContext) {
        if (!context.message.attachments.isEmpty()) {
            var attach = context.message.attachments[0];

            if (!attach.isImage && attach.fileName.toLowerCase().endsWith("sc2")) {
                try {
                    var bytes = fetchRemoteFile(attach.url)

                    var city = TemmieSC2KParser.readCity(bytes)

                    context.sendMessage(context.getAsMention(true) + "\n📖 Nome da Cidade: ${city!!.cityName}\n🍾 Ano Inaugurado: ${city.miscellaneous.get(MiscIndex.YEAR_FOUNDED)}\n💁 Prefeito: ${city.labels.get(0)}\n💵 Dinheiro da Cidade: ${city.miscellaneous.get(MiscIndex.MONEY_SUPPLY)} Simoleons")
                } catch(e: Exception) {
                    e.printStackTrace()
                    context.sendMessage(context.getAsMention(true) + "Não consegui baixar o arquivo! Motivo: " + e.cause)
                    return;
                }

            } else {
                context.sendMessage(context.getAsMention(true) + "Isto não é um save do SimCity 2000!")
            }
        } else {
            context.sendMessage(context.getAsMention(true) + "Você precisa fazer upload de um save do SimCity 2000 e usar \"+sc2000info\" na mesma mensagem!")
        }
    }
}