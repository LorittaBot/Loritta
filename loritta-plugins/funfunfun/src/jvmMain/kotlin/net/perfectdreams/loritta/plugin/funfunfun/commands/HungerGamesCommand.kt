package net.perfectdreams.loritta.plugin.funfunfun.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.Gender
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.funfunfun.FunFunFunPlugin
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jsoup.Jsoup

class HungerGamesCommand(m: FunFunFunPlugin) : DiscordAbstractCommandBase(m.loritta as LorittaDiscord, listOf("hungergames", "jogosvorazes", "hg"), CommandCategory.FUN) {
    private val LOCALE_PREFIX = "commands.command.hungergames"
    private val WEBSITE_URL = "https://brantsteele.net"

    override fun command() = create {
        loritta as Loritta

        localizedDescription("$LOCALE_PREFIX.description")
        localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            repeat(24) {
                argument(ArgumentType.USER) {}
            }
        }

        canUseInPrivateChannel = false

        executesDiscord {
            val users = mutableListOf<User>()
            val copyOfTheGuildUserList = guild.members.map { it.user }
                    .toMutableList()

            for (index in 0 until 24) {
                users.add(user(index)?.toJDA() ?: continue)
            }

            // If there aren't sufficient users in the list, we are going to include some random users
            copyOfTheGuildUserList.removeAll(users)

            while (24 > users.size) {
                if (copyOfTheGuildUserList.isEmpty())
                    fail(locale["$LOCALE_PREFIX.doesntHaveEnoughUsers"])

                val randomUser = copyOfTheGuildUserList.random()
                users.add(randomUser)
                copyOfTheGuildUserList.remove(randomUser)
            }

            // First we load the "disclaimer" page to get the cookie
            val disclaimer = loritta.http.get<HttpResponse>("$WEBSITE_URL/hungergames/disclaimer.php")

            // The PHPSESSID cookie seems to be always the last one (after the __cfuid cookie)
            // So we get the content after PHPSESSID= but before the ;, getting only the ID
            val phpSessId = disclaimer.headers.getAll("Set-Cookie")!!.joinToString(" ")
                    .substringAfter("PHPSESSID=")
                    .substringBefore(";")

            // Then we send a request to the "Agree" page, indicating we are 13 years old
            loritta.http.get<HttpResponse>("$WEBSITE_URL/hungergames/agree.php") {
                header("Cookie", "PHPSESSID=$phpSessId")
            }

            // Create a map of the user -> gender
            // If no gender is specified, we default to UNKNOWN
            // But because the website only has male/female, we default to male when creating the list
            val profiles = users.map {
                it to loritta.newSuspendedTransaction { loritta.getLorittaProfile(it.idLong)?.settings?.gender ?: Gender.UNKNOWN }
            }.toMap()

            // Submit our custom Hunger Games to the page
            loritta.http.submitFormWithBinaryData<HttpResponse>(
                    "$WEBSITE_URL/hungergames/personalize-24.php",
                    formData {
                        // Season Name
                        append("seasonname", guild.name)
                        // The URL in the top right corner
                        append("logourl", guild.iconUrl ?: user.effectiveAvatarUrl)
                        // 00 (custom?)
                        append("existinglogo", "00")

                        for ((index, user) in users.withIndex()) {
                            val numberWithPadding = (index + 1).toString().padStart(2, '0')

                            // Character Name
                            append("cusTribute$numberWithPadding", user.name)
                            // Character Image
                            append("cusTribute${numberWithPadding}img", user.effectiveAvatarUrl)
                            // Character Gender
                            // 0 = female
                            // 1 = male
                            val gender = profiles[user] ?: Gender.UNKNOWN
                            if (gender == Gender.FEMALE)
                                append("cusTribute${numberWithPadding}gender", "0")
                            else
                                append("cusTribute${numberWithPadding}gender", "1")
                            // ???
                            append("cusTribute${numberWithPadding}custom", "000")
                            // Character Nickname
                            append("cusTribute${numberWithPadding}nickname", user.name)
                            // Character Image when Dead
                            append("cusTribute${numberWithPadding}imgBW", "BW")
                        }

                        // Unknown
                        append("ChangeAll", "028")
                    }
            ) {
                header("Cookie", "PHPSESSID=$phpSessId")
            }

            // Try going to the save page
            val result3 = loritta.http.get<HttpResponse>("$WEBSITE_URL/hungergames/save.php") {
                header("Cookie", "PHPSESSID=$phpSessId")
            }

            // Get the season URL, it is inside of the #content element in a <a> tag
            val jsoup = Jsoup.parse(result3.readText())

            val saveLink = jsoup.getElementById("content")
                    .getElementsByTag("a")
                    .first()
                    .attr("href")

            // Reply with the simulation URL, have fun!~
            reply(
                    LorittaReply(
                            locale["$LOCALE_PREFIX.simulationCreated", saveLink],
                            Emotes.LORI_HAPPY
                    )
            )
        }
    }
}
