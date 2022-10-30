package net.perfectdreams.loritta.morenitta.commands.vanilla.roblox

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class RbUserCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    listOf("rbuser", "rbplayer"),
    net.perfectdreams.loritta.common.commands.CommandCategory.ROBLOX
) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.rbuser"
        private val json = Json {
            ignoreUnknownKeys = true
        }

        // Example data: {"description":"####### ######## ####################################################### Brasil!","created":"2013-01-22T11:00:23.88Z","isBanned":false,"id":37271405,"name":"SonicteamPower","displayName":"SonicteamPower"}
        @Serializable
        data class RobloxUserResponse(
            val description: String,
            val created: String,
            val isBanned: Boolean,
            val id: Long,
            val name: String,
            val displayName: String
        )

        // [{"id":2,"name":"Friendship","description":"This badge is given to players who have embraced the Roblox community and have made at least 20 friends. People who have this badge are good people to know and can probably help you out if you are having trouble.","imageUrl":"https://images.rbxcdn.com/5eb20917cf530583e2641c0e1f7ba95e.png"},{"id":12,"name":"Veteran","description":"This badge recognizes members who have played Roblox for one year or more. They are stalwart community members who have stuck with us over countless releases, and have helped shape Roblox into the game that it is today. These medalists are the true steel, the core of the Robloxian history ... and its future.","imageUrl":"https://images.rbxcdn.com/b7e6cabb5a1600d813f5843f37181fa3.png"},{"id":6,"name":"Homestead","description":"The homestead badge is earned by having your personal place visited 100 times. Players who achieve this have demonstrated their ability to build cool things that other Robloxians were interested enough in to check out. Get a jump-start on earning this reward by inviting people to come visit your place.","imageUrl":"https://images.rbxcdn.com/b66bc601e2256546c5dd6188fce7a8d1.png"},{"id":7,"name":"Bricksmith","description":"The Bricksmith badge is earned by having a popular personal place. Once your place has been visited 1000 times, you will receive this award. Robloxians with Bricksmith badges are accomplished builders who were able to create a place that people wanted to explore a thousand times. They no doubt know a thing or two about putting bricks together.","imageUrl":"https://images.rbxcdn.com/49f3d30f5c16a1c25ea0f97ea8ef150e.png"},{"id":18,"name":"Welcome To The Club","description":"This badge is awarded to players who have ever belonged to the illustrious Builders Club. These players are part of a long tradition of Roblox greatness.","imageUrl":"https://images.rbxcdn.com/6c2a598114231066a386fa716ac099c4.png"}]
        @Serializable
        data class RobloxBadge(
            val id: Long,
            val name: String,
            val description: String,
            val imageUrl: String
        )
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            argument(ArgumentType.TEXT) {
                optional = false
            }
        }

        executesDiscord {
            val context = this

            if (context.args.isNotEmpty()) {
                OutdatedCommandUtils.sendOutdatedCommandMessage(
                    this,
                    locale,
                    "roblox user",
                    true
                )
            } else {
                context.explain()
            }
        }
    }
}
