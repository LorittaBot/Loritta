package net.perfectdreams.loritta.parallax.wrapper

import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyObject

class ProxyTest : ProxyObject {
	fun hello() {
		println("owo")
	}

	override fun putMember(key: String?, value: Value?) {
		println()
		println("put1 $key")
		println("put2 $value")
	}

	override fun getMemberKeys(): Any {
		return listOf<Any?>()
		// TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMember(key: String?): Any {
		return "a"
		// TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun hasMember(key: String?): Boolean {
		return false
		// TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}