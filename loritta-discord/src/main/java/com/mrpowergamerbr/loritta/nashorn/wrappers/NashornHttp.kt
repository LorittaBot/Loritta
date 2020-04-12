import com.github.kevinsawicki.http.HttpRequest

class NashornHttp() {
    private val PROXY_URL = "https://cors-anywhere.herokuapp.com/"

    class HttpResult(public val code: Int, public val body: String) {
    }

    class HttpOptions {
        var method: String = "GET"
        var headers: Map<String, String> = emptyMap<String, String>()
        var body: String = ""
    }

    @NashornCommand.NashornDocs()
    fun http(uri: String, options: HttpOptions = HttpOptions()): HttpResult {
        try {
            val request = HttpRequest(PROXY_URL + uri, options.method)
                .connectTimeout(5_000)
                .readTimeout(5_000)
                .headers(options.headers)
                .body(options.body)

            return HttpResult(request.code(), request.body())
        } catch (e: Exception) {
            return HttpResult(0, "")
        }
    }
}