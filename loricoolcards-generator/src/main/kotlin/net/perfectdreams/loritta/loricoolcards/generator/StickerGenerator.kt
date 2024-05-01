package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.datetime.Clock
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.utils.GraphicsFonts
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

    val sqlCommands = mutableListOf<String>()

    /* val cardGensData = listOf(
        LoriCoolCardsManager.CardGenData(
            "0001",
            CardRarity.RARE,
            "mrpowergamerbr",
            "https://cdn.discordapp.com/avatars/123170274651668480/8bd2b747f135c65fd2da873c34ba485c.png?size=2048",
            "loritta-hey-hey-my-my-yo-yo.png",
            "Talarico",
            "grass_cutter.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0002",
            CardRarity.LEGENDARY,
            "hechfx",
            "https://cdn.discordapp.com/avatars/236167700777271297/a_c50c25909cb964b8423b36887748d5d1.png?size=2048",
            "loritta-and-the-dreamers-tiled.png",
            null,
            null
        ),
        LoriCoolCardsManager.CardGenData(
            "0003",
            CardRarity.LEGENDARY,
            "jvgm45",
            "https://cdn.discordapp.com/avatars/197308318119755776/336223c24d44ceda93f52a3e6b19050f.png?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/chocoholic.png",
            "Stonks!",
            "stonks.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0004",
            CardRarity.LEGENDARY,
            "stephany",
            "https://cdn.discordapp.com/avatars/400683515873591296/a_7cada2bd863b1e5d778b795014966d7b.gif?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/sad-cat-drama.jpeg",
            "Fã da Pantufa",
            "sparkly_member.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0005",
            CardRarity.LEGENDARY,
            "souarth",
            "https://cdn.discordapp.com/avatars/351760430991147010/cfa602bed263d31ae2a0c971b46b3086.png?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/loritta-grafiteira.png",
            null,
            null
        ),
        LoriCoolCardsManager.CardGenData(
            "0006",
            CardRarity.LEGENDARY,
            "srtapaum",
            "https://cdn.discordapp.com/avatars/197501878399926272/a_6c589b30ee99240457e3001472458e1f.gif?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/sad-cat-teddy-bear.jpeg",
            "Equipe do SparklyPower",
            "sparkly_staff.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0007",
            CardRarity.LEGENDARY,
            "peterstark000",
            "https://cdn.discordapp.com/avatars/361977144445763585/ab0260a79d42680625541f85b3586ce2.png?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/kurama.png",
            null,
            null
        ),
        LoriCoolCardsManager.CardGenData(
            "0008",
            CardRarity.LEGENDARY,
            "sjose_",
            "https://cdn.discordapp.com/avatars/472085605623529496/bbbf0d0d0c3e6b2d66c22819fdec6f87.png?size=2048",
            "https://assets.perfectdreams.media/loritta/backgrounds/bahia-yard-lake.jpeg",
            "Fashionista",
            "lori_caneca.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0009",
            CardRarity.LEGENDARY,
            "onathaan",
            "https://cdn.discordapp.com/avatars/437731723350900739/a_ed796c1682bdfe34b1a7706f6316b0a0.png?size=2048",
            "https://assets.perfectdreams.media/loritta/backgrounds/lori-dbz.png",
            null,
            null
        ),
        LoriCoolCardsManager.CardGenData(
            "0010",
            CardRarity.LEGENDARY,
            "danielagc_",
            "https://cdn.discordapp.com/avatars/395788326835322882/46dace3f67309d085d9ca46aee1e12bd.png?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/stardust-speedway-act2.png",
            "Desenvolvedor de Bots Pioneiro",
            "active_developer.png"
        ),
        LoriCoolCardsManager.CardGenData(
            "0011",
            CardRarity.RARE,
            "batato.",
            "https://cdn.discordapp.com/avatars/318181637550637057/b82f3e374302915604dc7653b90c5ef0.png?size=2048",
            "https://files.perfectdreams.media/loritta/profiles/backgrounds/default-blue.png",
            null,
            null
        ),
        LoriCoolCardsManager.CardGenData(
            "0012",
            CardRarity.RARE,
            "furalha",
            "https://cdn.discordapp.com/avatars/716468730799980587/904885acd691c759b71a8985f9db756b.png?size=2048",
            "sparkly-night-purple.png",
            null,
            null
        )
    ) */

    if (true) {
        val fonts = GraphicsFonts()
        val loriCoolCardsManager = LoriCoolCardsManager(fonts)

        val start = Clock.System.now()

        val unknownStickerGIF = loriCoolCardsManager.generateUnknownStickerGIF(

        )

        File("D:\\Pictures\\Loritta\\LoriCoolCards\\card-testrefactor-unknownsticker-animated.gif")
            .writeBytes(unknownStickerGIF)

        println("Took ${Clock.System.now() - start} to generate the card banner")
        return
    }

    while (true) {
        val cardGensData = CardRarity.entries.map {
            LoriCoolCardsManager.CardGenData(
                "0001",
                it,
                "mrpowergamerbr",
                ImageIO.read(URL("https://cdn.discordapp.com/avatars/123170274651668480/8bd2b747f135c65fd2da873c34ba485c.png?size=2048")),
                ImageIO.read(File("D:\\Pictures\\Loritta\\backgrounds\\loritta-hey-hey-my-my-yo-yo.png")),
                "Talarico",
                ImageIO.read(File("C:\\Users\\leona\\IdeaProjects\\LorittaBot\\Loritta\\loritta-bot-discord\\src\\main\\resources\\badges\\grass_cutter.png"))
            )
        }

        val fonts = GraphicsFonts()
        val loriCoolCardsManager = LoriCoolCardsManager(fonts)

        for (cardGenData in cardGensData) {
            val start = Clock.System.now()
            val frontFacingCard = loriCoolCardsManager.generateFrontFacingSticker(cardGenData)
            val stickerReceivedGIF = loriCoolCardsManager.generateStickerReceivedGIF(
                cardGenData.cardRarity,
                frontFacingCard,
                LoriCoolCardsManager.StickerReceivedRenderType.LoriCoolCardsEvent
            )

            File("D:\\Pictures\\Loritta\\LoriCoolCards\\card-testrefactor-${cardGenData.name}-${cardGenData.cardRarity.name}-animated.gif")
                .writeBytes(stickerReceivedGIF)

            println("Took ${Clock.System.now() - start} to generate the card banner")

            /* sqlCommands.add(
            "INSERT INTO loricoolcardseventcards (event, card_id, rarity, title, card_front_image_url, card_received_image_url) VALUES (1, '#$id', '${rarity.name}', '$name', 'https://stuff.loritta.website/loricoolcards/prototype/v0/card-$outputName-front.png', 'https://stuff.loritta.website/loricoolcards/prototype/v0/card-$outputName-animated.gif');"
        ) */
        }

        sqlCommands.forEach {
            println(it)
        }
    }
}