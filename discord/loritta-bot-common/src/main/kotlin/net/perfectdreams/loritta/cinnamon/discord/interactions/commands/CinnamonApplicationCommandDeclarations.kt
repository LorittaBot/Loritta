package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils
import net.perfectdreams.loritta.common.commands.CommandCategory

class CinnamonSlashCommandDeclaration(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val nameI18n: StringI18nData,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    override val executor: SlashCommandExecutor?,
    override val defaultMemberPermissions: Permissions?,
    override val dmPermission: Boolean?,
    override val subcommands: List<SlashCommandDeclaration>,
    override val subcommandGroups: List<SlashCommandGroupDeclaration>,
) : SlashCommandDeclaration() {
    override val name = languageManager.defaultI18nContext.get(nameI18n)
    override val nameLocalizations: Map<Locale, String> = SlashTextUtils.createLocalizedStringMapExcludingDefaultLocale(languageManager, nameI18n)
    override val description = SlashTextUtils.buildDescription(languageManager.defaultI18nContext, descriptionI18n, category)
    override val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, descriptionI18n, category)
}

class CinnamonSlashCommandGroupDeclaration(
    val languageManager: LanguageManager,
    val nameI18n: StringI18nData,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    override val subcommands: List<SlashCommandDeclaration>
) : SlashCommandGroupDeclaration() {
    override val name = languageManager.defaultI18nContext.get(nameI18n)
    override val nameLocalizations: Map<Locale, String> = SlashTextUtils.createLocalizedStringMapExcludingDefaultLocale(languageManager, nameI18n)
    override val description = SlashTextUtils.buildDescription(languageManager.defaultI18nContext, descriptionI18n, category)
    override val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, descriptionI18n, category)
}

class CinnamonUserCommandDeclaration(
    val declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val nameI18n: StringI18nData,
    override val defaultMemberPermissions: Permissions?,
    override val dmPermission: Boolean?,
    override val executor: UserCommandExecutor // User/Message commands always requires an executor, that's why it is not nullable!
) : UserCommandDeclaration() {
    override val name = SlashTextUtils.shorten(languageManager.defaultI18nContext.get(nameI18n))
    override val nameLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, nameI18n)
}

class CinnamonMessageCommandDeclaration(
    val declarationWrapper: CinnamonMessageCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val nameI18n: StringI18nData,
    override val defaultMemberPermissions: Permissions?,
    override val dmPermission: Boolean?,
    override val executor: MessageCommandExecutor // User/Message commands always requires an executor, that's why it is not nullable!
) : MessageCommandDeclaration() {
    override val name = SlashTextUtils.shorten(languageManager.defaultI18nContext.get(nameI18n))
    override val nameLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, nameI18n)
}