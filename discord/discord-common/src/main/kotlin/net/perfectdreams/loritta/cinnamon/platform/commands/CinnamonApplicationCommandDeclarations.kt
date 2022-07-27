package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.platform.utils.SlashTextUtils

class CinnamonSlashCommandDeclaration(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    override val executor: SlashCommandExecutor?,
    override val defaultMemberPermissions: Permissions?,
    override val dmPermission: Boolean?,
    override val subcommands: List<SlashCommandDeclaration>,
    override val subcommandGroups: List<SlashCommandGroupDeclaration>,
) : SlashCommandDeclaration() {
    override val description = SlashTextUtils.shorten(languageManager.defaultI18nContext.get(descriptionI18n))
    override val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, descriptionI18n, category)
    override val nameLocalizations: Map<Locale, String> = emptyMap()
}

class CinnamonSlashCommandGroupDeclaration(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    override val subcommands: List<SlashCommandDeclaration>
) : SlashCommandGroupDeclaration() {
    override val description = SlashTextUtils.shorten(languageManager.defaultI18nContext.get(descriptionI18n))
    override val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, descriptionI18n, category)
    override val nameLocalizations: Map<Locale, String> = emptyMap()
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