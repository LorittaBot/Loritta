package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options

import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.discordinteraktions.common.entities.Channel
import net.perfectdreams.discordinteraktions.common.entities.Role
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager

// ===[ STRING ]===
abstract class LocalizedStringCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : StringCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedStringCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required,
        choices?.map { it.build() },
        minLength,
        maxLength,
        autocompleteExecutor
    )
}

class LocalizedStringCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedStringCommandOptionBuilderBase<String>(languageManager, name, descriptionI18n, true)

class NullableLocalizedStringCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedStringCommandOptionBuilderBase<String?>(languageManager, name, descriptionI18n, false)

// ===[ INTEGER ]===
abstract class LocalizedIntegerCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : IntegerCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedIntegerCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required,
        choices?.map { it.build() },
        minValue,
        maxValue,
        autocompleteExecutor
    )
}

class LocalizedIntegerCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedIntegerCommandOptionBuilderBase<Long>(languageManager, name, descriptionI18n, true)

class NullableLocalizedIntegerCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedIntegerCommandOptionBuilderBase<Long?>(languageManager, name, descriptionI18n, false)

// ===[ NUMBER ]===
abstract class LocalizedNumberCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : NumberCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedNumberCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required,
        choices?.map { it.build() },
        minValue,
        maxValue,
        autocompleteExecutor
    )
}

class LocalizedNumberCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedNumberCommandOptionBuilderBase<Double>(languageManager, name, descriptionI18n, true)

class NullableLocalizedNumberCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedNumberCommandOptionBuilderBase<Double?>(languageManager, name, descriptionI18n, false)

// ===[ USER ]===
abstract class LocalizedUserCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : UserCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedUserCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required
    )
}

class LocalizedUserCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedUserCommandOptionBuilderBase<User>(languageManager, name, descriptionI18n, true)

class NullableLocalizedUserCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedUserCommandOptionBuilderBase<User?>(languageManager, name, descriptionI18n, false)

// ===[ CHANNEL ]===
abstract class LocalizedChannelCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : ChannelCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedChannelCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required,
        channelTypes
    )
}

class LocalizedChannelCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedChannelCommandOptionBuilderBase<Channel>(languageManager, name, descriptionI18n, true)

class NullableLocalizedChannelCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedChannelCommandOptionBuilderBase<Channel?>(languageManager, name, descriptionI18n, false)

// ===[ ROLE ]===
abstract class LocalizedRoleCommandOptionBuilderBase<T>(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    override val required: Boolean
) : RoleCommandOptionBuilderBase<T>() {
    override val description: String
        get() = error("String description is not supported in a ${this::class.simpleName}!")

    override fun build() = LocalizedRoleCommandOption(
        languageManager,
        name,
        descriptionI18n,
        required
    )
}

class LocalizedRoleCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedRoleCommandOptionBuilderBase<Role>(languageManager, name, descriptionI18n, true)

class NullableLocalizedRoleCommandOptionBuilder(
    languageManager: LanguageManager,
    name: String,
    descriptionI18n: StringI18nData
) : LocalizedRoleCommandOptionBuilderBase<Role?>(languageManager, name, descriptionI18n, false)