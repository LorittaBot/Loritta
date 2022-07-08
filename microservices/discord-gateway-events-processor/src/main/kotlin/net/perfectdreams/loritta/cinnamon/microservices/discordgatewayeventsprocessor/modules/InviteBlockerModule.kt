package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.gateway.Event
import dev.kord.gateway.InviteCreate
import dev.kord.gateway.InviteDelete
import dev.kord.gateway.MessageCreate
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.request.KtorRequestException
import io.ktor.http.*
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaPermission
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker.ActivateInviteBlockerBypassButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker.ActivateInviteBlockerData
import net.perfectdreams.loritta.cinnamon.platform.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.MessageUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.hasLorittaPermission
import net.perfectdreams.loritta.cinnamon.platform.utils.sources.MemberTokenSource
import net.perfectdreams.loritta.cinnamon.platform.utils.sources.UserTokenSource
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

class InviteBlockerModule(val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
    }

    private val cachedInviteLinks = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES)
        .build<Snowflake, Set<String>>()
        .asMap()

    override suspend fun processEvent(event: Event): ModuleResult {
        when (event) {
            is MessageCreate -> return handleMessage(event)
            // Delete invite list from cache when a server invite is created or deleted
            is InviteCreate -> event.invite.guildId.value?.let { cachedInviteLinks.remove(it) }
            is InviteDelete -> event.invite.guildId.value?.let { cachedInviteLinks.remove(it) }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleMessage(event: MessageCreate): ModuleResult {
        val message = event.message
        val guildId = message.guildId.value ?: return ModuleResult.Continue // Not in a guild
        val member = event.message.member.value ?: return ModuleResult.Continue // The member isn't in the guild
        val channelId = message.channelId
        val serverConfig = m.services.serverConfigs.getServerConfigRoot(guildId.value)
        val inviteBlockerConfig = serverConfig
            ?.getInviteBlockerConfig()
            ?: return ModuleResult.Continue
        if (inviteBlockerConfig.whitelistedChannels.contains(channelId.toLong()))
            return ModuleResult.Continue

        val i18nContext = m.languageManager.getI18nContextById(serverConfig.localeId)

        // Can the user bypass the invite blocker check?
        val canBypass = m.services.serverConfigs.hasLorittaPermission(guildId, member, LorittaPermission.ALLOW_INVITES)
        if (canBypass)
            return ModuleResult.Continue

        val content = message.content
            // We need to strip the code marks to avoid this:
            // https://cdn.discordapp.com/attachments/513405772911345664/760887806191992893/invite-bug.png
            .replace("`", "")
            .replace("\u200B", "")
            // https://discord.gg\loritta is actually detected as https://discord.gg/loritta on Discord
            // So we are going to flip all \ to /
            .replace("\\", "/")
            // https://discord.gg//loritta is actually detected as https://discord.gg/loritta on Discord
            // (yes, two issues, wow)
            // So we are going to replace all /+ to /, so https://discord.gg//loritta becomes https://discord.gg/loritta
            .replace(Regex("/+"), "/")

        val validMatchers = mutableListOf<Matcher>()
        val contentMatcher = getMatcherIfHasInviteLink(content)
        if (contentMatcher != null)
            validMatchers.add(contentMatcher)

        val embeds = message.embeds
        if (!isYouTubeLink(content)) {
            for (embed in embeds) {
                val descriptionMatcher = getMatcherIfHasInviteLink(embed.description)
                if (descriptionMatcher != null)
                    validMatchers.add(descriptionMatcher)

                val titleMatcher = getMatcherIfHasInviteLink(embed.title)
                if (titleMatcher != null)
                    validMatchers.add(titleMatcher)

                val urlMatcher = getMatcherIfHasInviteLink(embed.url)
                if (urlMatcher != null)
                    validMatchers.add(urlMatcher)

                val footerMatcher = getMatcherIfHasInviteLink(embed.footer.value?.text)
                if (footerMatcher != null)
                    validMatchers.add(footerMatcher)

                val authorNameMatcher = getMatcherIfHasInviteLink(embed.author.value?.name)
                if (authorNameMatcher != null)
                    validMatchers.add(authorNameMatcher)

                val authorUrlMatcher = getMatcherIfHasInviteLink(embed.author.value?.url)
                if (authorUrlMatcher != null)
                    validMatchers.add(authorUrlMatcher)

                val fields = embed.fields.value
                if (fields != null) {
                    for (field in fields) {
                        val fieldMatcher = getMatcherIfHasInviteLink(field.value)
                        if (fieldMatcher != null)
                            validMatchers.add(fieldMatcher)
                    }
                }
            }
        }

        // There isn't any matched links in the message!
        if (validMatchers.isEmpty())
            return ModuleResult.Continue

        // Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
        // necessário, e, ao pegar, vamos guardar no cache de invites
        val lorittaPermissions = m.cache.getLazyCachedLorittaPermissions(guildId, event.message.channelId)

        val allowedInviteCodes = mutableSetOf<String>()
        if (inviteBlockerConfig.whitelistServerInvites) {
            val cachedGuildInviteLinks = cachedInviteLinks[guildId]
            if (cachedGuildInviteLinks == null) {
                val guildInviteLinks = mutableSetOf<String>()

                if (lorittaPermissions.hasPermission(Permission.ManageGuild)) {
                    try {
                        val vanityInvite = m.rest.guild.getVanityInvite(guildId)
                        val vanityInviteCode = vanityInvite.code
                        if (vanityInviteCode != null)
                            guildInviteLinks.add(vanityInviteCode)
                    } catch (e: KtorRequestException) {
                        // Forbidden = The server does not have the feature enabled
                        if (e.httpResponse.status != HttpStatusCode.Forbidden)
                            throw e
                    }

                    val invites = m.rest.guild.getGuildInvites(guildId) // This endpoint does not return the vanity invite
                    val codes = invites.map { it.code }
                    guildInviteLinks.addAll(codes)
                }

                allowedInviteCodes.addAll(guildInviteLinks)
            } else {
                allowedInviteCodes.addAll(cachedGuildInviteLinks)
            }

            cachedInviteLinks[guildId] = allowedInviteCodes
        }

        for (matcher in validMatchers) {
            val urls = mutableSetOf<String>()
            while (matcher.find()) {
                var url = matcher.group()
                if (url.startsWith("discord.gg", true)) {
                    url = "discord.gg" + matcher.group(1).replace(".", "")
                }
                urls.add(url)
            }

            for (url in urls) {
                val inviteId = DiscordInviteUtils.getInviteCodeFromUrl(url) ?: continue

                if (inviteId in allowedInviteCodes)
                    continue

                if (inviteBlockerConfig.deleteMessage && lorittaPermissions.hasPermission(Permission.ManageMessages)) {
                    try {
                        // Discord does not log messages deleted by bots, so providing an audit log reason is pointless
                        m.rest.channel.deleteMessage(
                            channelId,
                            message.id
                        )
                    } catch (e: KtorRequestException) {
                        // Maybe the message was deleted by another bot?
                        if (e.httpResponse.status != HttpStatusCode.NotFound)
                            throw e
                    }
                }

                val warnMessage = inviteBlockerConfig.warnMessage

                if (inviteBlockerConfig.tellUser && !warnMessage.isNullOrEmpty() && lorittaPermissions.canTalk()) {
                    val toBeSent = MessageUtils.createMessage(
                        warnMessage,
                        listOf(
                            UserTokenSource(event.message.author),
                            MemberTokenSource(event.message.author, member)
                        ),
                        emptyMap()
                    )

                    val sentMessage = m.rest.channel.createMessage(channelId) {
                        toBeSent.apply(this)
                    }

                    if (m.cache.hasPermission(guildId, channelId, event.message.author.id, Permission.ManageGuild)) {
                        // If the user has permission to enable the invite bypass permission, make Loritta recommend to enable the permission
                        val topRole = m.cache.getRoles(guildId, member)
                            .asSequence()
                            .sortedByDescending { it.position }
                            .filter { !it.managed }
                            .filter { it.id != guildId } // If it is the role ID == guild ID, then it is the @everyone role!
                            .firstOrNull()

                        if (topRole != null) {
                            // A role has been found! Tell the user about enabling the invite blocker bypass
                            m.rest.channel.createMessage(channelId) {
                                this.failIfNotExists = false
                                this.messageReference = sentMessage.id

                                styled(
                                    i18nContext.get(I18nKeysData.Modules.InviteBlocker.ActivateInviteBlockerBypass("<@&${topRole.id}>")),
                                    Emotes.LoriSmile
                                )

                                styled(
                                    i18nContext.get(I18nKeysData.Modules.InviteBlocker.HowToReEnableLater("<${m.config.loritta.website}guild/${message.author.id}/configure/permissions>")),
                                    Emotes.LoriHi
                                )

                                actionRow {
                                    interactiveButton(
                                        ButtonStyle.Primary,
                                        i18nContext.get(I18nKeysData.Modules.InviteBlocker.AllowSendingInvites),
                                        ActivateInviteBlockerBypassButtonClickExecutor,
                                        m.encodeDataForComponentOrStoreInDatabase(
                                            ActivateInviteBlockerData(
                                                message.author.id,
                                                topRole.id
                                            )
                                        )
                                    ) {
                                        loriEmoji = Emotes.LoriPat
                                    }
                                }

                                // Empty allowed mentions because we don't want to mention the role
                                allowedMentions {}
                            }
                        }
                    }
                }

                // Invite has been found, exit!
                return ModuleResult.Cancel
            }
        }

        return ModuleResult.Continue
    }

    /**
     * Checks if [content] contains a YouTube Link
     *
     * @param content the content
     * @return if the link contains a YouTube URL
     */
    private fun isYouTubeLink(content: String?): Boolean {
        if (content.isNullOrBlank())
            return false

        val matcher = URL_PATTERN.matcher(content)
        return if (matcher.find()) {
            val everything = matcher.group(0)
            val afterSlash = matcher.group(1)
            val uri = everything.replace(afterSlash, "")
            uri.endsWith("youtube.com") || uri.endsWith("youtu.be")
        } else {
            false
        }
    }

    private fun getMatcherIfHasInviteLink(optionalString: Optional<String>?) = optionalString?.let {
        getMatcherIfHasInviteLink(it.value)
    }

    private fun getMatcherIfHasInviteLink(content: String?): Matcher? {
        if (content.isNullOrBlank())
            return null

        val matcher = URL_PATTERN.matcher(content)
        return if (matcher.find()) {
            matcher.reset()
            matcher
        } else {
            null
        }
    }
}