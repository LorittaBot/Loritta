package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import java.awt.Color

class LanguageCommand : AbstractCommand("language", listOf("linguagem", "speak"), category = CommandCategory.MISC) {
    private val resetPersonalLanguageEmote = "\uD83D\uDE45"

    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale.toNewLocale()["commands.misc.language.description", "\uD83D\uDE0A"]
    }

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.MANAGE_SERVER)
    }

    override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
        val profile = loritta.getOrCreateLorittaProfile(context.userHandle.idLong)

        val hasPersonalLanguage = loritta.newSuspendedTransaction { profile.settings.language != null && context.isPrivateChannel }

        val embed = EmbedBuilder()
        embed.setColor(Color(0, 193, 223))

        val validLanguages = listOf(
                LocaleWrapper(
                        "Português-Brasil",
                        loritta.getLocaleById("default"),
                        loritta.getLegacyLocaleById("default"),
                        "\uD83C\uDDE7\uD83C\uDDF7"
                ),
                /* LocaleWrapper(
                        "Português-Portugal",
                        loritta.getLocaleById("pt-pt"),
                        loritta.getLegacyLocaleById("pt-pt"),
                        "\uD83C\uDDF5\uD83C\uDDF9"
                ), */
                LocaleWrapper(
                        "English (United States)",
                        loritta.getLocaleById("en-us"),
                        loritta.getLegacyLocaleById("en-us"),
                        "\uD83C\uDDFA\uD83C\uDDF8"
                ),
                /* LocaleWrapper(
                        "Español",
                        loritta.getLocaleById("es-es"),
                        loritta.getLegacyLocaleById("es-es"),
                        "\uD83C\uDDEA\uD83C\uDDF8"
                ), */
                LocaleWrapper(
                        "Português-Funk",
                        loritta.getLocaleById("pt-funk"),
                        loritta.getLegacyLocaleById("pt-funk"),
                        "<:loritta_quebrada:338679008210190336>"
                ),
                LocaleWrapper(
                        "Português-Furry",
                        loritta.getLocaleById("pt-furry"),
                        loritta.getLegacyLocaleById("default"),
                        "\uD83D\uDC3E"
                ),
                LocaleWrapper(
                        "English-Furry",
                        loritta.getLocaleById("en-furry"),
                        loritta.getLegacyLocaleById("default"),
                        "\uD83D\uDC31"
                )
        )

        if (context.rawArgs.getOrNull(0) == "br-debug") {
            activateLanguage(
                    context,
                    profile,
                    LocaleWrapper(
                            "Auto-PT-BR-Debug",
                            loritta.getLocaleById("br-debug"),
                            loritta.getLegacyLocaleById("default"),
                            "\uD83D\uDC31"
                    )
            )
            return
        }

        if (context.rawArgs.getOrNull(0) == "en-debug") {
            activateLanguage(
                    context,
                    profile,
                    LocaleWrapper(
                            "Auto-EN-Debug",
                            loritta.getLocaleById("en-debug"),
                            loritta.getLegacyLocaleById("default"),
                            "\uD83D\uDC31"
                    )
            )
            return
        }

        val message = context.sendMessage(
                context.getAsMention(true),
                buildLanguageEmbed(
                        locale.toNewLocale(),
                        validLanguages.subList(0, 2),
                        context.isPrivateChannel,
                        hasPersonalLanguage
                )
        )

        message.onReactionAddByAuthor(context) {
            if (it.reactionEmote.isEmote("426183783008698391")) {
                message.edit(
                        " ",
                        buildLanguageEmbed(
                                locale.toNewLocale(),
                                validLanguages.subList(2, validLanguages.size),
                                context.isPrivateChannel,
                                hasPersonalLanguage
                        ),
                        true
                )

                for (wrapper in validLanguages.subList(2, validLanguages.size)) {
                    // O "replace" é necessário já que a gente usa emojis personalizados para algumas linguagens
                    message.addReaction(wrapper.emoteName.replace("<", "").replace(">", "")).queue()
                }
                return@onReactionAddByAuthor
            }

            // This removes/resets user's personal language (when you choose a language in DM)
            if (it.reactionEmote.isEmote(resetPersonalLanguageEmote) && hasPersonalLanguage) {
                loritta.newSuspendedTransaction {
                    profile.settings.language = null
                }
                context.reply(
                        LorittaReply(
                                locale.toNewLocale()["commands.misc.language.removedPersonalLanguage"]
                        )
                )
                return@onReactionAddByAuthor
            }

            val newLanguage = validLanguages.firstOrNull { language ->
                if (language.emoteName.startsWith("<")) {
                    it.reactionEmote.isEmote(language.emoteName.split(":")[2].removeSuffix(">"))
                } else {
                    it.reactionEmote.isEmote(language.emoteName)
                }
            }

            message.delete().queue()
            activateLanguage(context, profile, newLanguage
                    ?: validLanguages.first { it.locale.id == "default" }, context.isPrivateChannel)
        }

        for (wrapper in validLanguages.subList(0, 2)) {
            // O "replace" é necessário já que a gente usa emojis personalizados para algumas linguagens
            message.addReaction(wrapper.emoteName.replace("<", "").replace(">", "")).queue()
        }

        message.addReaction("lori_ok_hand:426183783008698391").queue()

        if (hasPersonalLanguage) message.addReaction(resetPersonalLanguageEmote).queue()
    }

    private suspend fun activateLanguage(context: CommandContext, profile: Profile, newLanguage: LocaleWrapper, isPrivateChannel: Boolean = false) {
        var localeId = newLanguage.locale.id

        loritta.newSuspendedTransaction {
            if (isPrivateChannel) // If command was executed in DM channel, will be set only to user
                profile.settings.language = localeId
            else
                context.config.localeId = localeId
        }

        val newLocale = loritta.getLocaleById(localeId)
        if (localeId == "default") {
            localeId = "pt-br" // Já que nós já salvamos, vamos trocar o localeId para algo mais "decente"
        }

        if (isPrivateChannel)
            context.reply(newLocale["commands.misc.language.languageChanged", "`${localeId}`"], "\uD83C\uDFA4")
        else
            context.reply(newLocale["commands.misc.language.serverLanguageChanged", "`${localeId}`"], "\uD83C\uDFA4")
    }

    private suspend fun buildLanguageEmbed(locale: BaseLocale, languages: List<LocaleWrapper>, isPrivateChannel: Boolean, hasPersonalLanguage: Boolean): MessageEmbed {
        val embed = EmbedBuilder()
        embed.setColor(Color(0, 193, 223))
        embed.setTitle("\uD83C\uDF0E " + locale["commands.misc.language.pleaseSelectYourLanguage"])

        if (isPrivateChannel) {
            embed.setDescription(locale["commands.misc.language.changeLanguageDescription"])
        } else {
            embed.setDescription(locale["commands.misc.language.changeServerLanguageDescription"])
            embed.setFooter(locale["commands.misc.language.personalLanguageTip"])
        }

        if (hasPersonalLanguage)
            embed.setFooter(locale["commands.misc.language.personalLanguageRemovalTip", resetPersonalLanguageEmote])

        for (wrapper in languages) {
            val translators = wrapper.locale.getList("loritta.translationAuthors").mapNotNull { lorittaShards.retrieveUserInfoById(it.toLong()) }

            embed.addField(
                    wrapper.emoteName + " " + wrapper.name,
                    "**${locale["commands.misc.language.translatedBy"]}:** ${translators.joinToString(transform = { "`${it.name}`" })}",
                    true
            )
        }
        embed.addField(
                locale["commands.misc.language.helpUsTranslate"],
                locale["commands.misc.language.crowdinLink"],
                false
        )
        return embed.build()
    }

    private class LocaleWrapper(
            val name: String,
            val locale: BaseLocale,
            val legacyLocale: LegacyBaseLocale,
            val emoteName: String
    )
}