package com.mrpowergamerbr.loritta.utils

import jdk.nashorn.api.scripting.NashornScriptEngineFactory

fun main(args: Array<String>) {
	val factory = NashornScriptEngineFactory()

	val engine = factory.getScriptEngine()
	val engine2 = factory.getScriptEngine()

	val blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};"
	// Funções inline para facilitar a programação de comandos
	val inlineMethods = """var nashornUtils = Java.type("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils");
var loritta=function(){ return nashornUtils.loritta(); };"""

	engine.eval(blacklisted)
	val bindings = engine.createBindings()


	engine.eval("quit()", bindings);
}