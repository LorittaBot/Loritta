package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory

@Serializable
sealed class ApplicationCommandInfo {
    abstract val name: StringI18nData
}

@Serializable
data class SlashCommandInfo(
    override val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val executorClazz: String?,
    var isGuildOnly: Boolean,
    val subcommands: List<SlashCommandInfo>,
    val subcommandGroups: List<SlashCommandGroupInfo>
) : ApplicationCommandInfo()

@Serializable
data class SlashCommandGroupInfo(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val subcommands: List<SlashCommandInfo>
)