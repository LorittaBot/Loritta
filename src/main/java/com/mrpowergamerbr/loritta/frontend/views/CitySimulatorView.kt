package com.mrpowergamerbr.loritta.frontend.views

import com.mrpowergamerbr.loritta.frontend.utils.RenderContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter

object CitySimulatorView {
	@JvmStatic
	fun render(context: RenderContext): Any {
		val guildId = context.arguments[1]

		val guild = lorittaShards.getGuildById(guildId)

		if (guild != null) {
			val oneByOneResidential = mutableMapOf(
					4 to SimCityBuilding("house0", 20),
					5 to SimCityBuilding("house1", 18)
			)
			val twoByTwoResidential = mutableMapOf(
					100 to SimCityBuilding("apts0", 34)
			)
			// println("City test!")
			val grid = Array(256, init = { IntArray(256) })

			val random = SplittableRandom(guild.idLong) // Usar o ID da guild como seed

			// nothing = 0
			// tree = 1
			// city center = 2
			// road = 3
			// house1 = 4
			// reserved = -1
			val centerX = 127
			val centerY = 127
			val population = guild.members.size
			var popCheck = population

			val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB_PRE)

			val graphics = image.graphics

			val fartest = Math.sqrt(Math.pow((centerX - 0).toDouble(), 2.toDouble()) + Math.pow((centerY - 0).toDouble(), 2.toDouble())) + 1;
			for (x in 0..255) {
				for (y in 0..255) {
					val distance = Math.sqrt(Math.pow((centerX - x).toDouble(), 2.toDouble()) + Math.pow((centerY - y).toDouble(), 2.toDouble()));

					// println(distance)
					// println((distance - fartest))
					if (random.nextInt((distance - fartest).toInt(), 1) in -5..0) {
						grid[x][y] = 1
					}
				}
			}


			// city center
			for (x in centerX - 4..centerX + 4) {
				for (y in centerY - 4..centerY + 4) {
					grid[x][y] = -1
				}
			}

			grid[127][119] = 2

			// fill roads around center
			for (x in centerX - 5..centerX + 5) {
				for (y in centerY - 5..centerY + 5) {
					if (grid[x][y] != 2 && grid[x][y] != -1) {
						grid[x][y] = 3
					}
				}
			}

			var direction = 0
			var ticks = 0

			for (z in 0..3) {
				var roadX = 122;
				var roadY = 122;
				if (z == 1) {
					roadX = 122;
					roadY = 132;
				}
				if (z == 2) {
					roadX = 132;
					roadY = 132;
				}
				if (z == 3) {
					roadX = 132;
					roadY = 122;
				}
				for (i in 0..population / 16) {
					val distance = Math.sqrt(Math.pow((centerX - roadX).toDouble(), 2.toDouble()) + Math.pow((centerY - roadY).toDouble(), 2.toDouble()));

					if (ticks > 2 && random.nextInt((distance - fartest).toInt(), 1) in -15..0) {
						var newDirection = direction
						while (newDirection == direction) {
							newDirection = random.nextInt(0, 4)
						}
						direction = newDirection
						ticks = 0
					}

					var oldX = roadX
					var oldY = roadY

					when (direction) {
						0 -> roadX++
						1 -> roadX--
						2 -> roadY++
						3 -> roadY--
					}

					if (roadX !in 0..255 || roadY !in 0..255) {
						roadX = oldX
						roadY = oldY
						var newDirection = direction
						while (newDirection == direction) {
							newDirection = random.nextInt(0, 4)
						}
						direction = newDirection
						// ticks = 0
					}

					if (grid[roadX][roadY] == 2 || grid[roadX][roadY] == -1) {
						roadX = oldX
						roadY = oldY
						var newDirection = direction
						while (newDirection == direction) {
							newDirection = random.nextInt(0, 4)
						}
						direction = newDirection
						ticks = 0
					}

					grid[roadX][roadY] = 3
					ticks++
				}
			}

			var riskyFactor = 0
			endpopulation@ for (z in 0..3) {
				for (x in 0..255) {
					for (y in 0..255) {
						val result = isNearRoad(x, y, grid);
						if (result.canBuild && riskyFactor >= result.riskyFactor) {
							if (canBuildAt(x, y, 2, grid)) {
								grid[x][y] = twoByTwoResidential.entries.toList()[random.nextInt(twoByTwoResidential.entries.size)].key
								setBuildingType(x, y, 2, 100, grid)
								popCheck -= 1
							}
							if (canBuildAt(x, y, 1, grid)) {
								grid[x][y] = oneByOneResidential.entries.toList()[random.nextInt(oneByOneResidential.entries.size)].key
								popCheck -= 1
							}
							if (0 >= popCheck) {
								break@endpopulation
							}
						}
					}
				}
				riskyFactor++
			}

			var strBuilder = StringBuilder();
			var currentX = 0
			var currentY = 1728

			var html = """<html>
<head>
 <meta charset="UTF-8">
<script
			  src="https://code.jquery.com/jquery-3.2.1.min.js"
			  integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4="
			  crossorigin="anonymous"></script>
<style>
body {
background-image: url("https://loritta.website/assets/loricity/background.png");
}
#sc2000 img {
position: absolute;
border: 0;
}
#popup {
position: absolute;
float: left;
pointer-events: none;
white-space: nowrap;
background-color: rgb(8, 8, 74);
color: #f2f2f2;
opacity: 0.8;
font-family: "Comic Sans MS", "Comic Sans", cursive;
border-radius: 16px;
box-shadow: 0px 0px 8px 14px rgb(8, 8, 74);
padding: 2px;
display: hidden;
}

#populationInfo {
position: fixed;
top: 0;
left: 0;
background-color: white;
}
</style>
</head>
<body>
<div id="popup">
</div>
<div id="populationInfo">
LorittaLand</br>
Cidade: ${guild.name}</br>
População: ${guild.members.size}</br>
Grana: §40028922
</div>
<div id="sc2000">
{{ code }}
</div>
<script>
window.location.hash = '#cityCore';
var isHovering = false
var tso = new Audio('https://loritta.website/assets/loricity/tsosas2_v2.mp3');
tso.play();
tso.volume = 0.2;
${'$'}(document).on('mousemove', function(e){
	var details = e.target
	if (${'$'}(details).attr('data-sctitle')){
        if (!isHovering) {
            var audio = new Audio('https://loritta.website/assets/loricity/snd_mouseover.ogg');
            audio.play();
        }
        isHovering = true
        ${'$'}('#popup').html(${'$'}(details).attr('data-sctitle'))
		${'$'}('#popup').css({
		   left:  e.pageX + 1,
		   top:   e.pageY + 1,
			"display": "inherit"
		});
	} else {
        isHovering = false
		${'$'}('#popup').css({
			"display": "none"
		});
	}
});
</script>
</body>
</html>"""
			var index = 1
			for (x in 0..255) {
				for (y in 0..255) {
					val bld = grid[x][y]

					if (bld == -1) {
						// strBuilder.append("<img alt=\"$x, $y\" src=\"blackhole.png\" style=\"z-index: -$y; top: ${currentY}px; left: ${currentX}px;\">\n")
					}
					if (bld == 1) {
						graphics.color = Color.GREEN
						graphics.fillRect(x, y, 1, 1)
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/trees.png\" style=\"pointer-events: none; z-index: -$y; top: ${currentY}px; left: ${currentX}px;\">\n")
					}
					if (bld == 2) {
						graphics.color = Color.MAGENTA
						graphics.fillRect(x, y, 1, 1)

						val serverIcon = LorittaUtils.downloadImage(guild.iconUrl.replace("jpg", "png")).getScaledInstance(288, 320, BufferedImage.TYPE_INT_ARGB_PRE).toBufferedImage()


						val lorittaImage = LorittaImage(serverIcon)
						lorittaImage.setCorners(
								0F, 248F,
								144F, 176F,
								287F, 248F,
								144F, 319F
						)
						val output = ByteArrayOutputStream()
						ImageIO.write(lorittaImage.bufferedImage, "png", output)
						val encodedFile = DatatypeConverter.printBase64Binary(output.toByteArray())
						strBuilder.append("<img id=\"cityCore\" src=\"data:image/png;base64, $encodedFile\" style=\"z-index: -$y; top: ${currentY}px; left: ${currentX}px;\">")
						// println(encodedFile)
					}
					if (bld == 3) {
						graphics.color = Color.BLUE
						graphics.fillRect(x, y, 1, 1)
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/road_v2.png\" data-sctitle=\"Uma rua qualquer...</br></br>$x, $y\" style=\"cursor:crosshair; z-index: -$y; top: ${currentY + (320 - 16)}px; left: ${currentX}px;\">")
					}
					if (oneByOneResidential.containsKey(bld)) {
						graphics.color = Color.RED
						graphics.fillRect(x, y, 1, 1)
						val scBld = oneByOneResidential.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-sctitle=\"kk eae men</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (320 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					if (twoByTwoResidential.containsKey(bld)) {
						val scBld = twoByTwoResidential.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-sctitle=\"irineu, você não sabe e nem eu</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (329 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					currentX += 16
					currentY -= 8
					// println("$x, $y")
				}
				currentX = 0 + (16 * index)
				currentY = 1728 + (8 * index)
				index++
			}

			// ImageIO.write(image, "png", File("D:\\citytest.png"))
			return html.replace("{{ code }}", strBuilder.toString())
		} else {
			return "Queria saber da onde você tirou essa guild... \uD83D\uDE45"
		}
	}

	fun isNearRoad(x: Int, y: Int, grid: Array<IntArray>): RoadResult {
		// Coisas podem ser colocadas até 3 tiles de distância de uma rua
		// Sendo x e y o centro aonde *deveria* colocar a casa
		val roadResults = mutableListOf<RoadResult>()

		for (roadX in x - 2..x + 2) {
			for (roadY in y - 2..y + 2) {
				if (roadX in 0..255 && roadY in 0..255) {
					val bld = grid[roadX][roadY]

					if (bld == 3) {
						var riskyFactor = Math.sqrt(Math.pow((x - roadX).toDouble(), 2.toDouble()) + Math.pow((y - roadY).toDouble(), 2.toDouble()))
						roadResults.add(RoadResult(true, riskyFactor))
					}
				}
			}
		}

		if (roadResults.isNotEmpty()) {
			roadResults.sortBy { it.riskyFactor }
			return roadResults[0]
		}
		return RoadResult(false, -1.0)
	}

	fun canBuildAt(x: Int, y: Int, size: Int, grid: Array<IntArray>): Boolean {
		for (roadX in x..x + (size - 1)) {
			for (roadY in y..y + (size - 1)) {
				if (roadX in 0..255 && roadY in 0..255) {
					val bld = grid[roadX][roadY]

					if (bld != 0) {
						return false
					}
				} else {
					return false
				}
			}
		}
		return true
	}

	fun setBuildingType(x: Int, y: Int, size: Int, id: Int, grid: Array<IntArray>): Boolean {
		for (roadX in x..x + (size - 1)) {
			for (roadY in y..y + (size - 1)) {
				if (roadX in 0..255 && roadY in 0..255) {
					grid[roadX][roadY] = -1
				}
			}
		}
		grid[x][y] = id
		return true
	}

	data class RoadResult(val canBuild: Boolean, val riskyFactor: Double)

	data class SimCityBuilding(
			val tileName: String,
			val size: Int
	)
}