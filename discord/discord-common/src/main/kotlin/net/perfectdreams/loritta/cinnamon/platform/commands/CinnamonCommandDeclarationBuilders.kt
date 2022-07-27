package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.utils.InteraKTionsDslMarker
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.platform.utils.SlashTextUtils

// ===[ LORITTA COMMANDS ]===
fun slashCommand(
    declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    languageManager: LanguageManager,
    name: String,
    description: StringI18nData,
    category: CommandCategory,
    block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit)
) = CinnamonSlashCommandDeclarationBuilder(
    declarationWrapper,
    languageManager,
    name,
    description,
    category
).apply(block)

@InteraKTionsDslMarker
class CinnamonSlashCommandDeclarationBuilder(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: String,
    val description: StringI18nData,
    val category: CommandCategory
) {
    var executor: SlashCommandExecutor? = null
    val subcommands = mutableListOf<CinnamonSlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CinnamonSlashCommandGroupDeclarationBuilder>()
    // Only root commands can have permissions and dmPermission
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun subcommand(name: String, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CinnamonSlashCommandDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun subcommandGroup(name: String, description: StringI18nData, block: CinnamonSlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CinnamonSlashCommandGroupDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun build(): SlashCommandDeclaration {
        return CinnamonSlashCommandDeclaration(
            declarationWrapper,
            name,
            SlashTextUtils.shorten(languageManager.defaultI18nContext.get(description)),
            SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, description, category),
            description,
            category,
            executor,
            defaultMemberPermissions,
            dmPermission,
            subcommands.map { it.build() },
            subcommandGroups.map { it.build() }
        )
    }
}

@InteraKTionsDslMarker
class CinnamonSlashCommandGroupDeclarationBuilder(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: String,
    val description: StringI18nData,
    val category: CommandCategory
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<CinnamonSlashCommandDeclarationBuilder>()

    fun subcommand(name: String, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CinnamonSlashCommandDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun build(): SlashCommandGroupDeclaration {
        return CinnamonSlashCommandGroupDeclaration(
            name,
            languageManager.defaultI18nContext.get(description),
            SlashTextUtils.createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager, description, category),
            description,
            category,
            subcommands.map { it.build() }
        )
    }
}

// ===[ USER COMMANDS ]===
fun userCommand(
    declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    languageManager: LanguageManager,
    name: StringI18nData,
    executor: UserCommandExecutor,
    block: CinnamonUserCommandDeclarationBuilder.() -> (Unit) = {}
) = CinnamonUserCommandDeclarationBuilder(declarationWrapper, languageManager, name, executor)
    .apply(block)

@InteraKTionsDslMarker
class CinnamonUserCommandDeclarationBuilder(
    val declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: StringI18nData,
    val executor: UserCommandExecutor
) {
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun build(): CinnamonUserCommandDeclaration {
        return CinnamonUserCommandDeclaration(
            declarationWrapper,
            languageManager.defaultI18nContext.get(name),
            SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, name),
            defaultMemberPermissions,
            dmPermission,
            executor
        )
    }
}