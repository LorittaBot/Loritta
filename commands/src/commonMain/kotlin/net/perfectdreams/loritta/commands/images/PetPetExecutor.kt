package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class PetPetExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/pet-pet",
    "petpet.gif"
) {
    companion object : CommandExecutorDeclaration(PetPetExecutor::class) {
        override val options = SingleImageOptions
    }
}