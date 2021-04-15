package net.perfectdreams.loritta.common.commands.options

interface CommandOptionType {
    val isNullable: Boolean

    interface ToNullable {
        fun toNullable(): Nullable
    }

    abstract class Nullable : CommandOptionType {
        override val isNullable = true
    }

    // ===[ TYPES ]===
    // String
    object String : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableString
    }

    object NullableString : Nullable()

    // Integer
    object Integer : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableInteger
    }

    object NullableInteger : Nullable()

    // Boolean
    // Can't be named "Boolean" because that causes Kotlin to go crazy
    object Bool : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableBool
    }

    object NullableBool : Nullable()

    // User
    object User : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableUser
    }

    object NullableUser : Nullable()

    // Channel
    object Channel : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableChannel
    }

    object NullableChannel : Nullable()

    // Role
    object Role : ToNullable, CommandOptionType {
        override val isNullable = false

        override fun toNullable() = NullableRole
    }

    object NullableRole : Nullable()

    // Stuff that isn't present in Discord Slash Commands yet
    // (After all, this CommandOptionType is based of Discord InteraKTions implementation! :3)
    // StringList
    object StringList : CommandOptionType {
        override val isNullable = false
    }

    // ImageReference
    object ImageReference : CommandOptionType {
        override val isNullable = false
    }
}