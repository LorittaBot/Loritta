package net.perfectdreams.loritta.trunfo

fun main() {
	val availableCards = mutableListOf(
			Card(
					"Loritta Morenitta",
					"https://cdn.discordapp.com/emojis/626942886432473098.png?v=1",

					170,
					50,
					16,
					75,
					0,
					0,
					97
			),
			Card(
					"Pantufa",
					"https://cdn.discordapp.com/emojis/626942886583205898.png?v=1",


					166,
					45,
					15,
					65,
					0,
					0,
					40
			),
			Card(
					"Gabriela",
					"https://cdn.discordapp.com/icons/602640402830589954/bae9a004d70279a54280de9ebc9ad65e.png?size=256",

					174,
					66,
					17,
					55,
					0,
					0,
					50
			),
			Card(
					"Dokyo Inuyama",
					"https://cdn.discordapp.com/emojis/603389181808607262.png?v=1",

					168,
					50,
					13,
					75,
					0,
					0,
					65
			),
			Card(
					"Gessy",
					"https://cdn.discordapp.com/emojis/593907632784408644.png?v=1",

					180,
					52,
					14,
					70,
					0,
					0,
					70
			),
			Card(
					"Kaike Carlos",
					"https://cdn.discordapp.com/avatars/123231508625489920/4cd2ed17d932605a6c9cef5fdf13e96d.png?size=256",

					148,
					28,
					17,
					53,
					0,
					0,
					37
			),
			Card(
					"Tobias",
					"https://cdn.discordapp.com/emojis/450476856303419432.png?v=1",

					30,
					4,
					6,
					78,
					0,
					0,
					67
			),
			Card(
					"Gato da Raça Meninas de 14 Anos",
					"https://cdn.discordapp.com/emojis/585536245891858470.png?v=1",

					25,
					5,
					14,
					35,
					0,
					0,
					74
			),
			Card(
					"Ricardo Milos",
					"https://cdn.discordapp.com/emojis/606162761386557464.gif?v=1",

					172,
					86,
					42,
					90,
					0,
					0,
					90
			),
			Card(
					"Sheldo",
					"https://cdn.discordapp.com/attachments/617182204212150316/630525989176213514/unknown.png",

					120,
					20,
					10,
					58,
					0,
					0,
					27
			),
			Card(
					"Moletom da Lori",
					"https://cdn.discordapp.com/emojis/623670395907866625.png?v=1",

					43,
					1,
					3,
					78,
					0,
					0,
					100
			),
			Card(
					"Gessy após ter soltado um barro",
					"https://cdn.discordapp.com/emojis/590716264528347161.png?v=1",

					180,
					48,
					14,
					85,
					0,
					0,
					63
			),
			Card(
					"Urso sem nome",
					"https://cdn.discordapp.com/attachments/297732013006389252/630561850521681920/urso.png",

					180,
					85,
					23,
					53,
					0,
					0,
					20
			),
			Card(
					"Chocoholic",
					"https://cdn.discordapp.com/attachments/358774895850815488/630561783807213579/Sweet_Land_Pet.png",

					75,
					7,
					3,
					67,
					0,
					0,
					57
			)
	)

	val wonCards = mutableMapOf<Card, Int>()
	val tieCards = mutableMapOf<Card, Int>()
	val lostCards = mutableMapOf<Card, Int>()

	for (card in availableCards) {
		for (matchCard in availableCards.filter { it != card }) {
			for (entry in listOf(
					Card::age,
					Card::weight,
					Card::height,
					Card::power,
					Card::fame
			)) {
				val el0 = entry.get(card)
				val el1 = entry.get(matchCard)

				if (el0 > el1) {
					wonCards[card] = wonCards.getOrPut(card, { 0 }) + 1
				} else if (el1 > el0) {
					lostCards[card] = lostCards.getOrPut(card, { 0 }) + 1
				} else {
					tieCards[card] = tieCards.getOrPut(card, { 0 }) + 1
				}
			}
		}
	}

	wonCards.entries.sortedByDescending { it.value }.forEach {
		println("${it.key.name}")
		val won = wonCards[it.key]
		val tie = tieCards[it.key]
		val lost = lostCards[it.key]

		println("Vitórias: $won")
		println("Empates: $tie")
		println("Derrotas: $lost")
	}
}