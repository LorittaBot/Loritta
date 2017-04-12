package com.mrpowergamerbr.loritta.utils.webpaste;

import java.io.IOException;

public class BitlyURLShortener extends HttpAPIClient implements URLShortener
{
    private static final String GENERIC_BITLY_REQUEST_FORMAT = "https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s";
    private static final String USERNAME = "o_5s5av92lgs";
    private static final String API_KEY = "R_fb665e9e7f6a830134410d9eb7946cdf";
    
    public BitlyURLShortener() {
        super(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s", "R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs", "%s"));
    }
    
    @Override
    public String shorten(final String longUrl) {
        try {
            final String result = this.exec(longUrl);
            if (!result.startsWith("http://bit.ly/")) {
                throw new IOException(result);
            }
            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            return longUrl;
        }
    }
}