package net.perfectdreams.loritta.cinnamon.discord.utils

object DiscordResourceLimits {
    object Guild {
        val Channels = 500
        val Roles = 250
    }

    object Command {
        object Description {
            const val Length = 100
        }

        object Options {
            val ChoicesCount = 25

            object Description {
                const val Length = 100
            }
        }
    }

    object Message {
        const val Length = 2000
        const val EmbedsPerMessage = 10
    }

    object Embed {
        const val Title = 256
        const val Description = 4096
        const val FieldsPerEmbed = 25
        const val TotalCharacters = 6000

        object Field {
            const val Name = 256
            const val Value = 1024
        }

        object Footer {
            const val Text = 2048
        }

        object Author {
            const val Name = 256
        }
    }
}