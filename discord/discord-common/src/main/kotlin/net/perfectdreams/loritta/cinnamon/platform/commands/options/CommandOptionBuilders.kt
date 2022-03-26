package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.discordinteraktions.common.entities.Channel
import net.perfectdreams.discordinteraktions.common.entities.Role
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.images.ImageReference
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutorDeclaration

sealed class CommandOptionBuilder<T, ChoiceableType>(
    val name: String,
    val description: StringI18nData,
) {
    abstract fun build(): CommandOption<T>
}

sealed class ChoiceableCommandOptionBuilder<T, ChoiceableType>(
    name: String,
    description: StringI18nData
) : CommandOptionBuilder<T, ChoiceableType>(name, description) {
    val choices: MutableList<CommandChoice<ChoiceableType>> = mutableListOf()
    var autocompleteExecutorDeclaration: AutocompleteExecutorDeclaration<ChoiceableType>? = null

    fun choice(value: ChoiceableType, name: StringI18nData): ChoiceableCommandOptionBuilder<T, ChoiceableType> {
        if (this.autocompleteExecutorDeclaration != null)
            error("You can't use pre-defined choices with an autocomplete executor set!")

        choices.add(buildLocalizedCommandChoice(value, name))

        return this
    }

    fun choice(value: ChoiceableType, name: String): ChoiceableCommandOptionBuilder<T, ChoiceableType> {
        if (this.autocompleteExecutorDeclaration != null)
            error("You can't use pre-defined choices with an autocomplete executor set!")

        choices.add(buildRawCommandChoice(value, name))

        return this
    }

    fun autocomplete(autocompleteExecutorDeclaration: AutocompleteExecutorDeclaration<ChoiceableType>): ChoiceableCommandOptionBuilder<T, ChoiceableType> {
        if (this.choices.isNotEmpty())
            error("You can't use an autocomplete executor with pre-defined choices set!")

        this.autocompleteExecutorDeclaration = autocompleteExecutorDeclaration

        return this
    }

    abstract fun buildLocalizedCommandChoice(value: ChoiceableType, name: StringI18nData): LocalizedCommandChoice<ChoiceableType>
    abstract fun buildRawCommandChoice(value: ChoiceableType, name: String): RawCommandChoice<ChoiceableType>
}

// ===[ STRING ]===
class StringCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<String, String>(name, description) {
    override fun buildLocalizedCommandChoice(value: String, name: StringI18nData) = LocalizedStringCommandChoice(name, value)
    override fun buildRawCommandChoice(value: String, name: String) = RawStringCommandChoice(name, value)

    override fun build() = StringCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

class NullableStringCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<String?, String>(name, description) {
    override fun buildLocalizedCommandChoice(value: String, name: StringI18nData) = LocalizedStringCommandChoice(name, value)
    override fun buildRawCommandChoice(value: String, name: String) = RawStringCommandChoice(name, value)

    override fun build() = NullableStringCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ INTEGER ]===
class IntegerCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<Long, Long>(name, description) {
    override fun buildLocalizedCommandChoice(value: Long, name: StringI18nData) = LocalizedIntegerCommandChoice(name, value)
    override fun buildRawCommandChoice(value: Long, name: String) = RawIntegerCommandChoice(name, value)

    override fun build() = IntegerCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

class NullableIntegerCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<Long?, Long>(name, description) {
    override fun buildLocalizedCommandChoice(value: Long, name: StringI18nData) = LocalizedIntegerCommandChoice(name, value)
    override fun buildRawCommandChoice(value: Long, name: String) = RawIntegerCommandChoice(name, value)

    override fun build() = NullableIntegerCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ NUMBER ]===
class NumberCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<Double, Double>(name, description) {
    override fun buildLocalizedCommandChoice(value: Double, name: StringI18nData) = LocalizedNumberCommandChoice(name, value)
    override fun buildRawCommandChoice(value: Double, name: String) = RawNumberCommandChoice(name, value)

    override fun build() = NumberCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

class NullableNumberCommandOptionBuilder(name: String, description: StringI18nData) : ChoiceableCommandOptionBuilder<Double?, Double>(name, description) {
    override fun buildLocalizedCommandChoice(value: Double, name: StringI18nData) = LocalizedNumberCommandChoice(name, value)
    override fun buildRawCommandChoice(value: Double, name: String) = RawNumberCommandChoice(name, value)

    override fun build() = NullableNumberCommandOption(
        name,
        description,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ BOOLEAN ]===
class BooleanCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Boolean, Boolean>(name, description) {
    override fun build() = BooleanCommandOption(name, description)
}

class NullableBooleanCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Boolean?, Boolean>(name, description) {
    override fun build() = NullableBooleanCommandOption(name, description)
}

// ===[ USER ]===
class UserCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<User, User>(name, description) {
    override fun build() = UserCommandOption(name, description)
}

class NullableUserCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<User?, User>(name, description) {
    override fun build() = NullableUserCommandOption(name, description)
}

// ===[ CHANNEL ]===
class ChannelCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Channel, Channel>(name, description) {
    override fun build() = ChannelCommandOption(name, description)
}

class NullableChannelCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Channel?, Channel>(name, description) {
    override fun build() = NullableChannelCommandOption(name, description)
}

// ===[ ROLE ]===
class RoleCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Role, Role>(name, description) {
    override fun build() = RoleCommandOption(name, description)
}

class NullableRoleCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<Role?, Role>(name, description) {
    override fun build() = NullableRoleCommandOption(name, description)
}

// Stuff that isn't present in Discord Slash Commands yet
// (After all, this CommandOptionType is based of Discord InteraKTions implementation! :3)
class StringListCommandOptionBuilder(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) : CommandOptionBuilder<List<String>, List<String>>(name, description) {
    override fun build() = StringListCommandOption(name, description, minimum, maximum)
}

class UserListCommandOptionBuilder(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) : CommandOptionBuilder<List<User>, List<User>>(name, description) {
    override fun build() = UserListCommandOption(name, description, minimum, maximum)
}

class ImageReferenceCommandOptionBuilder(name: String, description: StringI18nData) : CommandOptionBuilder<ImageReference, ImageReference>(name, description) {
    override fun build() = ImageReferenceCommandOption(name, description)
}