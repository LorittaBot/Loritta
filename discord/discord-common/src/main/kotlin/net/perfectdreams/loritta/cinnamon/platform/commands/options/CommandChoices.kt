package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

sealed class CommandChoice<T>(
    val value: T
)

sealed class LocalizedCommandChoice<T>(
    val name: StringI18nData,
    value: T
) : CommandChoice<T>(value)

sealed class RawCommandChoice<T>(
    val name: String,
    value: T
) : CommandChoice<T>(value)

class LocalizedStringCommandChoice(name: StringI18nData, value: String) : LocalizedCommandChoice<String>(name, value)
class LocalizedIntegerCommandChoice(name: StringI18nData, value: Long) : LocalizedCommandChoice<Long>(name, value)
class LocalizedNumberCommandChoice(name: StringI18nData, value: Double) : LocalizedCommandChoice<Double>(name, value)

class RawStringCommandChoice(name: String, value: String) : RawCommandChoice<String>(name, value)
class RawIntegerCommandChoice(name: String, value: Long) : RawCommandChoice<Long>(name, value)
class RawNumberCommandChoice(name: String, value: Double) : RawCommandChoice<Double>(name, value)