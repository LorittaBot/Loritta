import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import java.io.File
import java.time.ZoneId

suspend fun main() {
    val m = DiscordMessageRendererManager(
        ZoneId.of("America/Sao_Paulo"),
        setOf(
            "image/png"
        )
    )

    val imageAsByteArray = m.renderMessage(
        Json.decodeFromString("""{"id":1267229223165231186,"placeContext":{"type":"net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedAttachedGuild","id":268353819409252352,"channelId":739823666891849729,"channelName":"drafty-drafts","channelType":"TEXT","name":"Ideias Aleatórias","iconId":"caf959735a24b4bba1d31bb412fef58e"},"author":{"id":123170274651668480,"name":"mrpowergamerbr","discriminator":"0000","globalName":"MrPowerGamerBR","avatarId":"8bd2b747f135c65fd2da873c34ba485c","isBot":false,"isSystem":false,"flagsRaw":4326016},"member":{"nickname":null,"roles":[{"id":297051132793061378,"name":"new rolex","colorRaw":15277667,"icon":null},{"id":334711955736625185,"name":"test role","colorRaw":536870911,"icon":null},{"id":401353261266763778,"name":"abc","colorRaw":536870911,"icon":null},{"id":650869752524439563,"name":"pioioioio","colorRaw":536870911,"icon":null},{"id":996535574615838800,"name":"Stonkeiros","colorRaw":3066993,"icon":null},{"id":1097286707319156806,"name":"test role 123","colorRaw":536870911,"icon":null},{"id":1134949643915116554,"name":"GamerSafer Verified","colorRaw":3066993,"icon":null}]},"timeEdited":null,"content":"hello world <@297153970613387264> <@744361365724069898>","embeds":[],"attachments":[],"stickers":[],"mentions":{"users":[],"roles":[]},"reactions":[]}"""),
        null
    )

    File("renders/renderer_result_${System.currentTimeMillis()}.png")
        .writeBytes(imageAsByteArray)

    m.close()

    println("Done!")
}