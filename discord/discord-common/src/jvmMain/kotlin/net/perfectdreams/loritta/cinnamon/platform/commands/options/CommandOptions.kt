package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.entities.Channel
import net.perfectdreams.discordinteraktions.common.entities.Role
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.images.ImageReference

sealed class CommandOption<T>(
    val name: String,
    val description: StringI18nData
)

interface NullableCommandOption

sealed class ChoiceableCommandOption<T, ChoiceableType>(
    name: String,
    description: StringI18nData,
    val choices: List<CommandChoice<ChoiceableType>>,
    val autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<ChoiceableType>?
) : CommandOption<T>(name, description)

// ===[ STRING ]===
class StringCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<String>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<String>?
) : ChoiceableCommandOption<String, String>(name, description, choices, autoCompleteExecutorDeclaration)

class NullableStringCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<String>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<String>?
) : ChoiceableCommandOption<String?, String>(name, description, choices, autoCompleteExecutorDeclaration), NullableCommandOption

// ===[ INTEGER ]===
class IntegerCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<Long>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Long>?
) : ChoiceableCommandOption<Long, Long>(name, description, choices, autoCompleteExecutorDeclaration)

class NullableIntegerCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<Long>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Long>?
) : ChoiceableCommandOption<Long?, Long>(name, description, choices, autoCompleteExecutorDeclaration), NullableCommandOption

// ===[ NUMBER ]===
class NumberCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<Double>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Double>?
) : ChoiceableCommandOption<Double, Double>(name, description, choices, autoCompleteExecutorDeclaration)

class NullableNumberCommandOption(
    name: String,
    description: StringI18nData,
    choices: List<CommandChoice<Double>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Double>?
) : ChoiceableCommandOption<Double?, Double>(name, description, choices, autoCompleteExecutorDeclaration), NullableCommandOption

// ===[ BOOLEAN ]===
class BooleanCommandOption(name: String, description: StringI18nData) : CommandOption<Boolean>(name, description)
class NullableBooleanCommandOption(name: String, description: StringI18nData) : CommandOption<Boolean?>(name, description), NullableCommandOption

// ===[ USER ]===
class UserCommandOption(name: String, description: StringI18nData) : CommandOption<User>(name, description)
class NullableUserCommandOption(name: String, description: StringI18nData) : CommandOption<User?>(name, description), NullableCommandOption

// ===[ CHANNEL ]===
class ChannelCommandOption(name: String, description: StringI18nData) : CommandOption<Channel>(name, description)
class NullableChannelCommandOption(name: String, description: StringI18nData) : CommandOption<Channel?>(name, description), NullableCommandOption

// ===[ ROLE ]===
class RoleCommandOption(name: String, description: StringI18nData) : CommandOption<Role>(name, description)
class NullableRoleCommandOption(name: String, description: StringI18nData) : CommandOption<Role?>(name, description), NullableCommandOption

// Stuff that isn't present in Discord Slash Commands yet
// (After all, this CommandOptionType is based of Discord InteraKTions implementation! :3)
class StringListCommandOption(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) : CommandOption<List<String>>(name, description)
class UserListCommandOption(name: String, description: StringI18nData, val minimum: Int?, val maximum: Int?) : CommandOption<List<User>>(name, description)
class ImageReferenceCommandOption(name: String, description: StringI18nData) : CommandOption<ImageReference>(name, description)