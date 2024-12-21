package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.sendMessageAsync
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule(val loritta: LorittaBot) : MessageReceivedModule {
    companion object {
        val cachedInviteLinks = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build<Long, List<String>>().asMap()
	
        val unicodeLookalikes = mapOf(
            "a" to listOf("\u0430", "\u00e0", "\u00e1", "\u1ea1", "\u0105"),
            "c" to listOf("\u0441", "\u0188", "\u010b"),
            "d" to listOf("\u0501", "\u0257"),
            "e" to listOf("\u0435", "\u1eb9", "\u0117", "\u0117", "\u00e9", "\u00e8"),
            "g" to listOf("\u0121"),
            "h" to listOf("\u04bb"),
            "i" to listOf("\u0456", "\u00ed", "\u00ec", "\u00ef"),
            "j" to listOf("\u0458", "\u029d"),
            "k" to listOf("\u03ba"),
            "l" to listOf("\u04cf", "\u1e37"),
            "n" to listOf("\u0578"),
            "o" to listOf(
                "\u043e", "\u03bf", "\u0585", "\u022f", "\u1ecd", "\u1ecf", "\u01a1", "\u00f6", "\u00f3", "\u00f2"
            ),
            "p" to listOf("\u0440"),
            "q" to listOf("\u0566"),
            "s" to listOf("\u0282"),
            "u" to listOf("\u03c5", "\u057d", "\u00fc", "\u00fa", "\u00f9"),
            "v" to listOf("\u03bd", "\u0475"),
            "x" to listOf("\u0445", "\u04b3"),
            "y" to listOf("\u0443", "\u00fd"),
            "z" to listOf("\u0290", "\u017c")
        )
    }

    private fun replaceLookalikes(content: String): String {
        var modifiedContent = content
        for ((original, lookalikes) in unicodeLookalikes) {
            for (lookalike in lookalikes) {
                modifiedContent = modifiedContent.replace(lookalike, original)
            }
        }
        return modifiedContent
    }

    private fun checkMessage(message: MessageWrapper): List<Matcher> {
        val content = stripAnythingThatCouldCauseIssues(message.contentRaw)
        val modifiedContent = replaceLookalikes(content)
        val validMatchers = mutableListOf<Matcher>()
        val contentMatcher = getMatcherIfHasInviteLink(modifiedContent)
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

                val footerMatcher = getMatcherIfHasInviteLink(embed.footer?.text)
                if (footerMatcher != null)
                    validMatchers.add(footerMatcher)

                val authorNameMatcher = getMatcherIfHasInviteLink(embed.author?.name)
                if (authorNameMatcher != null)
                    validMatchers.add(authorNameMatcher)

                val authorUrlMatcher = getMatcherIfHasInviteLink(embed.author?.url)
                if (authorUrlMatcher != null)
                    validMatchers.add(authorUrlMatcher)

                for (field in embed.fields) {
                    val fieldMatcher = getMatcherIfHasInviteLink(field.value)
                    if (fieldMatcher != null)
                        validMatchers.add(fieldMatcher)
                }
            }
        }

        return validMatchers
    }
}

	sealed class MessageWrapper {
		abstract val contentRaw: String
		abstract val embeds: List<MessageEmbed>

		class Message(val message: net.dv8tion.jda.api.entities.Message) : MessageWrapper() {
			override val contentRaw
				get() = message.contentRaw
			override val embeds
				get() = message.embeds
		}

		class MessageSnapshot(val messageSnapshot: net.dv8tion.jda.api.entities.messages.MessageSnapshot) : MessageWrapper() {
			override val contentRaw
				get() = messageSnapshot.contentRaw
			override val embeds
				get() = messageSnapshot.embeds
		}
	}
}
