package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.MessageCreate
import dev.kord.rest.builder.message.create.allowedMentions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.serializable.UserId

class AFKModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (context.event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                handleAFK(context.event)
            }
            else -> {}
        }

        return ModuleResult.Continue
    }

    suspend fun handleAFK(
        messageCreate: MessageCreate
    ) {
        val guildId = messageCreate.message.guildId.value ?: return // Only process it in a guild
        val channelId = messageCreate.message.channelId

        // We don't want to process bot's AFK states, or reply to bot user mentions
        if (messageCreate.message.author.bot.discordBoolean)
            return

        // Disable User's AFK state if they sent a message
        val selfUserProfile = m.pudding.users.getUserProfile(UserId(messageCreate.message.author.id.value))
        selfUserProfile?.disableAfk()

        // User ID -> Reason
        val afkMembers = mutableListOf<Pair<Snowflake, String?>>()

        // Check all mentioned users...
        for (mention in messageCreate.message.mentions) {
            // Bots can't be AFK so let's skip over them
            if (mention.bot.discordBoolean)
                continue

            // Are they AFK?
            val lorittaProfile = m.pudding.users.getUserProfile(UserId(mention.id.value))

            if (lorittaProfile != null && lorittaProfile.isAfk) {
                // omg they are AFK!
                afkMembers.add(mention.id to lorittaProfile.afkReason)
            }
        }

        if (afkMembers.isNotEmpty()) {
            val lorittaPermissions = m.cache.getLazyCachedLorittaPermissions(guildId, channelId)

            // I can't talk here! Bye!!
            if (!lorittaPermissions.canTalk())
                return

            // Okay, so there are AFK members in the message!
            val serverConfig = m.pudding.serverConfigs.getServerConfigRoot(guildId.value)
            val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig?.localeId ?: "default")

            val message = m.rest.channel.createMessage(
                channelId
            ) {
                failIfNotExists = false
                messageReference = messageCreate.message.id

                if (afkMembers.size == 1) {
                    val (afkMemberId, afkReason) = afkMembers.first()

                    styled(
                        buildString {
                            append(i18nContext.get(I18nKeysData.Modules.Afk.UserIsAfk("<@${afkMemberId}>")))
                            if (afkReason != null) {
                                append(" ")
                                // To make things simpler, we will use empty set
                                append(i18nContext.get(I18nKeysData.Modules.Afk.AfkReason(cleanUpForOutput(m, guildId, emptySet(), afkReason))))
                            }
                        },
                        Emotes.LoriSleeping
                    )
                } else {
                    styled(
                        buildString {
                            append(i18nContext.get(I18nKeysData.Modules.Afk.UsersAreAfk(afkMembers.joinToString { "<@${it.first}>" })))

                            for ((afkMemberId, afkReason) in afkMembers) {
                                if (afkReason != null) {
                                    append("\n")
                                    append(
                                        i18nContext.get(
                                            I18nKeysData.Modules.Afk.AfkUserReason(
                                                "<@${afkMemberId}>",
                                                cleanUpForOutput(m, guildId, emptySet(), afkReason)
                                            )
                                        )
                                    )
                                }
                            }
                        },
                        Emotes.LoriSleeping
                    )
                }

                allowedMentions {} // We don't want to mention the users
            }

            // TODO: Don't use GlobalScope
            GlobalScope.launch {
                delay(5_000) // Wait 5s and then delete the AFK message

                m.rest.channel.deleteMessage(channelId, message.id)
            }
        }
    }
}