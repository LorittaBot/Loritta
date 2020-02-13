package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object CanellaDvdCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					267F, 50F,
					375F, 37F,
					370F, 182F,
					265F, 183F
			),
			// Covers no Canella
			BasicSkewedImageCommand.Corners(
					274F, 162F, // topo esquerdo
					331F, 156F, // topo direito
					334F, 224F, // inferior direito
					278F, 224F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					28F, 127F, // topo esquerdo
					100F, 130F, // topo direito
					100F, 207F, // inferior direito
					32F, 212F // inferior esquerdo
			),
			// Covers no Chão
			BasicSkewedImageCommand.Corners(
					317F, 227F, // topo esquerdo
					366F, 228F, // topo direito
					371F, 293F, // inferior direito
					322F, 294F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					207F, 243F, // topo esquerdo
					247F, 240F, // topo direito
					269F, 299F, // inferior direito
					228F, 302F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					268F, 284F, // topo esquerdo
					314F, 280F, // topo direito
					325F, 296F, // inferior direito
					271F, 302F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					209F, 296F, // topo esquerdo
					261F, 294F, // topo direito
					286F, 321F, // inferior direito
					228F, 329F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					152F, 259F, // topo esquerdo
					199F, 264F, // topo direito
					222F, 335F, // inferior direito
					171F, 334F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					189F, 340F, // topo esquerdo
					236F, 324F, // topo direito
					302F, 348F, // inferior direito
					253F, 369F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					125F, 283F, // topo esquerdo
					193F, 282F, // topo direito
					203F, 362F, // inferior direito
					130F, 370F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					8F, 308F, // inferior esquerdo
					56F, 293F, // inferior direito
					98F, 309F, // topo esquerdo
					63F, 326F // topo direito
			),
			BasicSkewedImageCommand.Corners(
					286F, 302F, // topo esquerdo
					340F, 300F, // topo direito
					374F, 335F, // inferior direito
					309F, 336F // inferior esquerdo
			),
			BasicSkewedImageCommand.Corners(
					25F, 337F, // inferior esquerdo
					50F, 301F, // topo esquerdo
					140F, 302F, // topo direito
					131F, 339F // inferior direito
			),
			BasicSkewedImageCommand.Corners(
					108F, 270F, // topo esquerdo
					153F, 270F, // topo direito
					116F, 357F, // inferior direito
					68F, 350F // inferior esquerdo
			)
	)
	override val sourceTemplatePath = "canella_dvd.png"
	override val descriptionKey = "commands.images.canelladvd.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("canelladvd", "matheuscanelladvd", "canellacover", "matheuscanelladvd")) {}
}