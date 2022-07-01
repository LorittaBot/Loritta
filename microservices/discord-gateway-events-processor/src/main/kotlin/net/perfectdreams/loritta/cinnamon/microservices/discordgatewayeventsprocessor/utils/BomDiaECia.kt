package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import java.util.concurrent.ConcurrentHashMap

class BomDiaECia(private val m: DiscordGatewayEventsProcessor) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val randomTexts = mutableListOf<String>().apply {
            fun addWithVariations(source: String) {
                this.add(source)
                this.add("$source!")
            }

            // Gerar todas as mensagens possíveis do bom dia & cia
            // "alôoooo, cê tá me escutando?"
            for (i in 0..10) {
                var str = "alô"
                repeat(i) {
                    str += "o"
                }
                this.add("$str, cê tá me escutando?")
                this.add("$str, cê está me escutando?")
                this.add("$str, você tá me escutando?")
                this.add("$str, você está me escutando?")
            }

            val ilove = listOf(
                "o yudi",
                "a priscilla",
                "o yudi tamashiro",
                "a priscilla alcantara",
                "o sbt",
                "o bom dia & cia",
                "o bom dia e cia",
                "o bom dia & companhia",
                "o yudi e a priscilla",
                "o yudi tamashiro e a priscilla",
                "o yudi e a priscilla alcantara",
                "o yudi tamashiro e a priscilla alcantara"
            )

            for (person in ilove) {
                addWithVariations("eu gosto d$person")
                addWithVariations("eu amo $person")
                addWithVariations("eu adoro $person!")
                addWithVariations("eu idolatro $person")

                addWithVariations("gosto d$person")
                addWithVariations("amo $person")
                addWithVariations("adoro $person!")
                addWithVariations("idolatro $person")
            }

            val playstations = listOf(
                "playstation",
                "preisteicho",
                "preisteixo",
                "prêiesteicho",
                "prêisteixo",
                "playesteicho",
                "preíesteichu"
            )

            val numbers = listOf(
                "1",
                "2",
                "3",
                "4",
                "5",
                "um",
                "one",
                "dois",
                "doís",
                "two",
                "três",
                "treis",
                "three",
                "quatro",
                "quatru",
                "four",
                "cinco",
                "cincu",
                "five"
            )

            val otherStuff = listOf(
                "jogo da vida",
                "banco imobiliário",
                "tablet",
                "notebook",
                "laptop",
                "celular",
                "skate",
                "mp4",
                "mp3",
                "patins"
            )

            for (playstation in playstations) {
                addWithVariations("quero ganhar 1 $playstation")
                addWithVariations("quero ganhar um $playstation")
                addWithVariations("eu quero ganhar um $playstation")
                addWithVariations("eu quero ganhar 1 $playstation")
                addWithVariations(playstation)

                for (game in otherStuff) {
                    addWithVariations("não quero ganhar um $game, quero ganhar 1 $playstation")
                    addWithVariations("não quero ganhar 1 $game, quero ganhar 1 $playstation")
                    addWithVariations("não quero ganhar um $game, quero ganhar um $playstation")
                    addWithVariations("não quero ganhar 1 $game, quero ganhar um $playstation")
                    addWithVariations("não quero ganhar um $game, eu quero ganhar um $playstation")
                    addWithVariations("não quero ganhar 1 $game, eu quero ganhar um $playstation")
                    addWithVariations("não quero ganhar um $game, eu quero ganhar 1 $playstation")
                    addWithVariations("não quero ganhar 1 $game, eu quero ganhar 1 $playstation")

                    addWithVariations("quero ganhar 1 $playstation, e não quero ganhar um $game")
                    addWithVariations("quero ganhar 1 $playstation, e não quero ganhar 1 $game")
                    addWithVariations("quero ganhar um $playstation, e não quero ganhar um $game")
                    addWithVariations("quero ganhar um $playstation, e não quero ganhar 1 $game")
                    addWithVariations("eu quero ganhar um $playstation, e não quero ganhar um $game")
                    addWithVariations("eu quero ganhar um $playstation, e não quero ganhar 1 $game")
                    addWithVariations("eu quero ganhar 1 $playstation, não quero ganhar um $game")
                    addWithVariations("eu quero ganhar 1 $playstation, não quero ganhar 1 $game")
                }

                for (number in numbers) {
                    addWithVariations("$playstation $number")
                    addWithVariations("quero ganhar 1 $playstation $number")
                    addWithVariations("quero ganhar um $playstation $number")
                    addWithVariations("eu quero ganhar um $playstation $number")
                    addWithVariations("eu quero ganhar 1 $playstation $number")

                    for (game in otherStuff) {
                        addWithVariations("não quero ganhar um $game, quero ganhar 1 $playstation $number")
                        addWithVariations("não quero ganhar 1 $game, quero ganhar 1 $playstation $number")
                        addWithVariations("não quero ganhar um $game, quero ganhar um $playstation $number")
                        addWithVariations("não quero ganhar 1 $game, quero ganhar um $playstation $number")
                        addWithVariations("não quero ganhar um $game, eu quero ganhar um $playstation $number")
                        addWithVariations("não quero ganhar 1 $game, eu quero ganhar um $playstation $number")
                        addWithVariations("não quero ganhar um $game, eu quero ganhar 1 $playstation $number")
                        addWithVariations("não quero ganhar 1 $game, eu quero ganhar 1 $playstation $number")
                    }

                    addWithVariations("4002-8922 é o funk do yudi que vai dar $playstation $number")
                    addWithVariations("4002-8922 é o funk do japonês que vai te dar $playstation $number")
                }
            }

            this.add("bts? eu só conheço o sbt!")
            this.add("bts? eu só conheço o sbt do Silvio Santos!")
        }

        private val randomImages by lazy {
            listOf(
                "https://loritta.website/assets/img/bom-dia-cia.jpg",
                "https://loritta.website/assets/img/bom-dia-cia-2.jpg",
                "https://loritta.website/assets/img/bom-dia-cia-3.jpg",
                "https://loritta.website/assets/img/bom-dia-cia-4.jpg"
            )
        }

        private val obfuscationCharacters = listOf(
            '\u200B',
            '‍',
            '‌'
        )
    }

    val activeTextChannels = ConcurrentHashMap<Snowflake, YudiTextChannelInfo>()
    var triedToCall = mutableSetOf<Long>()
    var lastBomDiaECia = 0L
    var available = false
    var currentText = randomTexts[0]
    private var scope: CoroutineScope? = null

    fun startBomDiaECiaTask() {
        logger.info { "Starting Bom Dia & Cia Task..." }

        // Cancel all running tasks
        scope?.cancel()
        scope = CoroutineScope(SupervisorJob())

        scope?.launch {
            // Create a random delay between 15 minutes and 30 minutes
            val wait = m.random.nextLong(1_000, 10_000)
            val estimatedTime = wait + System.currentTimeMillis()
            logger.info { "We will wait ${wait}ms until the next Bom Dia & Cia!" }
            delay(wait)
            handleBomDiaECia()
        }
    }

    suspend fun handleBomDiaECia() {
        triedToCall.clear()

        logger.info { "Starting Bom Dia & Cia!" }

        val validTextChannels = getValidActiveTextChannelIds()

        available = true

        currentText = randomTexts.random()

        lastBomDiaECia = System.currentTimeMillis()

        val obfuscatedText = currentText.toCharArray()
            .joinToString("", transform = {
                val obfuscationCharacter = obfuscationCharacters.random()
                "$obfuscationCharacter$it"
            })

        val jobs = validTextChannels.map {
            scope!!.async {
                m.rest.channel.createMessage(
                    it
                ) {
                    content = "bd&c `$obfuscatedText`"
                }
            }
        }

        jobs.awaitAll()
        /* validTextChannels.forEach { textChannel ->
            // TODO: Localization!
            try {
                val embed = EmbedBuilder()
                embed.setTitle("<:sbt:447560158344904704> Bom Dia & Cia")
                embed.setDescription("Você aí de casa querendo prêmios agora, neste instante? Então ligue para o Bom Dia & Cia! Corra que apenas a primeira pessoa que ligar irá ganhar prêmios! (Cada tentativa de ligação custa **75 Sonhos**!) `${activeTextChannels[textChannel.id]?.prefix ?: "+"}ligar 4002-8922 $obfuscatedText`")
                embed.setImage(randomImages.random())
                embed.setColor(Color(74, 39, 138))

                textChannel.sendMessage(embed.build()).queue()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (DefaultExecutor.thread.isInterrupted)
            DefaultExecutor.thread.start() */

        // TODO: This should be restarted after someone wins the bd&c!
        // TODO: ^ This could be hard because the slash commands process is in a separate app, so for now let's just keep it like this lol no harm
        startBomDiaECiaTask()
    }

    /* @Synchronized
    fun announceWinner(channel: TextChannel, guild: Guild, user: User) {
        activeTextChannels.clear()

        val validTextChannels = this.validTextChannels
            ?: return // If there isn't any valid active channels, we don't need to announce the winner

        val messageForLocales = mutableMapOf<String, Message>()

        loritta.legacyLocales.forEach { localeId, locale ->
            val message = MessageBuilder().append("<:yudi:446394608256024597> **|** Parabéns `${user.name.stripCodeMarks().stripLinks()}#${user.discriminator}` (`${user.id}`) por ter ligado primeiro em `${guild.name.stripCodeMarks().stripLinks()}` (`${guild.id}`)!")

            messageForLocales[localeId] = message.build()
        }

        validTextChannels.forEachIndexed { index, textChannel ->
            // TODO: Localization!
            textChannel.sendMessage(messageForLocales["default"]!!)
                .queueAfterWithMessagePerSecondTarget(index)
        }

        GlobalScope.launch(loritta.coroutineDispatcher) {
            delay(30000)
            if (triedToCall.isNotEmpty()) {
                val pronoun = loritta.newSuspendedTransaction {
                    loritta.getOrCreateLorittaProfile(user.idLong).settings.gender.getPronoun(loritta.localeManager.getLocaleById("default"))
                }

                channel.sendMessage("<:yudi:446394608256024597> **|** Sabia que ${user.asMention} foi $pronoun primeir$pronoun de **${triedToCall.size} usuários** a conseguir ligar no Bom Dia & Cia? ${Emotes.LORI_OWO}").queue { message ->
                    if (message.guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION)) {
                        message.onReactionAddByAuthor(user.idLong) {
                            if (it.reactionEmote.isEmote("⁉")) {
                                loritta.messageInteractionCache.remove(it.messageIdLong)

                                val triedToCall = triedToCall.mapNotNull { lorittaShards.retrieveUserInfoById(it) }
                                channel.sendMessage("<:yudi:446394608256024597> **|** Pois é, ${triedToCall.joinToString(", ", transform = { "`" + it.name + "`" })} tentaram ligar... mas falharam!").queue()
                            }
                        }
                        message.addReaction("⁉").queue()
                    }
                }
            }
        }

        this.validTextChannels = null
    } */

    suspend fun getValidActiveTextChannelIds(): Set<Snowflake> {
        val validTextChannelIds = mutableSetOf<Snowflake>()

        activeTextChannels.forEach { (channelId, info) ->
            // We need to get the information from the database
            // TODO: Application ID is not *actually* Loritta's User ID! Some bots do not have the same app ID for the bot ID
            val permissions = m.getPermissions(info.guildId, channelId, Snowflake(m.config.discord.applicationId))

            logger.info { "Permissions for ${channelId}: $permissions" }

            if (permissions.contains(Permission.SendMessages) && permissions.contains(Permission.EmbedLinks)) {
                if (info.users.size >= 5 && info.lastMessageSent > (System.currentTimeMillis() - 180_000)) {
                    val serverConfig = m.services.serverConfigs.getServerConfigRoot(info.guildId.value)
                    val miscellaneousConfig = serverConfig?.getMiscellaneousConfig()
                    val enableBomDiaECia = miscellaneousConfig?.enableBomDiaECia ?: false

                    if (enableBomDiaECia)
                        validTextChannelIds.add(channelId)
                }
            }
        }

        return validTextChannelIds
    }

    class YudiTextChannelInfo(val guildId: Snowflake) {
        val users = ConcurrentHashMap.newKeySet<Snowflake>()
        var lastMessageSent = System.currentTimeMillis()
    }
}