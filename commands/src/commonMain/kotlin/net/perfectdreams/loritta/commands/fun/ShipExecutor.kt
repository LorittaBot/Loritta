package net.perfectdreams.loritta.commands.`fun`

import kotlinx.datetime.Clock
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.loritta.commands.`fun`.declarations.ShipCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.utils.InputConverter
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.executeAndHandleExceptions
import kotlin.math.absoluteValue
import kotlin.random.Random

class ShipExecutor(
    val emotes: Emotes,
    val inputConverter: InputConverter<String, ConverterResult>,
    val client: GabrielaImageServerClient
) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ShipExecutor::class) {
        object Options : CommandOptions() {
            val user1 = string("user1", ShipCommand.I18N_PREFIX.Options.User1)
                .register()

            val user2 = optionalString("user2", ShipCommand.I18N_PREFIX.Options.User2)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage()

        val user1 = args[options.user1]
        val user2 = args[options.user2]

        // We need to pass thru our input converter to convert user mentions into a user object, or keep it as a string
        val result1 = inputConverter.convert(context, user1)
        val result2 = if (user2 != null)
            inputConverter.convert(context, user2)
        else UserResult(context.user) // If the user2 is not present, we will use the user itself in the ship

        val user1Id: Long
        val user2Id: Long
        val user1Name: String
        val user2Name: String
        val user1AvatarUrl: String
        val user2AvatarUrl: String

        when (result1) {
            is StringResult -> {
                user1Id = result1.string.hashCode().toLong()
                user1Name = result1.string
                user1AvatarUrl = "https://cdn.discordapp.com/embed/avatars/0.png?size=256"
            }
            is UserResult -> {
                user1Id = result1.user.id
                user1Name = result1.user.name
                user1AvatarUrl = result1.user.avatar.url
            }
            is StringWithImageResult -> {
                user1Id = result1.string.hashCode().toLong()
                user1Name = result1.string
                user1AvatarUrl = result1.imageUrl
            }
        }

        when (result2) {
            is StringResult -> {
                user2Id = result2.string.hashCode().toLong()
                user2Name = result2.string
                user2AvatarUrl = "https://cdn.discordapp.com/embed/avatars/0.png?size=256"
            }
            is UserResult -> {
                user2Id = result2.user.id
                user2Name = result2.user.name
                user2AvatarUrl = result2.user.avatar.url
            }
            is StringWithImageResult -> {
                user2Id = result2.string.hashCode().toLong()
                user2Name = result2.string
                user2AvatarUrl = result2.imageUrl
            }
        }

        // Now we will calculate the user's ship value, for that we will sum the user's IDs
        // The order of the sum doesn't change its result, so we don't need to sort it
        val seed = user1Id + user2Id

        // And now we will do a % to get a value between 0 and 100
        var value = (seed % 101).absoluteValue.toInt()

        var isMarried = false
        var isLoveYourself = false

        // "Loritta will be cancelled on Twitter due to result manipulation, oh no!"
        if (user1Id == user2Id) {
            // Easter Egg: Love Yourself
            value = 100
            isLoveYourself = true
        } else if (result1 is UserResult && result2 is UserResult) {
            // We will only check for manipulated values if both users are a UserResult
            // Because we don't need to spend requests by checking if an StringResult has a marriage
            val user1Marriage = context.loritta.services.marriages.getMarriageByUser(user1Id)
            val user2Marriage = context.loritta.services.marriages.getMarriageByUser(user2Id)

            // If both users are married, and the marriage ID of both are the same, then it means that they are married together, how cute!
            if (user1Marriage != null && user2Marriage != null && user1Marriage.id == user2Marriage.id) {
                value = 100 // And also set the value to 100% if they are married
                isMarried = true
            }

            // However ship effects can override the married percentage!
            val shipEffects = context.loritta.services.shipEffects.getShipEffectsForUser(user1Id) + context.loritta.services.shipEffects.getShipEffectsForUser(user2Id)

            // TODO: Add RPC call to only get valid (non expired) ship effects
            // TODO: Add RPC call to do what we are doing here in the .firstOrNull { ... } call
            val firstMatchedShipEffect = shipEffects
                .filter { it.expiresAt > Clock.System.now() }
                .sortedByDescending { it.id }
                .firstOrNull { (it.user1 == user1Id && it.user2 == user2Id) || (it.user2 == user1Id && it.user1 == user2Id) }

            if (firstMatchedShipEffect != null)
                value = firstMatchedShipEffect.editedShipValue
        }

        val loveTextResults: ListI18nData
        val loveTextEmote: Emote

        when {
            isLoveYourself -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreLoveYourself
                loveTextEmote = emotes.loriSmile
            }
            isMarried -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreMarried
                loveTextEmote = emotes.marriageRing
            }
            value == 100 -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScorePerfect
                loveTextEmote = emotes.sparklingHeart
            }
            value in 67..99 -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreLove
                loveTextEmote = emotes.loriHeart
            }
            value in 34..66 -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreShrug
                loveTextEmote = emotes.loriShrug
            }
            value in 1..33 -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreSob
                loveTextEmote = emotes.loriSob
            }
            value == 0 -> {
                loveTextResults = ShipCommand.I18N_PREFIX.ScoreImpossible
                loveTextEmote = emotes.loriHmpf
            }
            else -> error("Percentage is out of range")
        }

        // We will create a random with the user's seed, so the text answer will always be the same
        val loveTextResult = context.i18nContext.get(loveTextResults).random(Random(seed))

        // We will substring half of the name of both of user's, so we can create a nice "ship name"
        val shipName = if (isLoveYourself) {
            // Easter Egg: Love Yourself
            user1Name
        } else {
            val name1 = user1Name.substring(0..(user1Name.length / 2))
            val name2 = user2Name.substring(user2Name.length / 2 until user2Name.length)
            name1 + name2
        }

        val result = client.executeAndHandleExceptions(
            context,
            emotes,
            "/api/v1/images/ship",
            buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", user1AvatarUrl)
                    }
                    addJsonObject {
                        put("type", "url")
                        put("content", user2AvatarUrl)
                    }
                }

                put("percentage", value)
            }
        )

        context.sendMessage {
            content = """${emotes.loriHeartCombo1}${emotes.loriHeartCombo2} **${context.i18nContext.get(ShipCommand.I18N_PREFIX.NewCouple)}** ${emotes.loriHeartCombo1}${emotes.pantufaHeartCombo2}
                |${emotes.loriReading} `$user1Name` + `$user2Name` = ${emotes.sparkles} **`$shipName`** ${emotes.sparkles}
                |$loveTextEmote $loveTextResult $loveTextEmote
            """.trimMargin()

            addFile("ship.png", result)
        }
    }

    sealed class ConverterResult
    class UserResult(val user: User) : ConverterResult()
    class StringResult(val string: String) : ConverterResult()
    class StringWithImageResult(val string: String, val imageUrl: String) : ConverterResult()
}