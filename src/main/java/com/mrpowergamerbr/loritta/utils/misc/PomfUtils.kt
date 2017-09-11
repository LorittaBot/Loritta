package com.mrpowergamerbr.loritta.utils.misc

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object PomfUtils {
	const val POMF_URL = "http://pomf.cat/upload.php"

	fun uploadFile(array: ByteArray): String? {
		return uploadFile(POMF_URL, "mirror.png", array)
	}

	fun uploadFile(array: ByteArray, fileName: String): String? {
		return uploadFile(POMF_URL, fileName, array)
	}

	fun uploadFile(url: String, fileName: String, array: ByteArray): String? {
		val client = OkHttpClient()
		val formBody = MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("files[]", fileName,
						RequestBody.create(MediaType.parse("image/png"), array))
				.build()
		val request = Request.Builder().url(url)
				.post(formBody).build()
		val response = client.newCall(request).execute()

		val json = JsonParser().parse(response.body()!!.string()).obj

		if (json.has("files")) {
			return "http://a.pomf.cat/${json["files"].array[0]["url"].string}"
		} else {
			return null
		}
	}
}