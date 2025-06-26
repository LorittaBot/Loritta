package net.perfectdreams.loritta.morenitta.utils

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import org.jetbrains.exposed.sql.insert
import java.util.concurrent.TimeUnit

/**
 * Used to manage ratelimits for commands.
 */
class CommandCooldownManager(val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    private val userCooldown = Caffeine.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .maximumSize(1_000)
            .build<Long, Long>()
            .asMap()
    private val lastRatelimitMessageForUser = Caffeine.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .maximumSize(1_000)
            .build<Long, Long>()
            .asMap()
    private val growingRatelimitUsers = Caffeine.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .maximumSize(1_000)
            .build<Long, Int>()
            .asMap()

    suspend fun checkCooldown(ev: LorittaMessageEvent, commandCooldown: Int): CooldownResponse {
        val cooldownTriggeredAt = userCooldown.getOrDefault(ev.author.idLong, 0L)
        val diff = System.currentTimeMillis() - cooldownTriggeredAt

        val commandCooldownMultiplied = getUserCommandCooldown(ev.author, commandCooldown)

        // Example:
        // First command executed:
        // User Cooldown is set to current time
        //
        // First command executed during cooldown:
        // lastRatelimitMessageForUser is set
        //
        // Second command executed during cooldown:
        // Detected as message already sent, but will still be notified
        // growingRatelimitUsers is set to 2
        //
        // Third command executed during cooldown:
        // Should be notified *but* with increased cooldown
        // growingRatelimitUsers is set to 3
        //
        // Up until 5 messages are allowed, after that Lori will start to ratelimit the messages until 100 ratelimited commands, which triggers a one week ban.
        if (commandCooldownMultiplied >= diff && !loritta.isOwner(ev.author.idLong)) {
            // To avoid users creating a lot of "Watch out! You need to wait ..." messages, we are going to ratelimit the ratelimit message.
            //
            // So, if the user already used a command and is still on cooldown, we are going to ignore the message until the command cooldown is complete.
            val wasMessageAlreadySent = lastRatelimitMessageForUser.containsKey(ev.author.idLong)

            val newValue = growingRatelimitUsers.getOrPut(ev.author.idLong) { 0 } + 1
            growingRatelimitUsers[ev.author.idLong] = newValue

            val correctedCooldownMultiplied = getUserCommandCooldown(ev.author, commandCooldown)

            if (wasMessageAlreadySent) {
                logger.warn { "User ${ev.author} tried to use command while on cooldown *and* message was already sent! Needs to wait: $commandCooldownMultiplied; New Growing Value: $newValue" }

                val mod = when {
                    5 >= newValue -> 1
                    20 >= newValue -> 5
                    60 >= newValue -> 10
                    else -> 15
                }

                if (newValue >= 100) {
                    // Autoban, kthxbye!!
                    loritta.newSuspendedTransaction {
                        BannedUsers.insert {
                            it[userId] = ev.author.idLong
                            it[bannedAt] = System.currentTimeMillis()
                            it[bannedBy] = null
                            it[valid] = true
                            it[expiresAt] = System.currentTimeMillis() + (Constants.ONE_DAY_IN_MILLISECONDS * 3)
                            it[reason] = "Spamming too many messages during command cooldown! (Macro/Selfbot/Userbot) Guild ID: ${ev.guild?.idLong}; Channel ID: ${ev.message.channel.idLong}"
                        }
                    }

                    return CooldownResponse(
                            CooldownStatus.RATE_LIMITED_MESSAGE_ALREADY_SENT,
                            cooldownTriggeredAt,
                            correctedCooldownMultiplied
                    )
                }

                // Send message after increasing it X times, this allows the stupid user to see what they are doing
                if (newValue % mod == 0)
                    if (newValue >= 3) {
                        return CooldownResponse(
                            CooldownStatus.RATE_LIMITED_SEND_MESSAGE_REPEATED,
                            cooldownTriggeredAt,
                            correctedCooldownMultiplied
                        )
                    } else {
                        return CooldownResponse(
                            CooldownStatus.RATE_LIMITED_SEND_MESSAGE,
                            cooldownTriggeredAt,
                            correctedCooldownMultiplied
                        )
                    }


                return CooldownResponse(
                        CooldownStatus.RATE_LIMITED_MESSAGE_ALREADY_SENT,
                        cooldownTriggeredAt,
                        correctedCooldownMultiplied
                )
            }

            lastRatelimitMessageForUser[ev.author.idLong] = System.currentTimeMillis()

            return CooldownResponse(
                    CooldownStatus.RATE_LIMITED_SEND_MESSAGE,
                    cooldownTriggeredAt,
                    correctedCooldownMultiplied
            )
        } else {
            lastRatelimitMessageForUser.remove(ev.author.idLong)
            growingRatelimitUsers.remove(ev.author.idLong)
        }

        userCooldown[ev.author.idLong] = System.currentTimeMillis()

        return CooldownResponse(
                CooldownStatus.OK,
                cooldownTriggeredAt,
                commandCooldownMultiplied
        )
    }

    fun removeUserCooldown(user: User) = growingRatelimitUsers.remove(user.idLong)

    private fun getUserCommandCooldown(user: User, commandCooldown: Int) = getUserCommandCooldown(user.idLong, commandCooldown)

    private fun getUserCommandCooldown(userId: Long, commandCooldown: Int) = commandCooldown *
            Math.max(
                    1,
                    (
                            growingRatelimitUsers.getOrDefault(
                                    userId, 1
                            )
                            )
            )

    data class CooldownResponse(
            val status: CooldownStatus,
            val cooldownTriggeredAt: Long,
            val commandCooldown: Int
    )

    enum class CooldownStatus(val sendMessage: Boolean) {
        OK(false),
        RATE_LIMITED_SEND_MESSAGE(true),
        RATE_LIMITED_SEND_MESSAGE_REPEATED(true),
        RATE_LIMITED_MESSAGE_ALREADY_SENT(false)
    }
}