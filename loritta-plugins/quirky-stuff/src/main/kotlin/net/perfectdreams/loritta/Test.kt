package net.perfectdreams.loritta

fun main() {
	println(Christmas2019.SPECIAL_GUEST_MIKARU_HAT)

	var totalPoints = 2_000_000

	repeat(25) { // Presentes
		totalPoints -= 10_000
	}

	totalPoints -= 100_000 // Árvore de Natal

	totalPoints -= 20_000 // Guirlanda
	totalPoints -= 20_000 // Luzes de Natal
	totalPoints -= 20_000 // coisa amarela em volta da árvore
	totalPoints -= 40_000 // Estrela de natal

	totalPoints -= 50_000 // Negócio vermelho em volta das paredes https://img.elo7.com.br/product/zoom/1BBCF14/cenario-sala-de-natal-cenario.jpg

	repeat(10) {
		totalPoints -= 120_000 // convidado
		totalPoints -= 30_000 // gorro
	}

	println("Total points remaining: $totalPoints")

	Christmas2019.DropType.values().forEach {
		println("${it.name}: ${it.requiredPoints} pontos")
	}
}