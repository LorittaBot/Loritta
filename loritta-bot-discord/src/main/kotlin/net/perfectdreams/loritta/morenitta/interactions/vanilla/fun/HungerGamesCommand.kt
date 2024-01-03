package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import org.jsoup.Jsoup

class HungerGamesCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Hungergames
        private const val LOCALE_PREFIX = "commands.command.hungergames"
        private const val WEBSITE_URL = "https://brantsteele.net"
        private const val REQUIRED_USERS = 24
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        this.alternativeLegacyLabels.apply {
            add("jogosvorazes")
            add("hg")
        }

        executor = HungerGamesExecutor()
    }

    inner class HungerGamesExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            init {
                repeat(REQUIRED_USERS) {
                    // TODO: Implement a proper "userList" like how we had in Discord InteraKTions
                    optionalUser("user${it + 1}", I18N_PREFIX.Options.Users)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guildOrNull
            if (guild == null) {
                context.reply(true) {
                    content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
                }
                return
            }
            context.deferChannelMessage(false)

            // This is kinda hacky
            val mentionedUsers = options.registeredOptions.map { args[it] }.filterNotNull() as List<UserAndMember>

            val users = mentionedUsers.map { it.user }.toMutableList()

            if (users.size != REQUIRED_USERS) {
                val copyOfTheGuildUserList = guild.members.map { it.user }
                    .toMutableList()

                // If there aren't sufficient users in the list, we are going to include some random users
                copyOfTheGuildUserList.removeAll(users)

                while (REQUIRED_USERS > users.size) {
                    if (copyOfTheGuildUserList.isEmpty()) {
                        context.fail(false, context.locale["$LOCALE_PREFIX.doesntHaveEnoughUsers"])
                    }

                    val randomUser = copyOfTheGuildUserList.random()
                    users.add(randomUser)
                    copyOfTheGuildUserList.remove(randomUser)
                }
            }

            // First we load the "disclaimer" page to get the cookie
            val disclaimer = loritta.http.get("$WEBSITE_URL/hungergames/disclaimer.php")

            // The PHPSESSID cookie seems to be always the last one (after the __cfuid cookie)
            // So we get the content after PHPSESSID= but before the ;, getting only the ID
            val phpSessId = disclaimer.headers.getAll("Set-Cookie")!!.joinToString(" ")
                .substringAfter("PHPSESSID=")
                .substringBefore(";")

            // Then we send a request to the "Agree" page, indicating we are 13 years old
            loritta.http.get("$WEBSITE_URL/hungergames/agree.php") {
                header("Cookie", "PHPSESSID=$phpSessId")
            }

            // Create a map of the user -> gender
            // If no gender is specified, we default to UNKNOWN
            // But because the website only has male/female, we default to male when creating the list
            val profiles = users.map {
                it to loritta.newSuspendedTransaction { loritta.getLorittaProfile(it.idLong)?.settings?.gender ?: Gender.UNKNOWN }
            }.toMap()

            // Submit our custom Hunger Games to the page
            loritta.http.submitFormWithBinaryData(
                "$WEBSITE_URL/hungergames/personalize-24.php",
                formData {
                    // Season Name
                    append("seasonname", guild.name)
                    // The URL in the top right corner
                    append("logourl", guild.iconUrl ?: context.user.effectiveAvatarUrl)
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
            val result3 = loritta.http.get("$WEBSITE_URL/hungergames/save.php") {
                header("Cookie", "PHPSESSID=$phpSessId")
            }

            // Get the season URL, it is inside of the #content element in a <a> tag
            val jsoup = Jsoup.parse(result3.bodyAsText())

            val saveLink = jsoup.getElementById("content")!!
                .getElementsByTag("a")
                .first()!!
                .attr("href")

            // Reply with the simulation URL, have fun!~
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.SimulationCreated(saveLink)),
                    Emotes.LoriHappyJumping
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val map = mutableMapOf<OptionReference<*>, UserAndMember?>()

            repeat(REQUIRED_USERS) {
                val userAndMember = context.getUserAndMember(it)
                map[options.registeredOptions.get(it)] = userAndMember
            }

            return map
        }
    }
}