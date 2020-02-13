import nodecanvas.Buffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun writeFile(name: String, buffer: Buffer) {
	return suspendCoroutine { cont ->
		writeFile(name, buffer) { error: dynamic ->
			if (error == null)
				cont.resume(Unit)
			else {
				console.log("Exception! $error")
				cont.resumeWithException(RuntimeException())
			}
		}
	}
}