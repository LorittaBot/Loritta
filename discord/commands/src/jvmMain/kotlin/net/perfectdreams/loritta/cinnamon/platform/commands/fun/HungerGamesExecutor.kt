package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import dev.kord.rest.Image
import dev.kord.rest.service.RestClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.HungerGamesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import org.jsoup.Jsoup

class HungerGamesExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val users = userList("user", HungerGamesCommand.I18N_PREFIX.Options.Users, 24, 24)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        context.deferChannelMessage()

        val users = args[Options.users]
        val guild = rest.guild.getGuild(context.guildId)

        val hungerGamesUrl = "https://brantsteele.net/hungergames"

        // First we load the "disclaimer" page to get the cookie
        val disclaimer = context.loritta.http.get("$hungerGamesUrl/disclaimer.php")

        // The PHPSESSID cookie seems to be always the last one (after the __cfuid cookie)
        // So we get the content after PHPSESSID= but before the ;, getting only the ID
        val phpSessId = disclaimer.headers.getAll("Set-Cookie")!!.joinToString(" ")
            .substringAfter("PHPSESSID=")
            .substringBefore(";")

        context.loritta.http.get("$hungerGamesUrl/agree.php") {
            header("Cookie", "PHPSESSID=$phpSessId")
        }

        val extension = if (guild.icon?.startsWith("a_") == true) "gif" else "png"
        val urlIcon = "https://cdn.discordapp.com/icons/${guild.id.value}/${guild.icon}.$extension?size=2048"

        context.loritta.http.submitFormWithBinaryData(
            "$hungerGamesUrl/personalize-24.php",
            formData {
                append("seasonname", guild.name)
                append("logourl", if (guild.icon != null) urlIcon else context.user.avatar.cdnUrl.toUrl {
                    this.size = Image.Size.Size128
                    this.format = Image.Format.PNG
                })
                append("existinglogo", "00")

                for ((index, user) in users.withIndex()) {
                    val numberWithPadding = (index + 1).toString().padStart(2, '0')

                    append("cusTribute$numberWithPadding", user.name)

                    append("cusTribute${numberWithPadding}img", user.avatar.cdnUrl.toUrl {
                        this.size = Image.Size.Size128
                        this.format = Image.Format.PNG
                    })

                    // TODO: Character Gender
                    // 0 = female
                    // 1 = male
                    /* val gender = profiles[user] ?: Gender.UNKNOWN
                    if (gender == Gender.FEMALE)
                        append("cusTribute${numberWithPadding}gender", "0")
                    else
                        append("cusTribute${numberWithPadding}gender", "1") */
                    append("cusTribute${numberWithPadding}gender", "0")

                    append("cusTribute${numberWithPadding}custom", "000")

                    append("cusTribute${numberWithPadding}nickname", user.name)

                    append("cusTribute${numberWithPadding}imgBW", "BW")
                }

                append("ChangeAll", "028")
            }
        ) {
            header("Cookie", "PHPSESSID=$phpSessId")
        }

        // Try going to the save page
        val result3 = context.loritta.http.get("$hungerGamesUrl/save.php") {
            header("Cookie", "PHPSESSID=$phpSessId")
        }

        // Get the season URL, it is inside of the #content element in a <a> tag
        val jsoup = Jsoup.parse(result3.bodyAsText())

        val saveLink = jsoup.getElementById("content")
            .getElementsByTag("a")
            .first()
            .attr("href")

        context.sendMessage {
            styled(
                context.i18nContext.get(HungerGamesCommand.I18N_PREFIX.SimulationCreated(saveLink)),
                Emotes.LoriHappyJumping
            )
        }
    }
}