package com.mrpowergamerbr.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MigrationTool
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
	// STATIC MAGIC(tm)
	lateinit var loritta: Loritta

	@JvmStatic
	fun main(args: Array<String>) {
		val doNotStart = File("do_not_start").exists()
		if (doNotStart) {
			while (true) {
				System.out.println("Falha de segurança detectada!")
				Thread.sleep(120000)
			}
		}

		// https://bugs.openjdk.java.net/browse/JDK-7016595
		// Nós precisamos ativar o PATCH manualmente
		WebsiteUtils.allowMethods("PATCH")

		// https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val mf = jar.manifest
		val mattr = mf.mainAttributes
		// Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
		val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

		// The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
		// By the way, don't forget to append your original JAR at the end of the string!
		val clazz = LorittaLauncher::class.java
		val protectionDomain = clazz.protectionDomain
		val propClassPath = manifestClassPath.replace(" ", ":") + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

		// Now we set it to our own classpath
		System.setProperty("kotlin.script.classpath", propClassPath)

		val file = File(System.getProperty("conf") ?: "./config.yml")
		var config: LorittaConfig? = null

		if (file.exists()) {
			val json: String
			try {
				json = file.readText()
				config = Constants.MAPPER.readValue(json, LorittaConfig::class.java)
			} catch (e: IOException) {
				e.printStackTrace()
				System.exit(1) // Sair caso der erro
				return
			}

		} else {
			println("Welcome to Loritta!")
			println("Because this is your first time executing me, I will create a file named \"config.yml\", that you will need to configure before using me!")
			println("")
			println("After configuring the file, run me again!")
			System.exit(1)
			return
		}

		val arg0 = args.getOrNull(0)
		val arg1 = args.getOrNull(1)

		if (arg0 != null) {
			val tool = MigrationTool(config)

			when (arg1) {
				"warns" -> tool.migrateWarns()
				"local" -> tool.migrateLocalProfiles()
			}
			return
		}

		// Iniciar instância da Loritta
		loritta = Loritta(config)
		loritta.start()
	}
}