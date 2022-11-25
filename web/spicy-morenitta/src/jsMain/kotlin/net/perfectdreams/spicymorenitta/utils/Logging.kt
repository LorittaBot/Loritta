package net.perfectdreams.spicymorenitta.utils

interface Logging {
    fun success(vararg o: Any?) {
        console.log("%c[${this::class.simpleName}] [\uD83D\uDE0A success] ${o.first()}", "color: green;", *o.drop(1).toTypedArray())
    }

    fun info(vararg o: Any?) {
        console.log("%c[${this::class.simpleName}] [\uD83E\uDD14 info] ${o.first()}", "color: blue;", *o.drop(1).toTypedArray())
    }

    fun warn(vararg o: Any?) {
        console.log("%c[${this::class.simpleName}] [\uD83D\uDE44 warn] ${o.first()}", "color: pink;", *o.drop(1).toTypedArray())
    }

    fun error(vararg o: Any?) {
        console.log("%c[${this::class.simpleName}] [\uD83D\uDCA5 error] ${o.first()}", "color: red;", *o.drop(1).toTypedArray())
    }

    fun debug(vararg o: Any?) {
        console.log("%c[${this::class.simpleName}] [\uD83D\uDC40 debug] ${o.first()}", "color: gray;", *o.drop(1).toTypedArray())
    }
}