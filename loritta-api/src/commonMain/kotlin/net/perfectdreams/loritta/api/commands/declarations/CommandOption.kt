package net.perfectdreams.loritta.api.commands.declarations

import net.perfectdreams.loritta.utils.locale.LocaleKeyData
import kotlin.jvm.JvmName

data class CommandOption<T>(
    val type: Int,
    val name: String,
    val description: LocaleKeyData,
    val required: Boolean,
    val choices: List<CommandChoice<*>>
)

// ===[ REQUIRED ]===
// The reason it is all extension methods is because there isn't a way to convert a "<T?>" to a non nullable "<T>"
// And all of them have "@JvmName" even tho we don't support Java is due to type name clash
@JvmName("requiredString?")
fun CommandOption<String?>.required() = CommandOption<String>(
    type,
    name,
    description,
    true,
    choices
)

@JvmName("requiredInt?")
fun CommandOption<Int?>.required() = CommandOption<Int>(
    type,
    name,
    description,
    true,
    choices
)

@JvmName("requiredBoolean?")
fun CommandOption<Boolean?>.required() = CommandOption<Boolean>(
    type,
    name,
    description,
    true,
    choices
)

// ===[ CHOICE ]===
@JvmName("choiceString")
fun CommandOption<String>.choice(value: String, name: String) = CommandOption<String?>(
    type,
    this.name,
    description,
    required,
    choices + StringCommandChoice(name, value)
)

@JvmName("choiceString?")
fun CommandOption<String?>.choice(value: String, name: String) = CommandOption<String?>(
    type,
    this.name,
    description,
    required,
    choices + StringCommandChoice(name, value)
)

@JvmName("choiceInt")
fun CommandOption<Int>.choice(value: Int, name: String) = CommandOption<Int>(
    type,
    this.name,
    description,
    required,
    choices + IntegerCommandChoice(name, value)
)


@JvmName("choiceInt?")
fun CommandOption<Int?>.choice(value: Int, name: String) = CommandOption<Int>(
    type,
    this.name,
    description,
    required,
    choices + IntegerCommandChoice(name, value)
)