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
    val required: Boolean
) {
    abstract fun build(): CommandOption<T>
}

sealed class ChoiceableCommandOptionBuilder<T, ChoiceableType>(
    name: String,
    description: StringI18nData,
    required: Boolean
) : CommandOptionBuilder<T, ChoiceableType>(name, description, required) {
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

    abstract fun buildLocalizedCommandChoice(
        value: ChoiceableType,
        name: StringI18nData
    ): LocalizedCommandChoice<ChoiceableType>

    abstract fun buildRawCommandChoice(value: ChoiceableType, name: String): RawCommandChoice<ChoiceableType>
}

// ===[ STRING ]===
class StringCommandOptionBuilder<T : String?>(name: String, description: StringI18nData, required: Boolean) :
    ChoiceableCommandOptionBuilder<T, String>(name, description, required) {
    override fun buildLocalizedCommandChoice(value: String, name: StringI18nData) =
        LocalizedStringCommandChoice(name, value)

    override fun buildRawCommandChoice(value: String, name: String) = RawStringCommandChoice(name, value)

    override fun build() = StringCommandOption<T>(
        name,
        description,
        required,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ INTEGER ]===
class IntegerCommandOptionBuilder<T : Long?>(name: String, description: StringI18nData, required: Boolean) :
    ChoiceableCommandOptionBuilder<T, Long>(name, description, required) {
    override fun buildLocalizedCommandChoice(value: Long, name: StringI18nData) =
        LocalizedIntegerCommandChoice(name, value)

    override fun buildRawCommandChoice(value: Long, name: String) = RawIntegerCommandChoice(name, value)

    override fun build() = IntegerCommandOption<T>(
        name,
        description,
        required,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ NUMBER ]===
class NumberCommandOptionBuilder<T : Double?>(name: String, description: StringI18nData, required: Boolean) :
    ChoiceableCommandOptionBuilder<T, Double>(name, description, required) {
    override fun buildLocalizedCommandChoice(value: Double, name: StringI18nData) =
        LocalizedNumberCommandChoice(name, value)

    override fun buildRawCommandChoice(value: Double, name: String) = RawNumberCommandChoice(name, value)

    override fun build() = NumberCommandOption<T>(
        name,
        description,
        required,
        choices,
        autocompleteExecutorDeclaration
    )
}

// ===[ BOOLEAN ]===
class BooleanCommandOptionBuilder<T: Boolean?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<T, Boolean>(name, description, required) {
    override fun build() = BooleanCommandOption<T>(name, description, required)
}

// ===[ USER ]===
class UserCommandOptionBuilder<T: User?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<T, User>(name, description, required) {
    override fun build() = UserCommandOption<T>(name, description, required)
}

// ===[ CHANNEL ]===
class ChannelCommandOptionBuilder<T: Channel?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<T, Channel>(name, description, required) {
    override fun build() = ChannelCommandOption<T>(name, description, required)
}

// ===[ ROLE ]===
class RoleCommandOptionBuilder<T: Role?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<T, Role>(name, description, true) {
    override fun build() = RoleCommandOption<T>(name, description, required)
}

// Stuff that isn't present in Discord Slash Commands yet
// (After all, this CommandOptionType is based of Discord InteraKTions implementation! :3)
class StringListCommandOptionBuilder(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) :
    CommandOptionBuilder<List<String>, List<String>>(name, description, true) {
    override fun build() = StringListCommandOption(name, description, minimum, maximum)
}

class UserListCommandOptionBuilder(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) :
    CommandOptionBuilder<List<User>, List<User>>(name, description, true) {
    override fun build() = UserListCommandOption(name, description, minimum, maximum)
}

class ImageReferenceCommandOptionBuilder(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<ImageReference, ImageReference>(name, description, required) {
    override fun build() = ImageReferenceCommandOption(name, description, required)
}

class ImageReferenceOrAttachmentCommandOptionBuilder(name: String, description: StringI18nData, required: Boolean) :
    CommandOptionBuilder<ImageReference, ImageReference>(name, description, required) {
    override fun build() = ImageReferenceOrAttachmentCommandOption(name, description, required)
}