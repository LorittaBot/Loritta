package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object: ApplicationCommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun string(name: String, description: StringI18nData) = StringCommandOptionBuilder(
        name,
        description
    )

    fun optionalString(name: String, description: StringI18nData) = NullableStringCommandOptionBuilder(
        name,
        description
    )

    fun integer(name: String, description: StringI18nData) = IntegerCommandOptionBuilder(
        name,
        description
    )

    fun optionalInteger(name: String, description: StringI18nData) = NullableIntegerCommandOptionBuilder(
        name,
        description
    )

    fun number(name: String, description: StringI18nData) = NumberCommandOptionBuilder(
        name,
        description
    )

    fun optionalNumber(name: String, description: StringI18nData) = NullableNumberCommandOptionBuilder(
        name,
        description
    )

    fun boolean(name: String, description: StringI18nData) = BooleanCommandOptionBuilder(
        name,
        description
    )

    fun optionalBoolean(name: String, description: StringI18nData) = NullableBooleanCommandOptionBuilder(
        name,
        description
    )

    fun user(name: String, description: StringI18nData) = UserCommandOptionBuilder(
        name,
        description
    )

    fun optionalUser(name: String, description: StringI18nData) = NullableUserCommandOptionBuilder(
        name,
        description
    )

    fun channel(name: String, description: StringI18nData) = ChannelCommandOptionBuilder(
        name,
        description
    )

    fun optionalChannel(name: String, description: StringI18nData) = NullableChannelCommandOptionBuilder(
        name,
        description
    )

    fun role(name: String, description: StringI18nData) = RoleCommandOptionBuilder(
        name,
        description
    )

    fun optionalRole(name: String, description: StringI18nData) = NullableRoleCommandOptionBuilder(
        name,
        description
    )

    fun stringList(name: String, description: StringI18nData, minimum: Int? = null, maximum: Int? = null) = StringListCommandOptionBuilder(
        name,
        description,
        minimum,
        maximum
    )

    fun userList(name: String, description: StringI18nData, minimum: Int? = null, maximum: Int? = null) = UserListCommandOptionBuilder(
        name,
        description,
        minimum,
        maximum
    )

    fun imageReference(name: String, description: StringI18nData) = ImageReferenceCommandOptionBuilder(
        name,
        description
    )

    fun <T, ChoiceableType> CommandOptionBuilder<T, ChoiceableType>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument \"${this.name}\"!")

        val option = this.build()

        arguments.add(option)
        return option
    }
}