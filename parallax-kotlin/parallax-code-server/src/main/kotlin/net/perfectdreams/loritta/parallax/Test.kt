package net.perfectdreams.loritta.parallax

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import java.io.File

fun main() {
    val reader = ClassReader(File("L:\\kotlin-compiler-1.3.72\\temp\\CUSTOM_COMPILED_CODEKt.class").inputStream())

    println(reader.className)
}

class Visitor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {
    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        println(name)

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}