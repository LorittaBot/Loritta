package net.perfectdreams.loritta.website.routes.api.v1.loritta

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.api.utils.Rarity
import net.perfectdreams.loritta.datawrapper.Background

@kotlinx.serialization.ImplicitReflectionSerializer
fun main() {
	val list = listOf(
			Background(
					"test",
					"test.png",
					true,
					Rarity.LEGENDARY,
					null,
					null,
					"a",
					"b"
			),
			Background(
					"owo",
					"uwu.png",
					true,
					Rarity.LEGENDARY,
					null,
					null,
					"a",
					"b"
			)
	)


	val json = Json.toJson(Background.serializer().list, list)

	println(json)
}