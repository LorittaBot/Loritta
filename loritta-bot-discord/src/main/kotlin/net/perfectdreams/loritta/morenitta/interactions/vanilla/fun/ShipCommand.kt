package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import kotlinx.datetime.Clock
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.gabrielaimageserver.data.ShipRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.*
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordRegexes
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.serializable.UserId
import kotlin.math.absoluteValue
import kotlin.random.Random

class ShipCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val inputConverter = ShipDiscordMentionInputConverter()
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Ship
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        examples = I18N_PREFIX.Examples

        enableLegacyMessageSupport = true

        this.alternativeLegacyLabels.apply {
            add("shippar")
        }

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = ShipExecutor()
    }

    inner class ShipExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user1 = string("user1", I18N_PREFIX.Options.User1)
            val user2 = optionalString("user2", I18N_PREFIX.Options.User2)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

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

            // If the user that executed the command is in any of the ships, then this will be true
            // Used for achievements
            var isShipWithTheSelfUser = false

            when (result1) {
                is StringResult -> {
                    user1Id = result1.string.hashCode().toLong()
                    user1Name = result1.string
                    user1AvatarUrl = "https://cdn.discordapp.com/embed/avatars/0.png?size=256"
                }
                is UserResult -> {
                    if (result1.user.id == context.user.id)
                        isShipWithTheSelfUser = true

                    user1Id = result1.user.idLong
                    user1Name = result1.user.name
                    user1AvatarUrl = result1.user.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
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
                    if (result2.user.id == context.user.id)
                        isShipWithTheSelfUser = true

                    user2Id = result2.user.idLong
                    user2Name = result2.user.name
                    user2AvatarUrl = result2.user.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
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
            var isLoritta = false
            var hasShipEffects = false
            var isLorittaWithShipEffects = false
            var isNatural = true
            val isNatural100Ship = value == 100
            val isNatural0Ship = value == 0
            val isNatural69Ship = value == 69

            // "Loritta will be cancelled on Twitter due to result manipulation, oh no!"
            if (user1Id == user2Id) {
                // Easter Egg: Love Yourself
                value = 100
                isLoveYourself = true
                isNatural = false // Not a natural ship
            } else if (user1Id == context.loritta.config.loritta.discord.applicationId.value.toLong() || user2Id == context.loritta.config.loritta.discord.applicationId.value.toLong()) {
                // Easter Egg: Shipping you/someone with Loritta
                val shipEffects = mutableListOf<PuddingShipEffect>()

                if (result1 is UserResult)
                    shipEffects += context.loritta.pudding.shipEffects.getShipEffectsForUser(UserId(user1Id))

                if (result2 is UserResult)
                    shipEffects += context.loritta.pudding.shipEffects.getShipEffectsForUser(UserId(user2Id))

                // TODO: Add RPC call to only get valid (non expired) ship effects
                // TODO: Add RPC call to do what we are doing here in the .firstOrNull { ... } call
                val firstMatchedShipEffect = shipEffects
                    .filter { it.expiresAt > Clock.System.now() }
                    .sortedByDescending { it.id }
                    .firstOrNull { (it.user1.value.toLong() == user1Id && it.user2.value.toLong() == user2Id) || (it.user2.value.toLong() == user1Id && it.user1.value.toLong() == user2Id) }

                isLoritta = true
                if (firstMatchedShipEffect != null) {
                    isLorittaWithShipEffects = true
                    hasShipEffects = true
                }

                value = (seed % 51).absoluteValue.toInt()
                isNatural = false // Not a natural ship
            } else if (result1 is UserResult && result2 is UserResult) {
                // We will only check for manipulated values if both users are a UserResult
                // Because we don't need to spend requests by checking if an StringResult has a marriage
                val user1Marriage = context.loritta.pudding.marriages.getMarriageByUser(UserId(user1Id))
                val user2Marriage = context.loritta.pudding.marriages.getMarriageByUser(UserId(user2Id))

                // If both users are married, and the marriage ID of both are the same, then it means that they are married together, how cute!
                if (user1Marriage != null && user2Marriage != null && user1Marriage.id == user2Marriage.id) {
                    value = 100 // And also set the value to 100% if they are married
                    isMarried = true
                    isNatural = false // Not a natural ship
                }

                // However ship effects can override the married percentage!
                val shipEffects = context.loritta.pudding.shipEffects.getShipEffectsForUser(UserId(user1Id)) + context.loritta.pudding.shipEffects.getShipEffectsForUser(
                    UserId(user2Id)
                )

                // TODO: Add RPC call to only get valid (non expired) ship effects
                // TODO: Add RPC call to do what we are doing here in the .firstOrNull { ... } call
                val firstMatchedShipEffect = shipEffects
                    .filter { it.expiresAt > Clock.System.now() }
                    .sortedByDescending { it.id }
                    .firstOrNull { (it.user1.value.toLong() == user1Id && it.user2.value.toLong() == user2Id) || (it.user2.value.toLong() == user1Id && it.user1.value.toLong() == user2Id) }

                if (firstMatchedShipEffect != null) {
                    value = firstMatchedShipEffect.editedShipValue
                    isNatural = false // Not a natural ship
                    hasShipEffects = true
                }
            }

            val loveTextResults: ListI18nData
            val loveTextEmote: Emote

            when {
                isLorittaWithShipEffects -> {
                    loveTextResults = I18N_PREFIX.ScoreLorittaWithShipEffect
                    loveTextEmote = Emotes.LoriHmpf
                }
                isLoritta -> {
                    loveTextResults = I18N_PREFIX.ScoreLoritta
                    loveTextEmote = Emotes.LoriShrug
                }
                isLoveYourself -> {
                    loveTextResults = I18N_PREFIX.ScoreLoveYourself
                    loveTextEmote = Emotes.LoriSmile
                }
                isMarried -> {
                    loveTextResults = I18N_PREFIX.ScoreMarried
                    loveTextEmote = Emotes.MarriageRing
                }
                value == 100 -> {
                    loveTextResults = I18N_PREFIX.ScorePerfect
                    loveTextEmote = Emotes.SparklingHeart
                }
                value in 67..99 -> {
                    loveTextResults = I18N_PREFIX.ScoreLove
                    loveTextEmote = Emotes.LoriHeart
                }
                value in 34..66 -> {
                    loveTextResults = I18N_PREFIX.ScoreShrug
                    loveTextEmote = Emotes.LoriShrug
                }
                value in 1..33 -> {
                    loveTextResults = I18N_PREFIX.ScoreSob
                    loveTextEmote = Emotes.LoriSob
                }
                value == 0 -> {
                    loveTextResults = I18N_PREFIX.ScoreImpossible
                    loveTextEmote = Emotes.LoriHmpf
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

            val result = try {
                context.loritta.gabrielaImageServerClient.images.ship(
                    ShipRequest(
                        URLImageData(user1AvatarUrl),
                        URLImageData(user2AvatarUrl),
                        value
                    )
                )
            } catch (e: Exception) { // This is called if the image wasn't found
                when (e) {
                    is ImageNotFoundException -> context.reply(false) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                            Emotes.LoriSob
                        )
                    }
                    is UntrustedURLException -> context.reply(false) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.ImageUrlIsUntrusted),
                            Emotes.LoriSob
                        )
                    }
                    is ImageTooLargeException, is StreamExceedsLimitException, is ContentLengthTooLargeException -> context.reply(false) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.SentImageIsTooLarge),
                            Emotes.LoriSob
                        )
                    }
                    else -> throw e // Propagate it
                }
                return
            }

            context.reply(false) {
                content = """${Emotes.LoriHeartCombo1}${Emotes.LoriHeartCombo2} **${context.i18nContext.get(I18N_PREFIX.NewCouple)}** ${Emotes.LoriHeartCombo1}${Emotes.PantufaHeartCombo2}
                |${Emotes.LoriReading} `$user1Name` + `$user2Name` = ${Emotes.Sparkles} **`$shipName`** ${Emotes.Sparkles}
                |$loveTextEmote $loveTextResult $loveTextEmote
            """.trimMargin()

                files += FileUpload.fromData(result.inputStream(), "ship.png")
                    .setDescription(context.i18nContext.get(I18N_PREFIX.ShipImageAltText(user1Name, user2Name, (value / 100.0))))
            }

            if (isNatural && isNatural100Ship)
                context.giveAchievementAndNotify(AchievementType.NATURAL_100_SHIP, ephemeral = true)
            if (isNatural && isNatural0Ship)
                context.giveAchievementAndNotify(AchievementType.NATURAL_0_SHIP, ephemeral = true)
            if (isNatural && isNatural69Ship)
                context.giveAchievementAndNotify(AchievementType.NATURAL_69_SHIP, ephemeral = true)
            if (isMarried)
                context.giveAchievementAndNotify(AchievementType.MARRIED_SHIP, ephemeral = true)
            if (isLoveYourself && isShipWithTheSelfUser)
                context.giveAchievementAndNotify(AchievementType.LOVE_YOURSELF, ephemeral = true)
            if (hasShipEffects)
                context.giveAchievementAndNotify(AchievementType.FISHY_SHIP, ephemeral = true)
            if (isLoritta && isShipWithTheSelfUser && !isLorittaWithShipEffects)
                context.giveAchievementAndNotify(AchievementType.FRIENDZONED_BY_LORITTA, ephemeral = true)
            if (isLorittaWithShipEffects && isShipWithTheSelfUser)
                context.giveAchievementAndNotify(AchievementType.SABOTAGED_LORITTA_FRIENDZONE, ephemeral = true)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val user1Name: String? = args.getOrNull(0)
            var user2Name: String? = args.getOrNull(1)

            val user1 = context.getUser(0)
            var user2 = context.getUser(1)

            if (user1Name != null && user2Name == null && user2 == null) {
                // If the user2 is null, but user1 is not null, we are going to use user2 to the user that executed the command
                //
                // So, if I use "+ship @Loritta" it would be the same thing as using "+ship @Loritta @YourUserHere"
                user2 = context.user
            }

            if (user2 != null) {
                user2Name = user2.name
            }

            var formattedUser1: String? = user1Name
            var formattedUser2: String? = user2Name

            // Inject retrieved users into the mentioned users list
            if (user1 != null) {
                formattedUser1 = user1.asMention
                context.mentions.injectUser(user1)
            }

            if (user2 != null) {
                formattedUser2 = user2.asMention
                context.mentions.injectUser(user2)
            }

            if (formattedUser1 == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.user1 to formattedUser1,
                options.user2 to formattedUser2
            )
        }
    }

    sealed class ConverterResult
    class UserResult(val user: User) : ConverterResult()
    class StringResult(val string: String) : ConverterResult()
    class StringWithImageResult(val string: String, val imageUrl: String) : ConverterResult()

    class ShipDiscordMentionInputConverter {
        suspend fun convert(context: UnleashedContext, input: String): ConverterResult {
            // Check for user mention
            // We could manually check it from context.mentions.users
            // But because we use legacy command context, a fake user-mention-in-a-string input won't magically provide the user in the context.mentions.user list
            val userMatch = DiscordUtils.extractUserFromString(
                context.loritta,
                input,
                context.mentions.users
            )

            if (userMatch != null)
                return UserResult(userMatch)

            // Check for emote mention
            val emoteMatch = DiscordRegexes.DiscordEmote.matchEntire(input)
            if (emoteMatch != null) {
                val isAnimated = emoteMatch.groupValues[1].isNotEmpty()
                val extension = if (isAnimated) "gif" else "png"

                return StringWithImageResult(
                    emoteMatch.groupValues[2],
                    "https://cdn.discordapp.com/emojis/${emoteMatch.groupValues[3]}.$extension?v=1"
                )
            }

            // We'll take only 100 characters to avoid any "message too long" issues
            // (Example: Before, if you typed a big enough message, the ship's alt text would trigger an error)
            return StringResult(input.take(100))
        }
    }
}