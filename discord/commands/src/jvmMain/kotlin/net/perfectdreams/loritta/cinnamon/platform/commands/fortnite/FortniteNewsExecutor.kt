package net.perfectdreams.loritta.cinnamon.platform.commands.fortnite

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.neotilted.client.NeoTiltedClient
import net.perfectdreams.neotilted.data.FortniteBattleRoyaleNewsResponse

class FortniteNewsExecutor(val neoTiltedClient: NeoTiltedClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(FortniteNewsExecutor::class) {
        fun createMessage(
            user: User,
            newsData: FortniteBattleRoyaleNewsResponse.NewsData,
            currentPage: Int
        ) : MessageBuilder.() -> (Unit) {
            return {
                val currentMotd = newsData.motds[currentPage]

                embed {
                    title = "${Emotes.DefaultDance} ${currentMotd.title}"
                    description = currentMotd.body
                    image = currentMotd.image
                    color = Color(0, 125, 187)
                }

                val encodedComponent = ComponentDataUtils.encode(
                    ChangeFortniteBattleRoyaleNewsPageData(
                        user.id,
                        newsData.hash
                    )
                )

                actionRow {
                    selectMenu(
                        ChangeFortniteBattleRoyaleNewsPageExecutor,
                        encodedComponent
                    ) {
                        for ((pageId, motd) in newsData.motds.withIndex()) {
                            this.option(motd.title, pageId.toString()) {
                                loriEmoji = Emotes.DefaultDance

                                if (motd == currentMotd) {
                                    default = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage()

        // TODO: language
        val r = neoTiltedClient.getFortniteBattleRoyaleNews("pt-BR")

        context.sendMessage(createMessage(context.user, r, 0))
    }
}