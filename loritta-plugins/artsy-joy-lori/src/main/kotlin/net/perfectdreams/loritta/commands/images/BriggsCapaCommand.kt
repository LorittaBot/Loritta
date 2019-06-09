package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class BriggsCapaCommand : BasicSkewedImageCommand(
        arrayOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"),
        CommandCategory.IMAGES,
        "commands.images.briggscover.description",
        "briggs_capa.png",
        Corners(
                242F,67F, // UL

                381F,88F, // UR

                366F,266F, // LR

                218F, 248F // LL
        )
)