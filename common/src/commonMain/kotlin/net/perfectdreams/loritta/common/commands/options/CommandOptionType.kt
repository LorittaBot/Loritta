package net.perfectdreams.loritta.common.commands.options

interface CommandOptionType {
    val isNullable: Boolean

    interface ToNullable {
        fun toNullable(): CommandOptionType
    }

    object Integer : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableInteger
    }

    object NullableInteger : CommandOptionType {
        override val isNullable = true
    }
}