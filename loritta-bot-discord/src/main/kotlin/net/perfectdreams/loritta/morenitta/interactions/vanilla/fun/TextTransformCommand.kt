package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.VaporwaveUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import kotlin.random.Random

class TextTransformCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Text
        val fullMatch = mapOf(
            "100" to listOf("💯"),
            "alface" to listOf("🥗"),
            "alvo" to listOf("🎯"),
            "amo" to listOf("😍", "😻", "😘", "😗", "😙", "😚", "💘	", "❤", "💓", "💕", "💖", "💖"),
            "amor" to listOf("😍", "😻", "😘", "😗", "😙", "😚", "💘	", "❤", "💓", "💕", "💖", "💖"),
            "ap" to listOf("🏢"),
            "ape" to listOf("🏢"),
            "apice" to listOf("🔝", "🏔", "⛰", "🗻"),
            "arma" to listOf("🔫", "🔪", "💣💥"),
            "avalanche" to listOf("🏔", "❄", "☃"),
            "banda" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
            "bandas" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
            "banheira" to listOf("🛁"),
            "banheiro" to listOf("🚽"),
            "banho" to listOf("🚿", "🛁", "🧖‍♂️", "🧖‍♀️"),
            "bar" to listOf("🍺", "🍻", "🥃", "🍾", "🤮"),
            "beber" to listOf("🍺", "🍻", "🥃", "🍾", "🤮"),
            "bem" to listOf("☺"),
            "boa" to listOf("🤙"),
            "bolsa" to listOf("👜", "👝"),
            "bravo" to listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
            "bumbum" to listOf("😮", "😏"),
            "carro" to listOf("🚐", "🚗"),
            "casa" to listOf("😋"),
            "casal" to listOf("💑"),
            "caso" to listOf("💑"),
            "celular" to listOf("📱"),
            "cerebro" to listOf("🧠", "💭"),
            "chama" to listOf("📞", "☎"),
            "chef" to listOf("👨‍🍳", "👩‍🍳"),
            "ciencia" to listOf("👩‍🔬", "👨‍🔬", "⚗", "🔬", "🔭", "📡"),
            "classe" to listOf("📚", "📘"),
            "consciencia" to listOf("🧠", "💭"),
            "coracao" to listOf("💘	", "❤", "💓", "💕", "💖", "💖"),
            "corra" to listOf("🏃"),
            "corre" to listOf("🏃"),
            "croissant" to listOf("🥐"),
            "dado" to listOf("🎲"),
            "data" to listOf("📅", "🗓"),
            "dinheiro" to listOf("💳", "💵", "💰", "💲"),
            "embuste" to listOf("😭", "🤢", "💥", "😘", "😜"),
            "escola" to listOf("👨‍🎓", "👩‍🎓", "📚", "📘", "🏫"),
            "faculdade" to listOf("👨‍🎓", "👩‍🎓", "📚", "📘"),
            "feio" to listOf("😛"),
            "feia" to listOf("😛"),
            "fora" to listOf("👉"),
            "fim" to listOf("🙅‍♂️", "🙅‍♀️"),
            "já" to listOf("⏰"),
            "internet" to listOf("🌐"),
            "madame" to listOf("🌹"),
            "marcial" to listOf("💪"),
            "marciais" to listOf("💪"),
            "mente" to listOf("🧠", "💭"),
            "moca" to listOf("🌹"),
            "mundo" to listOf("🌍"),
            "nada" to listOf("😮"),
            "nao" to listOf("⛔", "🚫", "🛑", "✋", "✋🛑", "⚠"),
            "oi" to listOf("😏", "😉"),
            "ok" to listOf("👌"),
            "papo" to listOf("💬"),
            "parabens" to listOf("🎈", "🎉", "🎊", "👏"),
            "pc" to listOf("💻", "🖥", "🖱⌨", "💾", "👨‍💻", "👩‍💻"),
            "planeta" to listOf("🌍"),
            "preco" to listOf("💳", "💵", "💰", "💲"),
            "princesa" to listOf("👸"),
            "principe" to listOf("🤴"),
            "quer" to listOf("😏"),
            "raio" to listOf("⚡"),
            "ri" to listOf("😅", "😂", "🤣"),
            "rir" to listOf("😅", "😂", "🤣"),
            "risada" to listOf("😅", "😂", "🤣"),
            "riso" to listOf("😅", "😂", "🤣"),
            "rola" to listOf("😒", "😝", "👉👌"),
            "sai" to listOf("🚫", "⛔"),
            "saliente" to listOf("😋"),
            "secreto" to listOf("🕵️‍"),
            "sera" to listOf("🤨", "🤔", "🧐"),
            "sexo" to listOf("😆", "👉👌"),
            "soco" to listOf("🥊"),
            "sono" to listOf("💤"),
            "sos" to listOf("🆘"),
            "susto" to listOf("😱", "🎃"),
            "terra" to listOf("🌍"),
            "tesao" to listOf("🌚"),
            "tiro" to listOf("🔫"),
            "tomar" to listOf("🍺", "🍻"),
            "topo" to listOf("🔝", "🏔", "⛰", "🗻"),
            "ve" to listOf("👀", "👁"),
            "vem" to listOf("🚐", "🏎"),
            "ver" to listOf("👀👀", "👀"),
            "voce" to listOf("👉"),
            "zumbi" to listOf("🧟‍♂️", "🧟‍♀️"),
            /* Abreviações/Girias */
            "aff" to listOf("🙄"),
            "bb" to listOf("👶", "😍", "😂", "😜", "💘"),
            "caraio" to listOf("😜", "😩", "😖", "☹", "😛", "😏", "😞"),
            "caralho" to listOf("😜", "😩", "😖", "☹", "😛", "😏", "😞"),
            "escroto" to listOf("👺", "👹", "👿"),
            "lol" to listOf("😅", "😂", "🤣"),
            "mozao" to listOf("💘", "❤", "💓", "💕", "💖", "💖"),
            "top" to listOf("😂👌", "👌", "🔝", "🤩"),
            "topper" to listOf("😂👌", "👌", "🔝", "🤩"),
            "topperson" to listOf("😂👌", "👌", "🔝", "😛", "🤩"),
            "uau" to listOf("😋"),
            "wow" to listOf("😋"),
            /* Comidas */
            "abacate" to listOf("🥑"),
            "amendoim" to listOf("🥜"),
            "bacon" to listOf("🥓"),
            "batata" to listOf("🍟", "🥔"),
            "berinjela" to listOf("🍆"),
            "biscoito" to listOf("🍪"),
            "bolacha" to listOf("🍪"),
            "brocolis" to listOf("🥦"),
            "castanha" to listOf("🌰"),
            "cenoura" to listOf("🥕"),
            "cerveja" to listOf("🍺", "🍻"),
            "cogumelo" to listOf("🍄"),
            "doce" to listOf("🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯"),
            "ovo" to listOf("🥚", "🍳"),
            "pepino" to listOf("🥒"),
            "pizza" to listOf("🍕"),
            "pretzel" to listOf("🥨"),
            "salada" to listOf("🥗"),
            "sanduiche" to listOf("🥪"),
            "sushi" to listOf("🍣", "🍙", "🍱", "🍘"),
            "trato" to listOf("🤝"),
            /* Empresas */
            "aliexpress" to listOf("🇨🇳"),
            "donalds" to listOf("🍔🍟"),
            "globo" to listOf("🌍"),
            "mcdonalds" to listOf("🍔🍟"),
            "sedex" to listOf("📦", "📬"),
            /* Esportes */
            "basquete" to listOf("🏀"),
            "futebol" to listOf("⚽"),
            "volei" to listOf("🏐"),
            /* Signos */
            "aries" to listOf("♈"),
            "touro" to listOf("♉"),
            "gemeos" to listOf("♊"),
            "cancer" to listOf("♋"),
            "leao" to listOf("♌"),
            "virgem" to listOf("♍"),
            "libra" to listOf("♎"),
            "escorpiao" to listOf("♏"),
            "sagitario" to listOf("♐"),
            "capricornio" to listOf("♑"),
            "aquario" to listOf("♒"),
            "peixes" to listOf("♓"),
            /* Personagens */
            "bolsonaro" to listOf("🚫🏳️‍🌈", "🔫"),
            "doria" to listOf("💩"),
            "lula" to listOf("💰", "🏢", "🦑"),
            "mario" to listOf("🍄"),
            "neymar" to listOf("😍"),
            "noel" to listOf("🎅"),
            "pabblo" to listOf("👩", "🏳️‍🌈👩"),
            "pabbllo" to listOf("👩", "🏳️‍🌈👩"),
            "pabllo" to listOf("👩", "🏳️‍🌈👩"),
            "temer" to listOf("🧛‍♂️", "🚫"),
            "vittar" to listOf("👩", "🏳️‍🌈👩")
        )
        val partialMatchAny = mapOf(
            "brasil" to listOf("🇧🇷"),
            "cabel" to listOf("💇‍♂️", "💇‍♀️"),
            "deus" to listOf("👼", "😇", "🙏", "🙏🙏"),
            "doid" to listOf("🤪"),
            "fuma" to listOf("🚬", "🚭"),
            "kk" to listOf("😅", "😂", "🤣"),
            "piment" to listOf("🌶"),
            "mort" to listOf("☠", "💀", "⚰", "👻"),
            "zap" to listOf("📞", "♣", "📱")
        )
        val partialMatchPrefix = mapOf(
            "abrac" to listOf("🤗"),
            "alema" to listOf("🇩🇪"),
            "alun" to listOf("👨‍🎓", "👩‍🎓"),
            "anjo" to listOf("😇"),
            "armad" to listOf("🔫", "🔪", "💣💥"),
            "arte" to listOf("🖌"),
            "assust" to listOf("😱", "🎃"),
            "ataq" to listOf("💣", "🔫"),
            "atenc" to listOf("👀"),
            "bunda" to listOf("😮", "😏"),
            "calad" to listOf("🤐"),
            "casad" to listOf("💏", "👩‍❤️‍💋‍👨", "👨‍❤️‍💋‍👨"),
            "chave" to listOf("🔑", "🗝"),
            "cheir" to listOf("👃"),
            "combat" to listOf("💣", "🔫", "🎖", "💪"),
            "computa" to listOf("💻", "🖥", "🖱⌨", "💾", "👨‍💻", "👩‍💻"),
            "comun" to listOf("🇷🇺"),
            "combin" to listOf("🤝"),
            "condec" to listOf("🎖"),
            "conhec" to listOf("🧠", "💭"),
            "content" to listOf("😀", "😁", "😃", "😄", "😊", "🙂", "☺"),
            "correr" to listOf("🏃"),
            "corrid" to listOf("🏃"),
            "danca" to listOf("💃", "🕺"),
            "dance" to listOf("💃", "🕺"),
            "desculpa" to listOf("😅"),
            "docei" to listOf("🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯"),
            "doen" to listOf("😷", "🤒", "🤕", "🤢", "🤮", "🤧"),
            "enjo" to listOf("🤢", "🤮"),
            "espia" to listOf("🕵️‍"),
            "espio" to listOf("🕵️‍"),
            "europ" to listOf("🇪🇺"),
            "exercito" to listOf("🎖"),
            "familia" to listOf("👨‍👩‍👧‍👦"),
            "feli" to listOf("😀", "😁", "😃", "😄", "😊", "🙂", "☺"),
            "fest" to listOf("🎆", "🎇", "✨", "🎈", "🎉", "🎊"),
            "flor" to listOf("🌹"),
            "foga" to listOf("🔥"),
            "fogo" to listOf("🔥"),
            "fogu" to listOf("🔥"),
            "gat" to listOf("😏", "👌", "😽", "😻"),
            "goz" to listOf("💦"),
            "gostos" to listOf("😈", "😜"),
            "guerr" to listOf("💣", "🔫", "🎖"),
            "hora" to listOf("⌚", "⏲", "🕛"),
            "hospita" to listOf("👨‍⚕️", "⚕", "🚑"),
            "imediat" to listOf("⌚", "⏳", "🕛"),
            "invest" to listOf("💳", "💵", "💰", "💲"),
            "justic" to listOf("⚖", "👨‍⚖️"),
            "louc" to listOf("🤪", "😩", "😢", "😰"),
            "louv" to listOf("👼", "😇", "🙏", "🙏🙏"),
            "mao" to listOf("🖐", "🖐"),
            "maneir" to listOf("🔝"),
            "mentir" to listOf("🤥", "🤫"),
            "militar" to listOf("🎖"),
            "miste" to listOf("🕵️‍"),
            "monitor" to listOf("🖥"),
            "morre" to listOf("☠", "💀", "⚰", "👻"),
            "morri" to listOf("☠", "💀", "⚰", "👻"),
            "musica" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
            "olh" to listOf("👀"),
            "ouv" to listOf("👂"),
            "palavr" to listOf("✏", "✒", "🖋", "📝", "💬"),
            "palhac" to listOf("🤡"),
            "palma" to listOf("👏"),
            "paulista" to listOf("🏳", "🌈"),
            "patet" to listOf("😣"),
            "patriot" to listOf("🇧🇷"),
            "pens" to listOf("🧠", "💭"),
            "pesa" to listOf("🏋"),
            "pipo" to listOf("🍿"),
            "pistol" to listOf("🔫"),
            "pula" to listOf("🏃"),
            "pule" to listOf("🏃"),
            "querid" to listOf("☺", "🤗"),
            "quiet" to listOf("🤐"),
            "raiv" to listOf("⚡", "😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
            "rock" to listOf("🤟"),
            "safad" to listOf("😉"),
            "saudade" to listOf("😢"),
            "segred" to listOf("🕵️‍"),
            "sumid" to listOf("😍"),
            "surpre" to listOf("😮"),
            "telefo" to listOf("📱", "📞", "☎"),
            "text" to listOf("✏", "✒", "🖋", "📝", "💬"),
            "transa" to listOf("👉👌"),
            "transe" to listOf("👉👌"),
            "trist" to listOf("☹", "🙁", "😖", "😞", "😟", "😢", "😭", "😭", "😭", "😩", "😿"),
            "vergonh" to listOf("😳"),
            "vist" to listOf("👀"),
            "whisk" to listOf("🥃"),
            /* Abreviações/Girias */
            "bucet" to listOf("😜", "😘", "😟"),
            "fod" to listOf("👉👌", "🔞"),
            "fud" to listOf("👉👌", "🔞"),
            "haha" to listOf("😅", "😂", "🤣"),
            "hehe" to listOf("😉", "😎", "😋", "😏", "😜", "😈", "🙊", "😼"),
            "mackenz" to listOf("🐴"),
            "merd" to listOf("💩"),
            "nude" to listOf("🙊", "😼", "😏"),
            "print" to listOf("📱"),
            "put" to listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
            "vampir" to listOf("🦇"),
            /* Animais */
            "cachorr" to listOf("🐶"),
            "morceg" to listOf("🦇"),
            /* Comidas */
            "hamburger" to listOf("🍔"),
            "hamburguer" to listOf("🍔"),
            "pao" to listOf("🍞", "🥖"),
            "panqueca" to listOf("🥞"),
            "milh" to listOf("🌽"),
            /* Profissões */
            "astronaut" to listOf("👨‍🚀", "👩‍🚀"),
            "bombeir" to listOf("👩‍🚒", "👨‍🚒"),
            "cienti" to listOf("👩‍🔬", "👨‍🔬", "⚗", "🔬", "🔭", "📡"),
            "cozinh" to listOf("👨‍🍳", "👩‍🍳"),
            "juiz" to listOf("👨‍⚖️", "👩‍⚖️", "⚖"),
            "medic" to listOf("👨‍⚕️", "👩‍⚕️", "⚕"),
            "pilot" to listOf("👨‍✈️", "👩‍✈️"),
            "policia" to listOf("🚨", "🚔", "🚓", "👮‍♂️", "👮‍♀️", "🔫"),
            "professor" to listOf("👨‍🏫", "👩‍🏫"),
            /* Signos */
            "arian" to listOf("♈"),
            "taurin" to listOf("♉"),
            "geminian" to listOf("♊"),
            "cancerian" to listOf("♋"),
            "leonin" to listOf("♌"),
            "virginian" to listOf("♍"),
            "librian" to listOf("♎"),
            "escorpian" to listOf("♏"),
            "sagitario" to listOf("♐"),
            "capricornian" to listOf("♑"),
            "aquarian" to listOf("♒"),
            "piscian" to listOf("♓")
        )
        val happyEmojis = listOf("😀", "😁", "😂", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "☺", "😛", "😜", "😝", "👌")
        val angryEmojis = listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠")
        val sadEmojis = listOf("☹", "🙁", "😖", "😞", "😟", "😢", "😭", "😭", "😭", "😩", "😿")
        val sassyEmojis = listOf("😉", "😎", "😋", "😘", "😏", "😜", "😈", "😻", "🙊", "👉👌", "😼")
        val sickEmojis = listOf("😷", "🤒", "🤕", "🤢", "🤮", "🤧")
        val VEMDEZAP_I18N_PREFIX = I18N_PREFIX.Vemdezap
        const val CLAP_EMOJI = "\uD83D\uDC4F"
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.FUN) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Vaporwave.Label, I18N_PREFIX.Vaporwave.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("vaporwave")
            }

            executor = TextVaporwaveExecutor()
        }

        subcommand(I18N_PREFIX.Uppercase.Label, I18N_PREFIX.Uppercase.Description) {
            executor = TextUppercaseExecutor()
        }

        subcommand(I18N_PREFIX.Lowercase.Label, I18N_PREFIX.Lowercase.Description) {
            executor = TextLowercaseExecutor()
        }

        subcommand(I18N_PREFIX.Clap.Label, I18N_PREFIX.Clap.Description(CLAP_EMOJI)) {
            executor = TextClapExecutor()
        }

        subcommand(I18N_PREFIX.Mock.Label, I18N_PREFIX.Mock.Description) {
            executor = TextMockExecutor()
        }

        subcommand(I18N_PREFIX.Quality.Label, I18N_PREFIX.Quality.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("qualidade")
            }

            executor = TextQualityExecutor()
        }

        subcommand(I18N_PREFIX.Vaporquality.Label, I18N_PREFIX.Vaporquality.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("vaporquality")
                add("vaporqualidade")
            }

            executor = TextVaporQualityExecutor()
        }

        subcommand(I18N_PREFIX.Vemdezap.Label, I18N_PREFIX.Vemdezap.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("vemdezap")
            }

            executor = TextVemDeZapExecutor()
        }
    }

    inner class TextVaporwaveExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Vaporwave.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            val vaporwave = VaporwaveUtils.vaporwave(text)

            context.reply(false) {
                styled(
                    vaporwave,
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextUppercaseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Uppercase.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            context.reply(false) {
                styled(
                    text.uppercase(),
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextLowercaseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Lowercase.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            context.reply(false) {
                styled(
                    text.lowercase(),
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextClapExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Clap.Options.Text(CLAP_EMOJI))
            val emoji = optionalString("emoji", I18N_PREFIX.Clap.Options.Emoji)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])
            val emoji = args[options.emoji] ?: CLAP_EMOJI

            context.reply(false) {
                styled(
                    "$emoji${text.split(" ").joinToString(emoji)}$emoji",
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextMockExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Mock.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            val random = Random(text.hashCode())

            val mockedText = text.mapIndexed { _, c -> if (random.nextBoolean()) c.uppercaseChar() else c.lowercaseChar() }
                .joinToString("")

            context.reply(false) {
                styled(
                    mockedText,
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextQualityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Quality.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            context.reply(false) {
                styled(
                    text.uppercase().toCharArray().joinToString(" "),
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextVaporQualityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Vaporquality.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])

            val vaporQuality = VaporwaveUtils.vaporwave(text.uppercase().toCharArray().joinToString(" "))

            context.reply(false) {
                styled(
                    vaporQuality,
                    "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val text = args.joinToString(" ")

            return mapOf(
                options.text to text
            )
        }
    }

    inner class TextVemDeZapExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", VEMDEZAP_I18N_PREFIX.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val moods = listOf(
                MoodWrapper(
                    context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Happy),
                    "\uD83D\uDE0A",
                    ZapZapMood.HAPPY
                ),
                MoodWrapper(
                    context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Angry),
                    "\uD83D\uDE20",
                    ZapZapMood.ANGRY
                ),
                MoodWrapper(
                    context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sassy),
                    "\uD83D\uDE0F",
                    ZapZapMood.SASSY
                ),
                MoodWrapper(
                    context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sad),
                    "\uD83D\uDE22",
                    ZapZapMood.SAD
                ),
                MoodWrapper(
                    context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sick),
                    "\uD83E\uDD12",
                    ZapZapMood.SICK
                )
            )

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        VEMDEZAP_I18N_PREFIX.Options.Mood.Text
                    )
                )

                actionRow(
                    buildList {
                        moods.forEach { mood ->
                            this.add(
                                context.loritta.interactivityManager
                                    .buttonForUser(context.user, ButtonStyle.PRIMARY, mood.label, {
                                        emoji = Emoji.fromUnicode(mood.unicode)
                                    }) { moodSelectionContext ->
                                        moodSelectionContext.deferAndEditOriginal {
                                            styled(
                                                moodSelectionContext.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Text)
                                            )

                                            actionRow(
                                                buildList {
                                                    for (i in 1..5) {
                                                        val levels = listOf(
                                                            context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level1),
                                                            context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level2),
                                                            context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level3),
                                                            context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level4),
                                                            context.i18nContext.get(VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level5),
                                                        )

                                                        this.add(
                                                            context.loritta.interactivityManager
                                                            .buttonForUser(context.user, ButtonStyle.PRIMARY, levels[i-1]) {
                                                                val text = cleanUpForOutput(context, args[options.text])
                                                                val split = text.split(" ")

                                                                var output = ""

                                                                for (word in split) {
                                                                    val lowerCaseWord = word.lowercase()
                                                                    output += "$word "
                                                                    var addedEmoji = false

                                                                    for ((match, emojis) in fullMatch) {
                                                                        if (lowerCaseWord == match) {
                                                                            output += "${emojis.random()} "
                                                                            addedEmoji = true
                                                                        }
                                                                    }

                                                                    for ((match, emojis) in partialMatchAny) {
                                                                        if (lowerCaseWord.contains(match, true)) {
                                                                            output += "${emojis.random()} "
                                                                            addedEmoji = true
                                                                        }
                                                                    }

                                                                    for ((match, emojis) in partialMatchPrefix) {
                                                                        if (lowerCaseWord.startsWith(match, true)) {
                                                                            output += "${emojis.random()} "
                                                                            addedEmoji = true
                                                                        }
                                                                    }

                                                                    if (!addedEmoji) {
                                                                        val upperBound = (5 - i) + 3
                                                                        val randomInteger = context.loritta.random.nextLong(upperBound.toLong())

                                                                        if (randomInteger == 0L) {
                                                                            val moodEmojis = when (mood.zapMood) {
                                                                                ZapZapMood.HAPPY -> happyEmojis
                                                                                ZapZapMood.ANGRY -> angryEmojis
                                                                                ZapZapMood.SASSY -> sassyEmojis
                                                                                ZapZapMood.SAD -> sadEmojis
                                                                                ZapZapMood.SICK -> sickEmojis
                                                                            }

                                                                            val addEmojis = context.loritta.random.nextLong(1, i.toLong() + 2)

                                                                            for (j in 0 until addEmojis) {
                                                                                output += "${moodEmojis.random()} "
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                it.deferAndEditOriginal {
                                                                    styled(
                                                                        output,
                                                                        "✍"
                                                                    )
                                                                    styled(
                                                                        it.i18nContext.get(VEMDEZAP_I18N_PREFIX.RefreshIt)
                                                                    )
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                }
                            )
                        }
                    }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    data class MoodWrapper(
        val label: String,
        val unicode: String,
        val zapMood: ZapZapMood
    )

    enum class ZapZapMood {
        HAPPY,
        ANGRY,
        SASSY,
        SAD,
        SICK
    }
}