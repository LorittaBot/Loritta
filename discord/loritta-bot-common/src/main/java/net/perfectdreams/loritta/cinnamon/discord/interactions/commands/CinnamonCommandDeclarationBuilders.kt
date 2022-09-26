package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.utils.InteraKTionsDslMarker
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils

// ===[ SLASH COMMANDS ]===
fun slashCommand(
    declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    languageManager: LanguageManager,
    name: StringI18nData,
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
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory
) {
    var executor: ((LorittaCinnamon) -> SlashCommandExecutor)? = null
    // var executor: SlashCommandExecutor? = null
    val subcommands = mutableListOf<CinnamonSlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CinnamonSlashCommandGroupDeclarationBuilder>()
    // Only root commands can have permissions and dmPermission
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun subcommand(name: StringI18nData, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CinnamonSlashCommandDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun subcommandGroup(name: StringI18nData, description: StringI18nData, block: CinnamonSlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CinnamonSlashCommandGroupDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun build(loritta: LorittaCinnamon): SlashCommandDeclaration {
        return CinnamonSlashCommandDeclaration(
            declarationWrapper,
            languageManager,
            name,
            description,
            category,
            executor?.invoke(loritta),
            defaultMemberPermissions,
            dmPermission,
            subcommands.map { it.build(loritta) },
            subcommandGroups.map { it.build(loritta) }
        )
    }
}

@InteraKTionsDslMarker
class CinnamonSlashCommandGroupDeclarationBuilder(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<CinnamonSlashCommandDeclarationBuilder>()

    fun subcommand(name: StringI18nData, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CinnamonSlashCommandDeclarationBuilder(declarationWrapper, languageManager, name, description, category).apply(block)
    }

    fun build(loritta: LorittaCinnamon): SlashCommandGroupDeclaration {
        return CinnamonSlashCommandGroupDeclaration(
            languageManager,
            name,
            description,
            category,
            subcommands.map { it.build(loritta) }
        )
    }
}

// ===[ USER COMMANDS ]===
fun userCommand(
    declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    languageManager: LanguageManager,
    name: StringI18nData,
    executor: (LorittaCinnamon) -> (UserCommandExecutor),
    block: CinnamonUserCommandDeclarationBuilder.() -> (Unit) = {}
) = CinnamonUserCommandDeclarationBuilder(declarationWrapper, languageManager, name, executor)
    .apply(block)

@InteraKTionsDslMarker
class CinnamonUserCommandDeclarationBuilder(
    val declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: StringI18nData,
    val executor: (LorittaCinnamon) -> (UserCommandExecutor)
) {
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun build(loritta: LorittaCinnamon): CinnamonUserCommandDeclaration {
        return CinnamonUserCommandDeclaration(
            declarationWrapper,
            languageManager,
            name,
            defaultMemberPermissions,
            dmPermission,
            executor.invoke(loritta)
        )
    }
}

// ===[ MESSAGE COMMANDS ]===
fun messageCommand(
    declarationWrapper: CinnamonMessageCommandDeclarationWrapper,
    languageManager: LanguageManager,
    name: StringI18nData,
    executor: (LorittaCinnamon) -> (MessageCommandExecutor),
    block: CinnamonMessageCommandDeclarationBuilder.() -> (Unit) = {}
) = CinnamonMessageCommandDeclarationBuilder(declarationWrapper, languageManager, name, executor)
    .apply(block)

@InteraKTionsDslMarker
class CinnamonMessageCommandDeclarationBuilder(
    val declarationWrapper: CinnamonMessageCommandDeclarationWrapper,
    val languageManager: LanguageManager,
    val name: StringI18nData,
    val executor: (LorittaCinnamon) -> (MessageCommandExecutor)
) {
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

    fun build(loritta: LorittaCinnamon): CinnamonMessageCommandDeclaration {
        return CinnamonMessageCommandDeclaration(
            declarationWrapper,
            languageManager,
            name,
            defaultMemberPermissions,
            dmPermission,
            executor.invoke(loritta)
        )
    }
}