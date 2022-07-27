package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.utils.InteraKTionsDslMarker
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.platform.utils.I18nContextUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.SlashTextUtils

// ===[ LORITTA COMMANDS ]===
fun slashCommand(languageManager: LanguageManager, name: String, description: StringI18nData, category: CommandCategory, block: LorittaSlashCommandDeclarationBuilder.() -> (Unit)) = LorittaSlashCommandDeclarationBuilder(
    languageManager,
    name,
    description,
    category
).apply(block)

@InteraKTionsDslMarker
class LorittaSlashCommandDeclarationBuilder(
    val languageManager: LanguageManager,
    val name: String,
    val description: StringI18nData,
    val category: CommandCategory
) {
    var executor: SlashCommandExecutor? = null
    val subcommands = mutableListOf<LorittaSlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<LorittaCommandGroupDeclarationBuilder>()
    // Only root commands can have permissions and dmPermission
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun subcommand(name: String, description: StringI18nData, block: LorittaSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += LorittaSlashCommandDeclarationBuilder(languageManager, name, description, category).apply(block)
    }

    fun subcommandGroup(name: String, description: StringI18nData, block: LorittaCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += LorittaCommandGroupDeclarationBuilder(languageManager, name, description, category).apply(block)
    }

    fun build(): SlashCommandDeclaration {
        return LorittaCommandDeclaration(
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
class LorittaCommandGroupDeclarationBuilder(
    val languageManager: LanguageManager,
    val name: String,
    val description: StringI18nData,
    val category: CommandCategory
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<LorittaSlashCommandDeclarationBuilder>()

    fun subcommand(name: String, description: StringI18nData, block: LorittaSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += LorittaSlashCommandDeclarationBuilder(languageManager, name, description, category).apply(block)
    }

    fun build(): SlashCommandGroupDeclaration {
        return LorittaCommandGroupDeclaration(
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
fun userCommand(languageManager: LanguageManager, name: StringI18nData, executor: UserCommandExecutor, block: LorittaUserCommandDeclarationBuilder.() -> (Unit) = {}) = LorittaUserCommandDeclarationBuilder(languageManager, name, executor)
    .apply(block)

@InteraKTionsDslMarker
class LorittaUserCommandDeclarationBuilder(val languageManager: LanguageManager, val name: StringI18nData, val executor: UserCommandExecutor) {
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            languageManager.defaultI18nContext.get(name),
            SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, name),
            defaultMemberPermissions,
            dmPermission,
            executor
        )
    }
}