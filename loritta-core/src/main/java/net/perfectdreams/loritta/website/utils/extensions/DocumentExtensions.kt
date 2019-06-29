package net.perfectdreams.loritta.website.utils.extensions

import org.w3c.dom.Document
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Document.transformToString(): String {
	try {
		val sw = StringWriter()
		val tf = TransformerFactory.newInstance()
		val transformer = tf.newTransformer()
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat")

		transformer.transform(DOMSource(this), StreamResult(sw))
		return sw.toString()
	} catch (ex: Exception) {
		throw RuntimeException("Error converting to String", ex)
	}
}