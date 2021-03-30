package net.perfectdreams.loritta.interactions.commands.vanilla

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object PingCommandDeclaration : CommandDeclaration(
    name = "ping",
    description = LocaleKeyData("commands.command.ping.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        val something = subcommand(Something)
            .register()
        val anotherThing = subcommand(AnotherThing)
            .register()
    }

    object Something : CommandDeclaration(
        name = "something",
        description = LocaleKeyData("uwu")
    ) {
        override val options = Options

        object Options : CommandDeclaration.Options() {
            val text = string("text", LocaleKeyData("owo"))
                .required()
                .register()
        }
    }

    object AnotherThing : CommandDeclaration(
        name = "anotherthing",
        description = LocaleKeyData("ewe")
    )
}