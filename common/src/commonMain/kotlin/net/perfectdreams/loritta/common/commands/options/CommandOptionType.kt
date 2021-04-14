package net.perfectdreams.loritta.common.commands.options

interface CommandOptionType {
    val isNullable: Boolean

    interface ToNullable {
        fun toNullable(): CommandOptionType
    }

    abstract class Nullable : CommandOptionType {
        override val isNullable = true
    }

    // ===[ TYPES ]===
    // String
    object String : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableInteger
    }

    object NullableString : Nullable()

    // Integer
    object Integer : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableInteger
    }

    object NullableInteger : Nullable()
}