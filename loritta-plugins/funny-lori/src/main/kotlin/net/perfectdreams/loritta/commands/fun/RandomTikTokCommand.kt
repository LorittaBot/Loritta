package net.perfectdreams.loritta.commands.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import java.awt.Color
import java.net.URL

class RandomTikTokCommand : LorittaCommand(arrayOf("randomtiktok", "rtiktok"), category = CommandCategory.FUN) {
    companion object {
        private var lastRequest = 0L
        private var cachedTikToks = listOf<JsonElement>()
        private val hashtags = listOf(
                "foryou" to 42164,
                "destaque" to 2120860,
                "funny" to 5424,
                "duet" to 30496,
                "meme" to 23864
        )
        private val logger = KotlinLogging.logger {}
    }

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.fun.randomtiktok.description"]
    }

    override val needsToUploadFiles: Boolean
        get() = true

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (System.currentTimeMillis() >= (lastRequest + 900_000)) { // 15 minutes
            val newTikToks = mutableListOf<JsonElement>()

            for (hashtag in hashtags) {
                // Carregar TikToks é... complicado
                // O TikTok requer uma "signature" como um parâmetro GET, mas o que é isso?
                // Não sei, e o codigo é tão obfuscado que é impossível de entender.
                //
                // O signature, ao mudar, funciona "as vezes", por isso é realizado um brute force até encontrar a signature certa.
                // As vezes alterar os valores funcionam sem problemas, as vezes funciona após repetir o request de novo (wtf?), por isso
                // o request é realizado três vezes, caso não encontre em até 3 requests, rip.
                var loaded = false
                for (x in 0 until 3) {
                    var randomChar1 = (RANDOM.nextInt(26) + 'a'.toInt()).toChar().toString()
                    var randomChar2 = (RANDOM.nextInt(26) + 'a'.toInt()).toChar().toString()
                    var randomChar3 = (RANDOM.nextInt(26) + 'a'.toInt()).toChar().toString()

                    if (RANDOM.nextBoolean())
                        randomChar1 = randomChar1.toUpperCase()
                    if (RANDOM.nextBoolean())
                        randomChar2 = randomChar2.toUpperCase()
                    if (RANDOM.nextBoolean())
                        randomChar3 = randomChar3.toUpperCase()

                    val payload = HttpRequest.get("https://www.tiktok.com/share/item/list?secUid=&id=${hashtag.second}&type=3&count=48&minCursor=-1&maxCursor=0&shareUid=&_signature=jLAmZgAgEBPR061s-5Up7oywZ3AA${randomChar1}${randomChar2}${randomChar3}")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
                            .header("Referer", "https://www.tiktok.com/tag/${hashtag.first}?langCountry=en")
                            .body()

                    val json = jsonParser.parse(payload).obj

                    if (!json.has("body")) {
                        logger.warn { "Hashtag ${hashtag} failed to load, number of tries: ${x + 1}" }
                        continue
                    }

                    newTikToks.addAll(json["body"]["itemListData"].array.toList())
                    loaded = true
                    break
                }

                if (!loaded) {
                    logger.warn { "Hashtag $hashtag failed to load after three tries!" }
                } else {
                    logger.info { "Hashtag $hashtag loaded successfully!" }
                }
            }

            logger.info { "${newTikToks.size} TikToks were loaded" }
            cachedTikToks = newTikToks.filter {
                15 >= it["itemInfos"]["video"]["videoMeta"]["duration"].int // Apenas pegar TikToks não tão grandes, para evitar envios de arquivos gigantes no chat
            }

            lastRequest = System.currentTimeMillis()
        }

        if (cachedTikToks.isEmpty()) {
            context.reply(
                    locale["commands.fun.randomtiktok.noTikTokFound"],
                    Constants.ERROR
            )
            return
        }

        val item = cachedTikToks.random()

        val embed = EmbedBuilder()

        val itemInfo = item["itemInfos"].obj
        val authorInfo = item["authorInfos"].obj
        val musicInfo = item["musicInfos"].obj

        embed.setTitle("<:tiktok:637249082791952404> TikTok")
        embed.setColor(Color.BLACK)
        embed.setAuthor(authorInfo["nickName"].string, "https://www.tiktok.com/@${authorInfo["uniqueId"].string}", authorInfo["covers"].array.first().string)
        embed.setDescription(itemInfo["text"].string)
        embed.setFooter("\uD83C\uDFB5 ${musicInfo["musicName"].string} - ${musicInfo["authorName"].string}")

        val video = URL(itemInfo["video"]["urls"].array.first().string).openConnection().getInputStream().readAllBytes()

        context.sendFile(video.inputStream(), "tiktok.mp4", context.getAsMention(true), embed.build())
    }
}