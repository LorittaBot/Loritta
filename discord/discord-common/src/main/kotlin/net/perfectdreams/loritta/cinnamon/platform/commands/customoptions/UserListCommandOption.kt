package net.perfectdreams.loritta.cinnamon.platform.commands.customoptions

import dev.kord.common.Locale
import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.mapValues
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.platforms.kord.entities.KordUser

// ===[ OPTION ]===
class UserListCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>?,
    descriptionLocalizations: Map<Locale, String>?,
    val minimum: Int, // How many options are required
    val maximum: Int // Maximum options generated
) : NameableCommandOption<List<User>>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun register(builder: BaseInputChatBuilder) {
        for (it in 1..maximum) {
            builder.user("${name}$it", description) {
                this.nameLocalizations = this@UserListCommandOption.nameLocalizations?.toMutableMap()
                this.descriptionLocalizations = this@UserListCommandOption.descriptionLocalizations?.toMutableMap()
                this.required = minimum >= it
            }
        }
    }

    override fun parse(
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): List<User> {
        val listsValues = args.filter { opt -> opt.name.startsWith(name) }

        val foundSnowflakes = listsValues.map { it.value as Snowflake }

        return interaction
            .data
            .resolved
            .value
            ?.users
            ?.value
            ?.filterKeys { it in foundSnowflakes }
            ?.values
            ?.map {
                KordUser(it)
            } ?: emptyList()
    }
}

// ===[ BUILDER ]===
class UserListCommandOptionBuilder(
    name: String,
    description: String,
    private val minimum: Int,
    private val maximum: Int
) : CommandOptionBuilder<List<User>, List<User>>(name, description, true) {
    override fun build() = UserListCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        minimum,
        maximum
    )
}