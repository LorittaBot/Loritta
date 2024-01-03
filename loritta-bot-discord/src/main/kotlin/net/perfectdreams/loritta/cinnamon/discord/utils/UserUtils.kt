package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.UserData
import dev.kord.core.entity.Icon
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.json.request.DMCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import dev.kord.rest.request.KtorRequestException
import dev.kord.rest.route.Position
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.serializable.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

object UserUtils {
    private val logger = KotlinLogging.logger {}

    suspend fun handleIfUserIsBanned(loritta: LorittaBot, context: InteractionContext, user: User)
            = handleIfUserIsBanned(loritta, context, user.id)

    suspend fun handleIfUserIsBanned(loritta: LorittaBot, context: InteractionContext, userId: Snowflake): Boolean {
        // Check if the user is banned from using Loritta
        val userBannedState = loritta.pudding.users.getUserBannedState(UserId(userId))

        if (userBannedState != null) {
            val banDateInEpochMillis = userBannedState.bannedAt.toEpochMilliseconds()
            val expiresDateInEpochMillis = userBannedState.expiresAt?.toEpochMilliseconds()

            val messageBuilder: MessageBuilder.() -> (Unit) = {
                if (expiresDateInEpochMillis == null) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.UserIsBannedPermanent("<@$userId>")),
                        Emotes.LoriBonk
                    )
                } else {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.UserIsBannedTemporary("<@$userId>")),
                        Emotes.LoriBonk
                    )
                }

                styled(
                    context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.Reason),
                    Emotes.LoriReading
                )

                styled(userBannedState.reason)

                styled(
                    context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.BannedAt(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(banDateInEpochMillis))),
                    Emotes.LoriHmpf
                )

                if (expiresDateInEpochMillis == null) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.ExpiresAtPermanent),
                        Emotes.LoriLurk
                    )
                } else {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.ExpiresAtTemporary(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(expiresDateInEpochMillis))),
                        Emotes.LoriLurk
                    )
                }

                styled(
                    context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.TooLateToSaySorry),
                    Emotes.LoriSob
                )
            }

            if (context.interaKTionsContext.isDeferred && !context.interaKTionsContext.wasInitiallyDeferredEphemerally) {
                context.sendMessage(messageBuilder)
            } else {
                context.sendEphemeralMessage(messageBuilder)
            }
            return true
        }

        return false
    }

    /**
     * Creates a [Icon.UserAvatar] from the [userId] and [avatar]. If the [avatar] is null, a [Icon.DefaultUserAvatar] is created based on the [discriminator].
     *
     * @param userId        the user's ID
     * @param avatar        the user's avatar hash
     * @param discriminator the user's discriminator
     */
    fun createUserAvatarOrDefaultUserAvatar(kord: Kord, userId: Snowflake, avatar: String?, discriminator: String) = createUserAvatarOrDefaultUserAvatar(kord, userId, avatar, discriminator.toInt())

    /**
     * Creates a [Icon.UserAvatar] from the [userId] and [avatar]. If the [avatar] is null, a [Icon.DefaultUserAvatar] is created based on the [discriminator].
     *
     * @param userId        the user's ID
     * @param avatar        the user's avatar hash
     * @param discriminator the user's discriminator
     */
    fun createUserAvatarOrDefaultUserAvatar(kord: Kord, userId: Snowflake, avatar: String?, discriminator: Int) = if (avatar != null) {
        Icon.UserAvatar(
            userId,
            avatar,
            kord
        )
    } else {
        Icon.DefaultUserAvatar(discriminator, kord)
    }

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     *
     * @return if the message was successfully sent, a message may fail to be sent if the channel does not exist or if the user disabled their DMs
     */
    suspend fun sendMessageToUserViaDirectMessage(
        pudding: Pudding,
        rest: RestClient,
        userId: UserId,
        builder: UserMessageCreateBuilder.() -> (Unit)
    ) = sendMessageToUserViaDirectMessage(
        pudding,
        rest,
        userId,
        UserMessageCreateBuilder().apply(builder).toRequest()
    )

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     *
     * @return if the message was successfully sent, a message may fail to be sent if the channel does not exist or if the user disabled their DMs
     */
    suspend fun sendMessageToUserViaDirectMessage(
        pudding: Pudding,
        rest: RestClient,
        userId: UserId,
        request: MultipartMessageCreateRequest
    ): Boolean {
        val cachedChannelId = pudding.users.getCachedDiscordDirectMessageChannel(userId)

        return try {
            val channelId = cachedChannelId ?: run {
                val id = rest.user.createDM(DMCreateRequest(Snowflake(userId.value.toLong()))).id.value.toLong()
                pudding.users.insertOrUpdateCachedDiscordDirectMessageChannel(userId, id)
                id
            }

            rest.channel.createMessage(
                Snowflake(channelId),
                request
            )
            true
        } catch (e: KtorRequestException) {
            // 17:18:41.465 [DefaultDispatcher-worker-1] DEBUG [R]:[KTOR]:[ExclusionRequestRateLimiter] - [RESPONSE]:403:POST:https://discord.com/api/v9/channels/944325028885971016/messages body:{"message": "Cannot send messages to this user", "code": 50007}
            logger.warn(e) { "Something went wrong while trying to send a message to $userId! Invalidating cached direct message if present..." }
            pudding.users.deleteCachedDiscordDirectMessageChannel(userId)
            false
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(
        i18nContext: I18nContext,
        lorittaWebsiteUrl: String,
        userId: UserId,
        data: DailyTaxWarnUserNotification
    ): MessageBuilder.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.TitleWarning)

            description = i18nContext.get(
                I18nKeysData.InactiveDailyTax.Warn(
                    user = "<@${userId.value}>",
                    sonhosTaxBracketThreshold = data.minimumSonhosForTrigger,
                    currentSonhos = data.currentSonhos,
                    daysWithoutGettingDaily = data.maxDayThreshold,
                    howMuchWillBeRemoved = data.howMuchWillBeRemoved,
                    taxPercentage = data.tax,
                    inactivityTaxTimeWillBeTriggeredAt = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:f>",
                    timeWhenDailyTaxIsTriggered = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:t>",
                    dailyLink = GACampaigns.dailyWebRewardDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-warned-about-taxes"
                    ),
                    premiumLink = GACampaigns.premiumUpsellDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-warned-about-taxes"
                    )
                )
            ).joinToString("\n")

            color = LorittaColors.LorittaRed.toKordColor()

            timestamp = data.timestamp

            image = lorittaWebsiteUrl + "v3/assets/img/sonhos/loritta_sonhos_drool.png"
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(
        i18nContext: I18nContext,
        lorittaWebsiteUrl: String,
        userId: UserId,
        data: DailyTaxTaxedUserNotification
    ): MessageBuilder.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.TitleTaxed)

            description = i18nContext.get(
                I18nKeysData.InactiveDailyTax.Taxed(
                    user = "<@${userId.value}>",
                    sonhosTaxBracketThreshold = data.minimumSonhosForTrigger,
                    howMuchWasRemoved = data.howMuchWasRemoved,
                    daysWithoutGettingDaily = data.maxDayThreshold,
                    taxPercentage = data.tax,
                    nextInactivityTaxTimeWillBeTriggeredAt = "<t:${data.nextInactivityTaxTimeWillBeTriggeredAt.epochSeconds}:f>",
                    timeWhenDailyTaxIsTriggered = "<t:${data.nextInactivityTaxTimeWillBeTriggeredAt.epochSeconds}:t>",
                    dailyLink = GACampaigns.dailyWebRewardDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-taxed"
                    ),
                    premiumLink = GACampaigns.premiumUpsellDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-taxed"
                    )
                )
            ).joinToString("\n")

            color = LorittaColors.LorittaRed.toKordColor()

            timestamp = data.timestamp

            image = lorittaWebsiteUrl + "v3/assets/img/sonhos/loritta_sonhos_running.png"
        }
    }

    suspend fun fillUsersFromRecentMessages(
        context: InteractionContext,
        users: List<User?>
    ): UserFillResult {
        val targetSize = users.size
        var noPermissionToQuery = false
        val usersToBeFilled = users.toMutableList()

        // Are in a guild? Because if yes, we can do more :sparkles: crazy :sparkles: stuff to fill the users
        if (context is GuildApplicationCommandContext) {
            val guildId = context.guildId

            if (users.filterNotNull().size != targetSize) {
                // Get random users from chat
                try {
                    val currentNotNullUserIds = users.filterNotNull().map { it.id }

                    val messages = context.loritta.rest.channel.getMessages(context.channelId, limit = 100)

                    // We shuffle the array to avoid users using the same command a lot of times... just to be bored because all the responses are (almost) the same
                    // We also remove any users that are already present in the listOfUsers list
                    val uniqueUsers = messages
                        .asSequence()
                        .map { it.author }
                        .distinctBy { it.id }
                        .filter { it.id !in currentNotNullUserIds }
                        .shuffled()
                        .toList()

                    val uniqueNonBotUsers = LinkedList(uniqueUsers.filter { !it.bot.discordBoolean })
                    val uniqueBotUsers = LinkedList(uniqueUsers.filter { it.bot.discordBoolean })

                    // First we will get non bot users, because users love complaining that "but I don't want to have bots on my sad reality meme!! bwaaa!!"
                    while (usersToBeFilled.filterNotNull().size != targetSize && uniqueNonBotUsers.isNotEmpty()) {
                        val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                        usersToBeFilled[indexOfFirstNullEntry] = User(UserData.from(uniqueNonBotUsers.poll()), context.loritta.kord)
                    }

                    // If we still haven't found it, we will query bot users so the user can at least have a sad reality instead of a "couldn't find enough users" message
                    while (usersToBeFilled.filterNotNull().size != targetSize && uniqueBotUsers.isNotEmpty()) {
                        val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                        usersToBeFilled[indexOfFirstNullEntry] = User(UserData.from(uniqueBotUsers.poll()), context.loritta.kord)
                    }
                } catch (e: KtorRequestException) {
                    // No permission to query!
                    noPermissionToQuery = true
                }
            }

            // Okay, so it is still not filled, well...
            if (users.filterNotNull().size != targetSize) {
                // What we can do is pull from the GuildProfiles!
                val usersInTheGuild = context.loritta.pudding.transaction {
                    GuildProfiles.slice(GuildProfiles.userId)
                        .select { GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.isInGuild eq true) }
                        .map { it[GuildProfiles.userId] }
                        .filter { it !in usersToBeFilled.mapNotNull { it?.id?.toLong() } } // Ignore users that are already in the list
                        .shuffled()
                        .let { LinkedList(it) }
                }

                // From that, we will try filling out the null information
                while (usersToBeFilled.filterNotNull().size != targetSize && usersInTheGuild.isNotEmpty()) {
                    val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                    val userId = usersInTheGuild.poll()

                    try {
                        val user = context.loritta.rest.user.getUser(Snowflake(userId))
                        usersToBeFilled[indexOfFirstNullEntry] = User(UserData.from(user), context.loritta.kord)
                    } catch (e: KtorRequestException) {
                        // The user doesn't exist anymore! (Maybe?)
                    }
                }
            }

            // Hmm, now we are in the "This is the last shot" territory
            if (users.filterNotNull().size != targetSize) {
                // If it is a guild context, we can try pulling random users from the server!
                val members = context.loritta.rest.guild.getGuildMembers(
                    context.guildId,
                    Position.After(Snowflake(0)),
                    // We will try getting 1000 members...
                    // Because we aren't changing the position, it will always be the same 1000 "oldest" Discord users of the server
                    1000
                )
                    .filter { it.user.value?.id !in usersToBeFilled.mapNotNull { it?.id } } // Ignore users that are already in the list
                    .shuffled() // Shuffle the list to avoid the same results every single time...
                    .let { LinkedList(it) }

                while (usersToBeFilled.filterNotNull().size != targetSize && members.isNotEmpty()) {
                    val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                    // The user shouldn't be null here... well, I hope so
                    usersToBeFilled[indexOfFirstNullEntry] = User(UserData.from(members.poll().user.value!!), context.loritta.kord)
                }
            }
        }

        val nonNullUsers = usersToBeFilled.filterNotNull()

        return UserFillResult(
            nonNullUsers,
            nonNullUsers.size == targetSize,
            noPermissionToQuery
        )
    }

    data class UserFillResult(
        val users: List<User>,
        val successfullyFilled: Boolean,
        val noPermissionToQuery: Boolean
    )
}