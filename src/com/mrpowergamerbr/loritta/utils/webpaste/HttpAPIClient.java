package com.mrpowergamerbr.loritta.utils.webpaste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class HttpAPIClient
{
    protected final String urlFormat;
    
    public HttpAPIClient(final String urlFormat) {
        this.urlFormat = urlFormat;
    }
    
    protected final String exec(final Object... args) throws IOException {
        final URLConnection conn = new URL(String.format(this.urlFormat, args)).openConnection();
        conn.connect();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while (!reader.ready()) {}
        final StringBuilder ret = new StringBuilder();
        while (reader.ready()) {
            ret.append(reader.readLine()).append('\n');
        }
        reader.close();
        return ret.toString();
    }
}