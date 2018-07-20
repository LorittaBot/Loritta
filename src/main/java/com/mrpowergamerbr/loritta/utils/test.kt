package com.mrpowergamerbr.loritta.utils

import org.jsoup.Jsoup

fun main(args: Array<String>) {
	val doc = Jsoup.connect("http://wiki.vg/Pre-release_protocol#Spawn_Painting")
			.get()

	val wikitables = doc.getElementsByClass("wikitable")
	val table = wikitables[25]

	val trs = table.getElementsByTag("tr")

	println(table)
	println(trs)
	for (tr in trs) {
		val tds = tr.getElementsByTag("td")
		if (tds.isEmpty()) continue
		val td0 = tds[0]
		val code = td0.getElementsByTag("code")[0].text()
		val idx = tds[1].text().toInt()

		println("""
			case $idx:
			    return "$code";
		""".trimIndent())
	}
}