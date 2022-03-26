package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.discordinteraktions.common.entities.Channel
import net.perfectdreams.discordinteraktions.common.entities.Role
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object: ApplicationCommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun string(name: String, description: StringI18nData) = StringCommandOptionBuilder<String>(
        name,
        description,
        true
    )

    fun optionalString(name: String, description: StringI18nData) = StringCommandOptionBuilder<String?>(
        name,
        description,
        false
    )

    fun integer(name: String, description: StringI18nData) = IntegerCommandOptionBuilder<Long>(
        name,
        description,
        true
    )

    fun optionalInteger(name: String, description: StringI18nData) = IntegerCommandOptionBuilder<Long?>(
        name,
        description,
        false
    )

    fun number(name: String, description: StringI18nData) = NumberCommandOptionBuilder<Double>(
        name,
        description,
        true
    )

    fun optionalNumber(name: String, description: StringI18nData) = NumberCommandOptionBuilder<Double?>(
        name,
        description,
        false
    )

    fun boolean(name: String, description: StringI18nData) = BooleanCommandOptionBuilder<Boolean>(
        name,
        description,
        true
    )

    fun optionalBoolean(name: String, description: StringI18nData) = BooleanCommandOptionBuilder<Boolean?>(
        name,
        description,
        false
    )

    fun user(name: String, description: StringI18nData) = UserCommandOptionBuilder<User>(
        name,
        description,
        true
    )

    fun optionalUser(name: String, description: StringI18nData) = UserCommandOptionBuilder<User?>(
        name,
        description,
        false
    )

    fun channel(name: String, description: StringI18nData) = ChannelCommandOptionBuilder<Channel>(
        name,
        description,
        true
    )

    fun optionalChannel(name: String, description: StringI18nData) = ChannelCommandOptionBuilder<Channel?>(
        name,
        description,
        false
    )

    fun role(name: String, description: StringI18nData) = RoleCommandOptionBuilder<Role>(
        name,
        description,
        true
    )

    fun optionalRole(name: String, description: StringI18nData) = RoleCommandOptionBuilder<Role?>(
        name,
        description,
        false
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
        description,
        true
    )

    fun <T, ChoiceableType> CommandOptionBuilder<T, ChoiceableType>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument \"${this.name}\"!")

        val option = this.build()

        arguments.add(option)
        return option
    }
}