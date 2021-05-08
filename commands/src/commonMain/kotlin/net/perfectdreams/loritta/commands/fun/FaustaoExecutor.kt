package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.images.URLImageReference

class FaustaoExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(FaustaoExecutor::class)

    // TODO: Localization
    private val frases = listOf(
        "Que isso bicho, ó u cara lá ó",
        "Vamos ver as vídeo cassetadas!",
        "Voltamos já com vídeo cassetadas!",
        "ERRRROOOOOOOOOUUUUUUUU!!!!",
        "E agora, pra desligar essa merda aí, meu. Porra ligou, agora desliga! Tá pegando fogo, bicho!",
        "TÁ PEGANDO FOGO, BICHO!",
        "OLOCO!",
        "Essa fera ai, bicho!",
        "Essa fera ai, meu!",
        "Você destruiu o meu ovo! \uD83C\uDF73",
        "Ih Serjão, sujou! \uD83C\uDFC3\uD83D\uDCA8",
        "ERROU! ⚠",
        "Você vai morrer ⚰",
        "Olha o tamanho da criança",
        "Oito e sete",
        "Ô loco meu!",
        "É brincadera, bicho!",
        "Se vira nos 30!",
        "Quem sabe faz ao vivo!",
        "A TV é chata no domingo, é para quem não tem dinheiro nem o que fazer. Eu trabalho no domingo por isso. O domingo é chato. Para quem pode viajar e passear, o domingo é maravilhoso.",
        "Logo após os reclames do plim-plim!",
        "Olha só o que faz a maldita manguaça, bicho!",
        "{user} é bom tanto no pessoal quanto no profissional.",
        "Essa fera {user} aqui no domingão!"
    )

    private val avatars = listOf(
        "http://i.imgur.com/PS61w6I.png",
        "http://i.imgur.com/ofr6Tkj.png",
        "http://i.imgur.com/nABrbqD.png",
        "http://i.imgur.com/igpGeyg.png",
        "http://i.imgur.com/db2TFRm.png",
        "http://i.imgur.com/RAPYIU9.png",
        "http://i.imgur.com/rVmgwZC.png",
        "http://i.imgur.com/z7Ec5I3.png"
    )

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage {
            impersonation("Faustão", URLImageReference(avatars.random()))

            content = frases.random().replace("{user}", context.user.asMention)
        }
    }
}