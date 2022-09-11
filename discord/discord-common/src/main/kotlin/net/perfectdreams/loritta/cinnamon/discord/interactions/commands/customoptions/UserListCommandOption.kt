package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions

import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.UserData
import dev.kord.core.entity.User
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import net.perfectdreams.discordinteraktions.common.commands.options.CommandOptionBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.InteraKTionsCommandOption
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils

// ===[ OPTION ]===
class UserListCommandOption(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    val minimum: Int, // How many options are required
    val maximum: Int // Maximum options generated
) : InteraKTionsCommandOption<List<User>> {
    val description = languageManager.defaultI18nContext.get(descriptionI18n).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)
    val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, descriptionI18n)

    override fun register(builder: BaseInputChatBuilder) {
        for (it in 1..maximum) {
            builder.user("${name}$it", description) {
                this.descriptionLocalizations = this@UserListCommandOption.descriptionLocalizations?.toMutableMap()
                this.required = minimum >= it
            }
        }
    }

    override fun parse(
        kord: Kord,
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): List<User> {
        val listsValues = args.filter { opt -> opt.name.startsWith(name) }

        val foundSnowflakes = listsValues.map { it.value as Snowflake }

        val users = interaction
            .data
            .resolved
            .value
            ?.users
            ?.value ?: return emptyList()

        return foundSnowflakes.map {
            User(UserData.from(users[it]!!), kord)
        }
    }
}

// ===[ BUILDER ]===
class UserListCommandOptionBuilder(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    private val minimum: Int,
    private val maximum: Int
) : CommandOptionBuilder<List<User>, List<User>>() {
    override val required = true

    override fun build() = UserListCommandOption(
        languageManager,
        name,
        descriptionI18n,
        minimum,
        maximum
    )
}