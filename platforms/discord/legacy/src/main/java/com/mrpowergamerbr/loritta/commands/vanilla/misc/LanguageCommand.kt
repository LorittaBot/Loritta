package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import java.awt.Color

class LanguageCommand : AbstractCommand("language", listOf("linguagem", "speak", "lang"), category = CommandCategory.MISC) {
    private val resetPersonalLanguageEmote = "\uD83D\uDE45"

    override fun getDescriptionKey() = LocaleKeyData(
            "commands.command.language.description",
            listOf(
                    LocaleStringData("\uD83D\uDE0A")
            )
    )

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.MANAGE_SERVER)
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val profile = loritta.getOrCreateLorittaProfile(context.userHandle.idLong)

        val hasPersonalLanguage = loritta.newSuspendedTransaction { profile.settings.language != null && context.isPrivateChannel }

        val embed = EmbedBuilder()
        embed.setColor(Color(0, 193, 223))

        val validLanguages = listOf(
                LocaleWrapper(
                        "Português-Brasil",
                        loritta.localeManager.getLocaleById("default"),
                        "\uD83C\uDDE7\uD83C\uDDF7",
                        false
                ),
                /* LocaleWrapper(
                        "Português-Portugal",
                        loritta.localeManager.getLocaleById("pt-pt"),
                        loritta.getLegacyLocaleById("pt-pt"),
                        "\uD83C\uDDF5\uD83C\uDDF9"
                ), */
                LocaleWrapper(
                        "English (United States)",
                        loritta.localeManager.getLocaleById("en-us"),
                        "\uD83C\uDDFA\uD83C\uDDF8",
                        false
                ),
                LocaleWrapper(
                        "Español",
                        loritta.localeManager.getLocaleById("es"),
                        "\uD83C\uDDEA\uD83C\uDDF8",
                        false
                ),
                LocaleWrapper(
                        "Português-Funk",
                        loritta.localeManager.getLocaleById("pt-funk"),
                        "<:loritta_quebrada:338679008210190336>",
                        true
                ),
                LocaleWrapper(
                        "Português-Furry",
                        loritta.localeManager.getLocaleById("pt-furry"),
                        "\uD83D\uDC3E",
                        true
                ),
                LocaleWrapper(
                        "English-Furry",
                        loritta.localeManager.getLocaleById("en-furry"),
                        "\uD83D\uDC31",
                        true
                )
        )

        if (context.rawArgs.getOrNull(0) == "br-debug") {
            activateLanguage(
                    context,
                    profile,
                    LocaleWrapper(
                            "Auto-PT-BR-Debug",
                            loritta.localeManager.getLocaleById("br-debug"),
                            "\uD83D\uDC31",
                            true
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
                            loritta.localeManager.getLocaleById("en-debug"),
                            "\uD83D\uDC31",
                            true
                    )
            )
            return
        }

        val message = context.sendMessage(
                context.getAsMention(true),
                buildLanguageEmbed(
                        locale,
                        validLanguages.filter { !it.isSecret },
                        context.isPrivateChannel,
                        hasPersonalLanguage
                )
        )

        message.onReactionAddByAuthor(context) {
            if (it.reactionEmote.isEmote("426183783008698391")) {
                message.edit(
                        " ",
                        buildLanguageEmbed(
                                locale,
                                validLanguages.filter { it.isSecret },
                                context.isPrivateChannel,
                                hasPersonalLanguage
                        ),
                        true
                )

                for (wrapper in validLanguages.filter { it.isSecret }) {
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
                                locale["commands.command.language.removedPersonalLanguage"]
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

        for (wrapper in validLanguages.filter { !it.isSecret }) {
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

        val newLocale = loritta.localeManager.getLocaleById(localeId)
        if (localeId == "default") {
            localeId = "pt-br" // Já que nós já salvamos, vamos trocar o localeId para algo mais "decente"
        }

        if (isPrivateChannel)
            context.reply(newLocale["commands.command.language.languageChanged", "`${localeId}`"], "\uD83C\uDFA4")
        else
            context.reply(newLocale["commands.command.language.serverLanguageChanged", "`${localeId}`"], "\uD83C\uDFA4")
    }

    private suspend fun buildLanguageEmbed(locale: BaseLocale, languages: List<LocaleWrapper>, isPrivateChannel: Boolean, hasPersonalLanguage: Boolean): MessageEmbed {
        val embed = EmbedBuilder()
        embed.setColor(Color(0, 193, 223))
        embed.setTitle("\uD83C\uDF0E " + locale["commands.command.language.pleaseSelectYourLanguage"])

        if (isPrivateChannel) {
            embed.setDescription(locale["commands.command.language.changeLanguageDescription"])
        } else {
            embed.setDescription(locale["commands.command.language.changeServerLanguageDescription"])
            embed.setFooter(locale["commands.command.language.personalLanguageTip"])
        }

        if (hasPersonalLanguage)
            embed.setFooter(locale["commands.command.language.personalLanguageRemovalTip", resetPersonalLanguageEmote])

        for (wrapper in languages) {
            val translators = wrapper.locale.getList("loritta.translationAuthors").mapNotNull { lorittaShards.retrieveUserInfoById(it.toLong()) }

            embed.addField(
                    wrapper.emoteName + " " + wrapper.name,
                    "**${locale["commands.command.language.translatedBy"]}:** ${translators.joinToString(transform = { "`${it.name}`" })}",
                    true
            )
        }
        embed.addField(
                locale["commands.command.language.helpUsTranslate"],
                loritta.config.crowdin.url,
                false
        )
        return embed.build()
    }

    private class LocaleWrapper(
        val name: String,
        val locale: BaseLocale,
        val emoteName: String,
        val isSecret: Boolean
    )
}
