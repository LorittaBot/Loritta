package net.perfectdreams.loritta.utils

import java.io.File
import java.net.URL
import java.util.*

fun main() {
    while (true) {
        println("ID do usuário (Discord)?")
        val userId = readLine()!!
        println("Nome do usuário? (Se o artista já tem um arquivo, escrava \"discord\" para procurar nos arquivos pelo ID do Discord)")
        var userName = readLine()!!

        val artistId = if (userName == "discord") {
            println("Procurando via Discord...")
            var temp: String? = null
            for (it in File("C:\\Users\\leona\\Documents\\LorittaAssets\\website\\fan_arts\\").listFiles()) {
                if (it.extension == "conf") {
                    val text = it.readText()
                    if (text.contains("        id = \"$userId\"")) {
                        temp = it.nameWithoutExtension
                        println("Arquivo encontrado! $it")
                        break
                    }
                }
            }
            temp ?: throw RuntimeException("Artista não existe! :(")
        } else {
            println("ID do artista? (Vazio = baseado no nome do autor)")
            readLine()!!.let {
                if (it.isEmpty())
                    userName.toLowerCase().replace(" ", "_")
                else
                    it
            }
        }

        if (userName == "discord") {
            println("Qual é o username que será utilizado no nome do arquivo?")
            userName = readLine()!!
        }

        println("Data da Fan Art? (Vazio = hoje)")
        val date = readLine()!!.let {
            val cal = Calendar.getInstance()
            if (it.isEmpty())
                "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(
                    Calendar.DAY_OF_MONTH
                ).toString().padStart(2, '0')}"
            else
                it
        }

        println("URL da Fan Art?")
        val fanArtUrl = readLine()!!.trim()
        println("Nome da Fan Art? (Vazio = Loritta_idx_-_AuthorName.png/jpg/etc)")
        val ext = if (fanArtUrl.endsWith("jpg"))
            "jpg"
        else
            "png"

        val fanArtName = readLine()!!.let {
            if (it.isEmpty()) {
                val first = File("C:\\Users\\leona\\Pictures\\Loritta\\fanarts\\Loritta_-_${userName.replace(" ", "_")}.$ext")
                if (!first.exists())
                    first.name
                else {
                    var recursiveness = 2
                    var f: File
                    do {
                        f = File("C:\\Users\\leona\\Pictures\\Loritta\\fanarts\\Loritta_${recursiveness}_-_${userName.replace(" ", "_")}.$ext")
                        recursiveness++
                    } while (f.exists())

                    f.name
                }
            } else
                it
        }

        println("ID do usuário (Discord): $userId")
        println("Nome do usuário: $userName")
        println("ID do artista: $artistId")
        println("Data da Fan Art: ${date}")
        println("URL da Fan Art: $fanArtUrl")
        println("Nome da Fan Art: $fanArtName")
        println("Tudo OK?")
        readLine()

        println("Baixando fan art...")
        val contents = URL(fanArtUrl).openConnection().getInputStream().readAllBytes()
        File("C:\\Users\\leona\\Pictures\\Loritta\\fanarts\\$fanArtName").writeBytes(contents)
        println("Fan Art salva com sucesso!")
        println("Criando arquivo do artista...")

        val artistFile = File("C:\\Users\\leona\\Documents\\LorittaAssets\\website\\fan_arts\\$artistId.conf")

        val fanArtSection = """    {
        |        file-name = "$fanArtName"
        |        created-at = "$date"
        |        tags = []
        |    }
    """.trimMargin()

        if (artistFile.exists()) {
            println("Arquivo do artista já existe! Vamos apenas inserir a fan art...")
            val artistTemplate = artistFile.readText()
            val lines = artistTemplate.lines().toMutableList()

            val insertAt = lines.indexOf("]")
            lines.addAll(insertAt, fanArtSection.lines())

            println("Isto está OK?")
            println(lines.joinToString("\n"))
            readLine()
            artistFile.writeText(lines.joinToString("\n"))
            println("Finalizado! :3")
        } else {
            val fullArtistTemplate = """id = "$artistId"

info {
    name = "$userName"
}

fan-arts = [
$fanArtSection
]

networks = [
    {
        type = "discord"
        id = "$userId"
    }
]
"""

            println("Isto está OK?")
            println(fullArtistTemplate)
            readLine()
            artistFile.writeText(fullArtistTemplate)
            println("Finalizado! :3")
        }

        println("Quer continuar a fazer?")
        readLine()!!
        println("Okay, então lá vamos nós de novo!")
    }
}