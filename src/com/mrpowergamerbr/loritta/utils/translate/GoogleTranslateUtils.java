package com.mrpowergamerbr.loritta.utils.translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;

public class GoogleTranslateUtils {
	/**
	 * Traduz uma mensagem de uma língua para outra
	 * 
	 * Criado por Kurimatzu
	 * 
	 * @param message
	 * @param from
	 * @param to
	 * @return A mensagem traduzida, caso seja null, quer dizer que falhou ao traduzir a mensagem
	 */
	public static String translate(String message, String from ,String to) {
        try {
            message = URLEncoder.encode(message, "UTF-8");
            URL url = new URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + from + "&tl="
                    + to + "&dt=t&q=" + message + "&ie=UTF-8&oe=UTF-8");
            URLConnection uc = url.openConnection();
            uc.setRequestProperty("User-Agent", "Mozilla/5.0"); 
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            // CHANGED!!!!
            String s = builder.toString();
            JSONArray jsonArray = new JSONArray(s);
            JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
            JSONArray jsonArray3 = (JSONArray) jsonArray2.get(0);
            in.close();
            return jsonArray3.get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
