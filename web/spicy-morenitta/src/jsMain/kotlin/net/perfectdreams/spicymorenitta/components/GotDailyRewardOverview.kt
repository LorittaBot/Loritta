
package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import kotlinx.coroutines.delay
import net.perfectdreams.loritta.common.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.serializable.responses.GetDailyRewardResponse
import net.perfectdreams.spicymorenitta.i18nContext
import net.perfectdreams.spicymorenitta.routes.DailyScreen
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

@Composable
fun GotDailyRewardOverview(
    screen: DailyScreen.GotDailyRewardScreen
) {
    Div(
        attrs = {
            classes("daily-overview")
        }
    ) {
        val question = screen.response.question
        val hasCorrectAnswer = screen.response.dailyPayoutBonuses.filterIsInstance<GetDailyRewardResponse.Success.DailyPayoutBonus.DailyQuestionBonus>().isNotEmpty()

        Div(attrs = {
            classes("daily-reward-question-result")
        }) {
            Div(attrs = {
                classes("status")
            }) {
                if (hasCorrectAnswer) {
                    Text("Parabéns, você acertou! Hoje você ganhou...")
                } else {
                    Text("Que pena, você errou...")
                }
            }

            if (question != null && !hasCorrectAnswer) {
                Div {
                    Text(i18nContext.get(question.incorrectExplanation))
                }

                Div(attrs = {
                    classes("status")
                }) {
                    Text("Hoje você ganhou...")
                }
            }
        }

        Div(
            attrs = {
                classes("sonhos-reward-wrapper")
            }
        ) {
            Img(src = "https://stuff.loritta.website/sonhos/bundle-45b3b35d@320w.png") {
                this.attr("width", "200")
            }

            Div(
                attrs = {
                    classes("sonhos-data")
                }
            ) {
                Div(
                    attrs = {
                        classes("sonhos-quantity")
                    }
                ) {
                    Span(
                        attrs = {
                            ref { htmlDivElement ->
                                val job = screen.launch {
                                    var i = 0

                                    // Calculating how much time we want to take is hard, since delay(...) only works with milliseconds
                                    // (If you use a duration, it converts to a milliseconds)
                                    // So what we will do is use a double
                                    var storedTime = 0.0
                                    // And then the time will be based off this
                                    val delayBetweenTicks = (5.seconds.inWholeMilliseconds / screen.response.dailyPayoutWithoutAnyBonus.toDouble())
                                    val delayBatch = 20.0

                                    // Calling delay seems to actually incur a ~16ms delay penalty
                                    // To avoid this, we will "batch" into batches instead
                                    // println("Delay Between Ticks: $delayBetweenTicks - Payout: ${screen.response.dailyPayout}")
                                    while (i != screen.response.dailyPayoutWithoutAnyBonus) {
                                        i++

                                        // Now, to "simulate" that we are waiting 5s, we will add it to the storedTime
                                        // If the time is >= 1.0, then we will wait 1ms and THEN decrease from the storedTime
                                        // So, if delayBetweenTicks is 0.5, it will take 2 loops before we wait
                                        storedTime += delayBetweenTicks
                                        if (storedTime >= delayBatch) {
                                            // We change the current here because there is no point in changing it outside the loop, since it would be
                                            // "instantaneous"

                                            htmlDivElement.innerText =
                                                (screen.response.dailyPayoutWithoutAnyBonus * sin(((i / screen.response.dailyPayoutWithoutAnyBonus.toDouble()) * PI) / 2)).toInt()
                                                    .toString()

                                            while (storedTime >= delayBatch) {
                                                delay(delayBatch.toLong())
                                                storedTime -= delayBatch
                                            }
                                        }
                                    }

                                    htmlDivElement.innerText = screen.response.dailyPayoutWithoutAnyBonus.toString()
                                    screen.cash.play()
                                }

                                onDispose {
                                    job.cancel()
                                }
                            }
                        }
                    ) {}

                    Text(" sonhos!")
                }

                for (bonus in screen.response.dailyPayoutBonuses) {
                    Div(
                        attrs = {
                            classes("bonus-quantity")
                        }
                    ) {
                        when (bonus) {
                            is GetDailyRewardResponse.Success.DailyPayoutBonus.DailyQuestionBonus -> Text("+ ${bonus.quantity} pelo acerto")
                        }
                    }
                }
            }
        }

        Div {
            Text("Siga a Loritta nas Redes Sociais!")
        }

        Div(
            attrs = {
                attr("style", "display: flex; justify-content: center; flex-wrap: wrap; gap: 0.5em;")
            }
        ) {
            A(href = "https://bsky.app/profile/loritta.website", attrs = {
                classes("button", "primary")
                target(ATarget.Blank)
                attr("style", "justify-content: center; align-items: center; display: flex;")
            }) {
                I(attrs = { classes("fab", "fa-bluesky") })
            }

            A(href = "https://www.youtube.com/c/Loritta", attrs = {
                classes("button", "red")
                target(ATarget.Blank)
                attr("style", "justify-content: center; align-items: center; display: flex;")
            }) {
                I(attrs = { classes("fab", "fa-youtube") })
            }

            A(href = "https://www.instagram.com/lorittabot/", attrs = {
                classes("button", "pink")
                target(ATarget.Blank)
                attr("style", "justify-content: center; align-items: center; display: flex;")
            }) {
                I(attrs = { classes("fab", "fa-instagram") })
            }

            A(href = "https://tiktok.com/@lorittamorenittabot", attrs = {
                classes("button", "black")
                target(ATarget.Blank)
                attr("style", "justify-content: center; align-items: center; display: flex;")
            }) {
                I(attrs = { classes("fab", "fa-tiktok") })
            }

            A(href = "https://www.twitch.tv/lorittamorenitta", attrs = {
                classes("button", "purple")
                target(ATarget.Blank)
                attr("style", "justify-content: center; align-items: center; display: flex;")
            }) {
                I(attrs = { classes("fab", "fa-twitch") })
            }
        }

        // If there's a twitch channel set, we will display an iframe
        val twitchChannelToAdvertise = screen.response.twitchChannelToAdvertise
        if (twitchChannelToAdvertise != null) {
            Div(
                attrs = {
                    attr("style", "display: flex; justify-content: center; flex-wrap: wrap; gap: 0.5em;")
                }
            ) {
                Iframe(
                    {
                        attr("src", "https://player.twitch.tv/?channel=${twitchChannelToAdvertise.channelId}&parent=${window.location.host}&muted=false")
                        attr("width", "1280")
                        attr("height", "720")
                        attr("allowfullscreen", "")
                        attr("frameborder", "")

                        style {
                            width(100.percent)
                            height(auto)
                            property("aspect-ratio", "16/9")
                        }
                    }
                )
            }
        }

        Div {
            val sponsoredBy = screen.response.sponsoredBy
            if (sponsoredBy != null) {
                H1 {
                    Text("Você ganhou ${sponsoredBy.sonhosMultipliedBy}x mais sonhos, graças ao...")
                }

                val iconId = sponsoredBy.sponsoredByGuild.iconId

                if (iconId != null) {
                    val guildIconUrl =
                        "https://cdn.discordapp.com/icons/${sponsoredBy.sponsoredByGuild.id}/$iconId" +
                                if (iconId.startsWith("a_"))
                                    ".gif"
                                else
                                    ".png"

                    Img(src = guildIconUrl) {
                        attr("width", "128")
                        attr("height", "128")
                        attr("style", "border-radius: 99999px")
                    }
                }

                H2 {
                    Text(sponsoredBy.sponsoredByGuild.name)
                }

                P {
                    Text("Você iria ganhar ${sponsoredBy.originalPayout}, mas graças ao patrocínio do ${sponsoredBy.sponsoredByGuild.name} você ganhou ${screen.response.dailyPayoutWithoutAnyBonus} sonhos!")
                }

                val sponsoredUser = sponsoredBy.sponsoredByUser
                if (sponsoredUser != null) {
                    P {
                        Text("Agradeça ${sponsoredUser.name}#${sponsoredUser.discriminator} por ter feito você ganhar ${screen.response.dailyPayoutWithoutAnyBonus - sponsoredBy.originalPayout} mais sonhos que o normal!")
                    }
                }
            }

            P {
                Text("Agora você possui ${screen.response.currentBalance} sonhos, que tal gastar seus sonhos jogando no SparklyPower, o servidor de Minecraft da Loritta? (mc.sparklypower.net)")
            }

            if (screen.response.failedGuilds.isNotEmpty()) {
                for (failedGuild in screen.response.failedGuilds) {
                    P {
                        Text("Você poderia ganhar x${failedGuild.multiplier} sonhos ")
                        when (failedGuild.type) {
                            DailyGuildMissingRequirement.REQUIRES_MORE_TIME -> {
                                Text("após ficar por mais de 15 dias em ")
                            }

                            DailyGuildMissingRequirement.REQUIRES_MORE_XP -> {
                                Text("se você conseguir ser mais ativo ao ponto de ter 500 XP em ")
                            }
                        }

                        Text(failedGuild.guild.name)
                        Text("!")
                    }
                }
            }

            Img(src = "https://stuff.loritta.website/loritta-happy.gif", attrs = {
                classes("come-back-image")
            })

            P {
                Text("Volte sempre!")
            }
        }
    }
}