package net.perfectdreams.loritta.website.backend.utils

import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse

class EtherealGambiImages(private val etherealGambiClient: EtherealGambiClient) {
    private val all = mutableListOf<PreloadedImageInfo>()

    val serverIcons = ServerIcons()
    val commands = Commands()
    val emotes = Emotes()

    val lorittaGabi = registerImagePreload("/loritta/loritta-gabi")
    val lorittaPrize = registerImagePreload("/loritta/loritta-prize")
    val lorittaCommunity = registerImagePreload("/loritta/loritta-community")
    val lorittaCommands = registerImagePreload("/loritta/loritta-commands")
    val lorittaNotification = registerImagePreload("/loritta/loritta-notification")
    val lorittaNotificationVideo = registerImagePreload("/loritta/loritta-notification-video")
    val lorittaSupport = registerImagePreload("/loritta/loritta-support")
    val robloxLogo = registerImagePreload("/loritta/roblox-logo")
    val lorittaMinecraft = registerImagePreload("/loritta/loritta-minecraft")
    val lorittaAnime = registerImagePreload("/loritta/loritta-anime")
    val lorittaVideos = registerImagePreload("/loritta/loritta-videos")
    val lorittaPikachu = registerImagePreload("/loritta/loritta-pikachu")
    val lorittaSans = registerImagePreload("/loritta/loritta-sans")
    val lorittaHug = registerImagePreload("/loritta/loritta-hug")
    val lorittaMiscellaneous = registerImagePreload("/loritta/loritta-miscellaneous")
    val lorittaUtilities = registerImagePreload("/loritta/loritta-utilities")
    val lorittaWumpus = registerImagePreload("/loritta/loritta-wumpus")
    val lorittaSocial = registerImagePreload("/loritta/loritta-social")
    val lorittaModeration = registerImagePreload("/loritta/loritta-moderation")
    val lorittaFun = registerImagePreload("/loritta/loritta-fun")
    val lorittaImages = registerImagePreload("/loritta/loritta-images")
    val lorittaFortnite = registerImagePreload("/loritta/loritta-fortnite")

    val lorittaJumbotronBase = registerImagePreload("/loritta/loritta-jumbotron/loritta-v3-outline12px-base")
    val lorittaJumbotronEyesOpen = registerImagePreload("/loritta/loritta-jumbotron/loritta-v3-outline12px-eyes")
    val lorittaJumbotronDark = registerImagePreload("/loritta/loritta-jumbotron/loritta-v3-outline12px-dark")
    val lorittaJumbotronBlush = registerImagePreload("/loritta/loritta-jumbotron/loritta-v3-outline12px-blush")
    val lorittaJumbotronBlink = registerImagePreload("/loritta/loritta-jumbotron/loritta-v3-outline12px-blink")

    val lorittaRichHeathecliff = registerImagePreload("/loritta/loritta-rich-heathecliff")

    suspend fun loadImagesInfo() {
        val imagesInfo = etherealGambiClient.getImageInfo(*all.map { it.path }.toTypedArray())

        for (imageInfo in imagesInfo) {
            val preloadedImageInfo = all.first { it.path == imageInfo.key }
            preloadedImageInfo.imageInfo = imageInfo.value
            preloadedImageInfo.initialized = true
        }

        // Validate all imagesInfo
        val notInitializedImages = all.filter { !it.initialized }

        if (notInitializedImages.isNotEmpty()) {
            error("Images are missing from EtherealGambi! ${notInitializedImages.joinToString { it.path }}")
        }
    }

    private fun registerImagePreload(path: String): PreloadedImageInfo {
        return PreloadedImageInfo(path).also { all.add(it) }
    }

    class PreloadedImageInfo(
        val path: String
    ) {
        var initialized = false
        lateinit var imageInfo: ImageVariantsResponse
    }

    inner class ServerIcons {
        val zezao = registerImagePreload("/loritta/server-icons/zezao")
        val imkary = registerImagePreload("/loritta/server-icons/imkary")
        val animesk = registerImagePreload("/loritta/server-icons/animesk")
        val tocaDoCoelho = registerImagePreload("/loritta/server-icons/toca_do_coelho")
        val kamaitachi = registerImagePreload("/loritta/server-icons/kamaitachi")
        val detetiveYoutuber = registerImagePreload("/loritta/server-icons/detetive_youtuber")
        val eimine = registerImagePreload("/loritta/server-icons/eimine")
        val piratas = registerImagePreload("/loritta/server-icons/piratas")
        val servidorDaSeita = registerImagePreload("/loritta/server-icons/servidor_da_seita")
        val fdn = registerImagePreload("/loritta/server-icons/fdn")
        val ballerini = registerImagePreload("/loritta/server-icons/ballerini")
        val loucademiaDoPolicia = registerImagePreload("/loritta/server-icons/loucademia_do_policia")
        val cidcidoso = registerImagePreload("/loritta/server-icons/cidcidoso")
        val maiconKuster = registerImagePreload("/loritta/server-icons/maicon_kuster")
        val diogoDefante = registerImagePreload("/loritta/server-icons/diogo_defante")
        val dokebu = registerImagePreload("/loritta/server-icons/dokebu")
        val servidorDosMentecaptos = registerImagePreload("/loritta/server-icons/servidor_dos_mentecaptos")
        val rik = registerImagePreload("/loritta/server-icons/rik")
        val lubatv = registerImagePreload("/loritta/server-icons/lubatv")
        val gartic = registerImagePreload("/loritta/server-icons/gartic")

        val drawnMask = registerImagePreload("/loritta/server-icons/drawn_mask")
        val emiland = registerImagePreload("/loritta/server-icons/emiland")
        val cellbit = registerImagePreload("/loritta/server-icons/cellbit")
        val serverDoFelpinho = registerImagePreload("/loritta/server-icons/server_do_felpinho")
        val republicaCoisaDeNerd = registerImagePreload("/loritta/server-icons/republica_coisa_de_nerd")
        val forever = registerImagePreload("/loritta/server-icons/forever")
        val buuf = registerImagePreload("/loritta/server-icons/buuf")
        val chelly = registerImagePreload("/loritta/server-icons/chelly")
        val servidorDoOda = registerImagePreload("/loritta/server-icons/servidor_do_oda")
        val flakesPower = registerImagePreload("/loritta/server-icons/flakes_power")
        val redeDark = registerImagePreload("/loritta/server-icons/rede_dark")
        val polado = registerImagePreload("/loritta/server-icons/polado")
        val servidorDoBerg = registerImagePreload("/loritta/server-icons/servidor_do_berg")
        val colmeiaDoAbelha = registerImagePreload("/loritta/server-icons/colmeia_do_abelha")
        val kleberianus = registerImagePreload("/loritta/server-icons/kleberianus")
        val keio = registerImagePreload("/loritta/server-icons/keio")
        val serverUtopico = registerImagePreload("/loritta/server-icons/server_utopico")
    }

    inner class Commands {
        val amizade = registerImagePreload("/loritta/commands/rip_amizade")
        val ata = registerImagePreload("/loritta/commands/ata")
        val art = registerImagePreload("/loritta/commands/art")
        val asciiArt = registerImagePreload("/loritta/commands/asciiart")
        val atendente = registerImagePreload("/loritta/commands/atendente")
        val bobFire = registerImagePreload("/loritta/commands/bobfire")
        val bolsoDrake = registerImagePreload("/loritta/commands/bolsodrake")
        val bolsoFrame = registerImagePreload("/loritta/commands/bolsoframe")
        val bolsonaroTv = registerImagePreload("/loritta/commands/bolsonaro_tv")
        val bolsonaroTv2 = registerImagePreload("/loritta/commands/bolsonaro_tv2")
        val briggsCapa = registerImagePreload("/loritta/commands/briggs_capa")
        val buckShirt = registerImagePreload("/loritta/commands/buck_shirt")
        val canellaDvd = registerImagePreload("/loritta/commands/canella_dvd")
        val cepo = registerImagePreload("/loritta/commands/cepo")
        val chicoAta = registerImagePreload("/loritta/commands/chico_ata")
        val contentAwareScale = registerImagePreload("/loritta/commands/content_aware_scale")
        val demon = registerImagePreload("/loritta/commands/demon")
        val discordia = registerImagePreload("/loritta/commands/discordia")
        val drake = registerImagePreload("/loritta/commands/drake")
        val drawnWord = registerImagePreload("/loritta/commands/drawn_word")
        val drawnMaskPlaca = registerImagePreload("/loritta/commands/drawn_mask_placa")
        val deus = registerImagePreload("/loritta/commands/deus")
        val deuses = registerImagePreload("/loritta/commands/deuses")
        val dogeVida = registerImagePreload("/loritta/commands/doge_vida")
        val ednaldoBandeira = registerImagePreload("/loritta/commands/ednaldo_bandeira")
        val ednaldoTv = registerImagePreload("/loritta/commands/ednaldo_tv")
        val gangue = registerImagePreload("/loritta/commands/gangue")
        val getOverHere = registerImagePreload("/loritta/commands/getoverhere")
        val gessyAta = registerImagePreload("/loritta/commands/gessy_ata")
        val gumball = registerImagePreload("/loritta/commands/gumball")
        val inverter = registerImagePreload("/loritta/commands/invertido")
        val jooj = registerImagePreload("/loritta/commands/jooj")
        val knuxThrow = registerImagePreload("/loritta/commands/knuxthrow")
        val lava = registerImagePreload("/loritta/commands/lava")
        val lavaReverso = registerImagePreload("/loritta/commands/lavareverso")
        val loriAta = registerImagePreload("/loritta/commands/lori_ata")
        val loriSign = registerImagePreload("/loritta/commands/lori_sign")
        val maniaTitleCard = registerImagePreload("/loritta/commands/mania_title_card")
        val morrePraga = registerImagePreload("/loritta/commands/morre_praga")
        val passingPaper = registerImagePreload("/loritta/commands/passing_paper")
        val perdao = registerImagePreload("/loritta/commands/perdao")
        val perfeito = registerImagePreload("/loritta/commands/perfeito")
        val pepeDream = registerImagePreload("/loritta/commands/pepe_dream")
        val petPet = registerImagePreload("/loritta/commands/petpet")
        val primeirasPalavras = registerImagePreload("/loritta/commands/tirinha_baby")
        val ojjo = registerImagePreload("/loritta/commands/ojjo")
        val quadro = registerImagePreload("/loritta/commands/wolverine_frame")
        val razoes = registerImagePreload("/loritta/commands/reasons")
        val ripTv = registerImagePreload("/loritta/commands/rip_tv")
        val romeroBritto = registerImagePreload("/loritta/commands/romero_britto")
        val sam = registerImagePreload("/loritta/commands/south_america_memes")
        val susto = registerImagePreload("/loritta/commands/loritta_susto")
        val studiopolisTv = registerImagePreload("/loritta/commands/studiopolis_tv")
        val swing = registerImagePreload("/loritta/commands/swing")
        val terminator = registerImagePreload("/loritta/commands/terminator_anime")
        val triggered = registerImagePreload("/loritta/commands/triggered")
        val trump = registerImagePreload("/loritta/commands/trump")
        val toBeContinued = registerImagePreload("/loritta/commands/to_be_continued")
    }

    inner class Emotes {
        val loriSunglasses = registerImagePreload("/loritta/emotes/lori-sunglasses")
        val loriSob = registerImagePreload("/loritta/emotes/lori-sob")
        val loriKiss = registerImagePreload("/loritta/emotes/lori-kiss")
        val loriHm = registerImagePreload("/loritta/emotes/lori-hm")
        val loriBonk = registerImagePreload("/loritta/emotes/lori-bonk")
        val loriCard = registerImagePreload("/loritta/emotes/lori-card")
        val loriWhat = registerImagePreload("/loritta/emotes/lori-what")
        val loriZap = registerImagePreload("/loritta/emotes/lori-zap")
        val loriNemLigo = registerImagePreload("/loritta/emotes/lori-nem-ligo")
        val loriRage = registerImagePreload("/loritta/emotes/lori-rage")
        val loriClown = registerImagePreload("/loritta/emotes/lori-clown")
        // val loriLick = registerImagePreload("/loritta/emotes/lori-lick.gif")
        val loriHi = registerImagePreload("/loritta/emotes/lori-hi")
        // val loriPensandoMuito = registerImagePreload("/loritta/emotes/lori-pensando-muito.gif")
        val loriDemon = registerImagePreload("/loritta/emotes/lori-demon")
        val loriPeace = registerImagePreload("/loritta/emotes/lori-peace")
        val loriZz = registerImagePreload("/loritta/emotes/lori-zz")
        val loriSmart = registerImagePreload("/loritta/emotes/lori-smart")
        val loriAngel = registerImagePreload("/loritta/emotes/lori-angel")
        val loriMegaphone = registerImagePreload("/loritta/emotes/lori-megaphone")
        val loriTroll = registerImagePreload("/loritta/emotes/lori-troll")
        val loriIdiotSandwich = registerImagePreload("/loritta/emotes/lori-idiot-sandwich")
    }
}