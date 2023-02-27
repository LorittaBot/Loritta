package net.perfectdreams.loritta.morenitta.interactions.commands.options

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

sealed class OptionReference<T>(
    val name: String
)

sealed class DiscordOptionReference<T>(
    name: String,
    val description: StringI18nData,
    val required: Boolean
) : OptionReference<T>(name) {
    abstract fun get(option: OptionMapping): T
}

class StringDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    val choices = mutableListOf<Choice>()

    fun choice(name: String, value: String) = choices.add(Choice(name, value))

    override fun get(option: OptionMapping): T {
        return option.asString as T
    }

    data class Choice(
        val name: String,
        val value: String
    )
}

class LongDiscordOptionReference<T>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    requiredRange: LongRange?
) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T {
        return option.asLong as T
    }
}

class UserDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T {
        val user = option.asUser
        val member = option.asMember

        return UserAndMember(
            user,
            member
        ) as T
    }
}

data class UserAndMember(
    val user: User,
    val member: Member?
)