package com.mrpowergamerbr.loritta.frontend.views

import com.mrpowergamerbr.loritta.frontend.utils.RenderContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter

object CitySimulatorView {
	enum class RoadDirection(val direction: Int) {
		NORTH(0),
		SOUTH(1),
		EAST(2),
		WEST(3);

		fun takeATurn(): List<RoadDirection> {
			when(this) {
				RoadDirection.NORTH -> return listOf(RoadDirection.EAST, RoadDirection.WEST)
				RoadDirection.SOUTH -> return listOf(RoadDirection.EAST, RoadDirection.WEST)
				RoadDirection.EAST -> return listOf(RoadDirection.NORTH, RoadDirection.SOUTH)
				RoadDirection.WEST -> return listOf(RoadDirection.NORTH, RoadDirection.SOUTH)
			}
		}
	}

	@JvmStatic
	fun render(context: RenderContext): Any {
		val guildId = context.arguments[1]

		val guild = lorittaShards.getGuildById(guildId)

		if (guild != null) {
			val oneByOneResidentialRich = mutableMapOf(
					4 to SimCityBuilding("house0", 20),
					5 to SimCityBuilding("house1", 18)
			)
			val oneByOneResidentialMedium = mutableMapOf(
					10 to SimCityBuilding("mc_house0", 18)
			)
			val oneByOneResidentialLow = mutableMapOf(
					20 to SimCityBuilding("lc_house0", 16)
			)
			val twoByTwoResidential = mutableMapOf(
					100 to SimCityBuilding("apts0", 34)
			)

			val oneByOneComercial = mutableMapOf(
					1000 to SimCityBuilding("sm_comercial0", 42),
					1001 to SimCityBuilding("sm_comercial1", 24),
					1002 to SimCityBuilding("sm_comercial2", 33),
					1003 to SimCityBuilding("sm_comercial3", 31),
					1004 to SimCityBuilding("sm_comercial4", 19),
					1005 to SimCityBuilding("sm_comercial5", 26),
					1006 to SimCityBuilding("sm_comercial6", 17),
					1007 to SimCityBuilding("sm_comercial7", 22)
			)

			// println("City test!")
			val grid = Array(256, init = { IntArray(256) })
			val ownerGrid = Array(256, init = { Array<String>(256, init = { "Ninguém..." }) })

			val random = SplittableRandom(guild.idLong) // Usar o ID da guild como seed

			// nothing = 0
			// tree = 1
			// city center = 2
			// road = 3
			// house1 = 4
			// comercial0 = 1000

			// reserved = -1
			val centerX = 127
			val centerY = 127
			val population = guild.members.size
			var buildingCount = population
			var comercialBuildings = 0

			// Carregar os userdatas dos usuários
			val serverConfig = loritta.getServerConfigForGuild(guildId)
			val userDataClone = HashMap<String, LorittaServerUserData>(serverConfig.userData)
			val toRemove = mutableListOf<String>()

			guild.members.forEach {
				if (!userDataClone.containsKey(it.user.id)) {
					userDataClone.put(it.user.id, LorittaServerUserData())
				}
			}

			userDataClone.forEach { id, data ->
				if (guild.getMemberById(id) == null) {
					toRemove.add(id)
				}
			}

			toRemove.forEach {
				userDataClone.remove(it)
			}

			val userDataEntries = userDataClone.entries

			val idToComercial = mutableMapOf<String, Int>()

			userDataEntries.sortedBy {
				it.value.xp
			}

			// Adicionar os prédios comerciais
			userDataEntries.forEach {
				comercialBuildings += it.value.getCurrentLevel().currentLevel - 1
				if (it.value.getCurrentLevel().currentLevel > 1) {
					idToComercial.put(it.key, it.value.getCurrentLevel().currentLevel - 1)
				}
			}

			val filledUserData = userDataEntries.toMutableList()

			buildingCount += comercialBuildings

			// XP do usuário com mais XP no servidor
			val maxXp = filledUserData.toList()[0].value.xp

			var popCheck = population
			val gridValues = mutableListOf<GridValue>()
			val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB_PRE)

			val graphics = image.graphics

			val fartest = Math.sqrt(Math.pow((centerX - 0).toDouble(), 2.toDouble()) + Math.pow((centerY - 0).toDouble(), 2.toDouble())) + 1;
			for (x in 0..255) {
				for (y in 0..255) {
					val distance = Math.sqrt(Math.pow((centerX - x).toDouble(), 2.toDouble()) + Math.pow((centerY - y).toDouble(), 2.toDouble()));

					// println(distance)
					// println((distance - fartest))
					gridValues.add(GridValue(x, y, distance))
					if (random.nextInt((distance - fartest).toInt(), 1) in -15..0) {
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

			var direction: RoadDirection = RoadDirection.NORTH
			var ticks = 0

			for (z in 0..3) {
				var roadX = 122;
				var roadY = 127;
				direction = RoadDirection.SOUTH
				if (z == 1) {
					roadX = 132;
					roadY = 127;
					direction = RoadDirection.NORTH
				}
				if (z == 2) {
					roadX = 127;
					roadY = 132;
					direction = RoadDirection.EAST
				}
				if (z == 3) {
					roadX = 127;
					roadY = 122;
					direction = RoadDirection.WEST
				}
				for (i in 0..buildingCount) {
					val distance = Math.sqrt(Math.pow((centerX - roadX).toDouble(), 2.toDouble()) + Math.pow((centerY - roadY).toDouble(), 2.toDouble()));

					if (ticks > 2 && random.nextInt((distance - fartest).toInt(), 1) in -7..0) {
						direction = direction.takeATurn()[random.nextInt(0, 2)]
						ticks = 0
					}

					var oldX = roadX
					var oldY = roadY

					when (direction) {
						RoadDirection.NORTH -> roadX++
						RoadDirection.SOUTH -> roadX--
						RoadDirection.EAST -> roadY++
						RoadDirection.WEST -> roadY--
					}

					if (roadX !in 0..255 || roadY !in 0..255) {
						roadX = oldX
						roadY = oldY
						direction = direction.takeATurn()[random.nextInt(0, 2)]
						// ticks = 0
					}

					if (grid[roadX][roadY] == 2 || grid[roadX][roadY] == -1) {
						roadX = oldX
						roadY = oldY
						direction = direction.takeATurn()[random.nextInt(0, 2)]
						ticks = 0
					}

					grid[roadX][roadY] = 3
					ticks++
				}
			}

			gridValues.sortBy { it.distanceToCore }
			var riskyFactor = 0
			var comercialBuildingsCheck = comercialBuildings
			var cityMoney = 0.0
			endpopulation@ for (z in 0..5) {
				for ((x, y) in gridValues) {
					val result = isNearRoad(x, y, grid);
					if (result.canBuild && riskyFactor >= result.riskyFactor) {
						var toBuildNow = random.nextInt(0, 2)
						if (toBuildNow == 1 && idToComercial.entries.toMutableList().size == 0) {
							toBuildNow = 0
						}
						if (toBuildNow == 0) {
							val userLivingHere = userDataClone.toList()[population - popCheck]
							ownerGrid[x][y] = guild.getMemberById(userLivingHere.first)?.effectiveName ?: "Saiu do servidor..."
							if (canBuildAt(x, y, 2, grid)) {
								grid[x][y] = twoByTwoResidential.entries.toList()[random.nextInt(twoByTwoResidential.entries.size)].key
								setBuildingType(x, y, 2, 100, grid)
								popCheck -= 1
							}
							if (canBuildAt(x, y, 1, grid)) {
								if (userLivingHere.second.xp.toDouble() / maxXp.toDouble() >= 66) {
									grid[x][y] = oneByOneResidentialRich.entries.toList()[random.nextInt(oneByOneResidentialRich.entries.size)].key
								} else if (userLivingHere.second.xp.toDouble() / maxXp.toDouble() >= 33) {
									grid[x][y] = oneByOneResidentialMedium.entries.toList()[random.nextInt(oneByOneResidentialMedium.entries.size)].key
								} else {
									grid[x][y] = oneByOneResidentialLow.entries.toList()[random.nextInt(oneByOneResidentialLow.entries.size)].key
								}
								popCheck -= 1
							}
						} else if (toBuildNow == 1) {
							if (canBuildAt(x, y, 1, grid)) {
								grid[x][y] = oneByOneComercial.entries.toList()[random.nextInt(oneByOneComercial.entries.size)].key
								comercialBuildingsCheck -= 1

								val owner = idToComercial.entries.toMutableList()[random.nextInt(idToComercial.entries.size)]

								ownerGrid[x][y] = guild.getMemberById(owner.key)?.effectiveName ?: "Saiu do servidor..."

								var quantity = owner.value

								quantity -= 1
								cityMoney += 500

								if (quantity == 0) {
									idToComercial.remove(owner.key)
								} else {
									idToComercial.put(owner.key, quantity)
								}
							}
						}
						if (0 >= popCheck && comercialBuildingsCheck >= 0) {
							break@endpopulation
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
Grana: §$cityMoney</br>
popCheck: $popCheck</br>
comercialBuildingsCheck: $comercialBuildingsCheck</br>
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
var lastHover = null
${'$'}(document).on('mousemove', function(e){
	var details = e.target
    if (lastHover != details) {
        ${'$'}(lastHover).css({
"filter": "none",
"z-index": ${'$'}(lastHover).attr('data-zindex')
});
}
	if (${'$'}(details).attr('data-sctitle')){
        lastHover = details
        if (!isHovering) {
            var audio = new Audio('https://loritta.website/assets/loricity/snd_mouseover.ogg');
            audio.play();
        }
        isHovering = true
        ${'$'}('#popup').html(${'$'}(details).attr('data-sctitle'))
		${'$'}('#popup').css({
		   left:  e.pageX + 1,
		   top:   e.pageY + 1,
			"display": "inherit",
           "z-index": "999999"
		});
        ${'$'}(details).css({
"filter": "drop-shadow(1px 1px 0 white) drop-shadow(-1px 1px 0 white) drop-shadow(1px -1px 0 white) drop-shadow(-1px -1px 0 white)",
"z-index": "999998"
});
	} else {
        isHovering = false
		${'$'}('#popup').css({
			"display": "none",
            "filter:": "inherit",
		});
	}
});
</script>
</body>
</html>"""
			var index = 1
			var popIndex = 0
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
						strBuilder.append("<img id=\"cityCore\" src=\"data:image/png;base64, $encodedFile\" style=\"pointer-events: none; z-index: -$y; top: ${currentY}px; left: ${currentX}px;\">")
						// println(encodedFile)
					}
					if (bld == 3) {
						graphics.color = Color.BLUE
						graphics.fillRect(x, y, 1, 1)
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/road_v2.png\" data-zindex=\"$y\" data-sctitle=\"Uma rua qualquer...</br></br>$x, $y\" style=\"cursor:crosshair; z-index: -$y; top: ${currentY + (320 - 16)}px; left: ${currentX}px;\">")
					}
					val owner = ownerGrid[x][y]
					if (oneByOneResidentialRich.containsKey(bld)) {
						graphics.color = Color.RED
						graphics.fillRect(x, y, 1, 1)
						val scBld = oneByOneResidentialRich.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-zindex=\"$y\" data-sctitle=\"${owner}</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (320 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					if (oneByOneResidentialMedium.containsKey(bld)) {
						graphics.color = Color.RED
						graphics.fillRect(x, y, 1, 1)
						val scBld = oneByOneResidentialMedium.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-zindex=\"$y\" data-sctitle=\"${owner}</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (320 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					if (oneByOneResidentialLow.containsKey(bld)) {
						graphics.color = Color.RED
						graphics.fillRect(x, y, 1, 1)
						val scBld = oneByOneResidentialLow.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-zindex=\"$y\" data-sctitle=\"${owner}</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (320 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					if (twoByTwoResidential.containsKey(bld)) {
						val scBld = twoByTwoResidential.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-zindex=\"$y\" data-sctitle=\"${owner}</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (329 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
					}
					if (oneByOneComercial.containsKey(bld)) {
						val scBld = oneByOneComercial.get(bld)!!
						strBuilder.append("<img src=\"https://loritta.website/assets/loricity/${scBld.tileName}.png\" data-zindex=\"$y\" data-sctitle=\"Prédio comercial de ${owner}</br></br>+§500 para a cidade</br></br>$x, $y\" style=\"z-index: -$y; top: ${currentY + (320 - scBld.size)}px; left: ${currentX}px; cursor:crosshair;\">")
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

	data class GridValue(val x: Int, val y: Int, val distanceToCore: Double)

	data class SimCityBuilding(
			val tileName: String,
			val size: Int
	)
}