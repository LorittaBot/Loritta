package net.perfectdreams.loritta.commands.vanilla.minecraft.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object MinecraftCommandDeclaration {
    object Root : CommandDeclaration(
        name = "mcuser",
        // TODO: Fix Locale
        description = LocaleKeyData("idk")
    ) {
        override val options = Options

        object Options : CommandDeclaration.Options() {
            val avatar = subcommand(Avatar)
                .register()

            val body = subcommand(Body)
                .register()

            val head = subcommand(Head)
                .register()

            val skin = subcommand(Skin)
                .register()

            val uuid = subcommand(UUID)
                .register()

            val offlineUUID = subcommand(OfflineUUID)
                .register()
        }
    }

    object Avatar : CommandDeclaration(
        name = "avatar",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mcavatar.description")
    ) {
        override val options = PlayerNameOptions
    }

    object Body : CommandDeclaration(
        name = "body",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mcbody.description")
    ) {
        override val options = PlayerNameOptions
    }

    object Head : CommandDeclaration(
        name = "head",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mchead.description")
    ) {
        override val options = PlayerNameOptions
    }

    object Skin : CommandDeclaration(
        name = "skin",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mcskin.description")
    ) {
        override val options = PlayerNameOptions
    }

    object UUID : CommandDeclaration(
        name = "uuid",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mcuuid.description")
    ) {
        override val options = PlayerNameOptions
    }

    object OfflineUUID : CommandDeclaration(
        name = "offlineuuid",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.mcofflineuuid.description")
    ) {
        override val options = PlayerNameOptions
    }

    // Because all of them uses the same "player name" option, we are going to create a single object to keep everything nice and tidy
    object PlayerNameOptions : CommandDeclaration.Options() {
        // TODO: Fix Locale
        val playerName = string("player_name", LocaleKeyData("idk"))
            .required()
            .register()
    }
}