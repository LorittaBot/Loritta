package net.perfectdreams.loritta.morenitta.interactions.commands.options

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteExecutor

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
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun choice(name: String, value: String) = choices.add(Choice.RawChoice(name, value))
    fun choice(name: StringI18nData, value: String) = choices.add(Choice.LocalizedChoice(name, value))

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

    override fun get(option: OptionMapping): T {
        return option.asString as T
    }

    sealed class Choice {
        class LocalizedChoice(
            val name: StringI18nData,
            val value: String
        ) : Choice()

        class RawChoice(
            val name: String,
            val value: String
        ) : Choice()
    }
}

class LongDiscordOptionReference<T>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    val requiredRange: LongRange?
) : DiscordOptionReference<T>(name, description, required) {
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

    override fun get(option: OptionMapping): T {
        return option.asLong as T
    }
}

class NumberDiscordOptionReference<T>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    val requiredRange: ClosedFloatingPointRange<Double>?
) : DiscordOptionReference<T>(name, description, required) {
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

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