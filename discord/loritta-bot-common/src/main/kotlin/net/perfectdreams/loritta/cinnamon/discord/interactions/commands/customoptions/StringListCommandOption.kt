package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions

import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordInteraction
import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import net.perfectdreams.discordinteraktions.common.commands.options.CommandOptionBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.InteraKTionsCommandOption
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils

// A custom "String List" command option implementation, showing off how to implement your own custom options
// While for your code it looks like a List<String>, "behind the scenes" it is actually multiple string options

// ===[ OPTION ]===
class StringListCommandOption(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    val minimum: Int, // How many options are required
    val maximum: Int // Maximum options generated
) : InteraKTionsCommandOption<List<String>> {
    val description = languageManager.defaultI18nContext.get(descriptionI18n)
        .shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)
    val descriptionLocalizations =
        SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, descriptionI18n)

    override fun register(builder: BaseInputChatBuilder) {
        for (it in 1..maximum) {
            builder.string("${name}$it", description) {
                this.descriptionLocalizations = this@StringListCommandOption.descriptionLocalizations?.toMutableMap()
                this.required = minimum >= it
            }
        }
    }

    override fun parse(
        kord: Kord,
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): List<String> {
        val listsValues = args.filter { opt -> opt.name.startsWith(name) }

        return listsValues.map { it.value as String }
    }
}

// ===[ BUILDER ]===
class StringListCommandOptionBuilder(
    val languageManager: LanguageManager,
    override val name: String,
    val descriptionI18n: StringI18nData,
    private val minimum: Int,
    private val maximum: Int
) : CommandOptionBuilder<List<String>, List<String>>() {
    override val required = true

    override fun build() = StringListCommandOption(
        languageManager,
        name,
        descriptionI18n,
        minimum,
        maximum
    )
}