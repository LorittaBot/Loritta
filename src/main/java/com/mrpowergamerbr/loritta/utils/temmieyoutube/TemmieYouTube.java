package com.mrpowergamerbr.loritta.utils.temmieyoutube;

import com.github.kevinsawicki.http.HttpRequest;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.response.SearchResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TemmieYouTube {
	private String apiKey;
	
	public TemmieYouTube(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public SearchResponse searchOnYouTube(String searchQuery) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("part", "snippet");
		params.put("q", searchQuery);
		params.put("key", apiKey);
		HttpRequest req = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?" + buildQuery(params));

		String body = req.body();

		return Loritta.getGson().fromJson(body, SearchResponse.class);
	}

	public void searchOnGoogle(String searchQuery) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("q", searchQuery);
		params.put("cx", "017576662512468239146:omuauf_lfve"); // wtf is this?
		params.put("key", apiKey);
		HttpRequest req = HttpRequest.get("https://www.googleapis.com/customsearch/v1?" + buildQuery(params));

		String body = req.body();
		
		System.out.println(body);
		// return Loritta.getGson().fromJson(body, SearchResponse.class);
	}
	
	private String buildQuery(Map<String, Object> params) {
		String[] query = new String[params.size()];
		int index = 0;
		for (String key : params.keySet()) {
			String val = String.valueOf(params.get(key) != null ? params.get(key) : "");
			try {
				val = URLEncoder.encode(val, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			query[index++] = key+"="+val;
		}

		return StringUtils.join(query, "&");
	}
}
