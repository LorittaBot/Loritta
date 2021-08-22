package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.images.ImageReference

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun string(name: String, description: StringI18nData) = argument<String>(
        CommandOptionType.String,
        name,
        description
    )

    fun optionalString(name: String, description: StringI18nData) = argument<String?>(
        CommandOptionType.NullableString,
        name,
        description
    )

    fun integer(name: String, description: StringI18nData) = argument<Int>(
        CommandOptionType.Integer,
        name,
        description
    )

    fun optionalInteger(name: String, description: StringI18nData) = argument<Int?>(
        CommandOptionType.NullableInteger,
        name,
        description
    )

    fun number(name: String, description: StringI18nData) = argument<Double>(
        CommandOptionType.Number,
        name,
        description
    )

    fun optionalNumber(name: String, description: StringI18nData) = argument<Double?>(
        CommandOptionType.NullableNumber,
        name,
        description
    )

    fun boolean(name: String, description: StringI18nData) = argument<Boolean>(
        CommandOptionType.Bool,
        name,
        description
    )

    fun optionalBoolean(name: String, description: StringI18nData) = argument<Boolean?>(
        CommandOptionType.NullableBool,
        name,
        description
    )

    fun user(name: String, description: StringI18nData) = argument<User>(
        CommandOptionType.User,
        name,
        description
    )

    fun optionalUser(name: String, description: StringI18nData) = argument<User?>(
        CommandOptionType.NullableUser,
        name,
        description
    )

    fun stringList(name: String, description: StringI18nData, minimum: Int? = null, maximum: Int? = null) = ListCommandOptionBuilder<List<String>>(
        CommandOptionType.StringList,
        name,
        description,
        minimum,
        maximum
    )

    fun imageReference(name: String, description: StringI18nData) = argument<ImageReference>(
        CommandOptionType.ImageReference,
        name,
        description
    )

    fun <T> argument(type: CommandOptionType, name: String, description: StringI18nData) = CommandOptionBuilder<T>(
        type,
        name,
        description,
        mutableListOf()
    )

    fun <T> CommandOptionBuilder<T>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = CommandOption(
            this.type,
            this.name,
            this.description,
            this.choices
        )

        arguments.add(option)
        return option
    }

    fun <T> ListCommandOptionBuilder<T>.register(): ListCommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = ListCommandOption<T>(
            this.type,
            this.name,
            this.description,
            this.minimum,
            this.maximum
        )

        arguments.add(option)
        return option
    }
}