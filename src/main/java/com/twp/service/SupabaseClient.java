package com.twp.service;

import com.twp.util.EnvConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseClient {
    private static final String SUPABASE_URL = EnvConfig.get("SUPABASE_URL");
    private static final String SUPABASE_KEY = EnvConfig.get("SUPABASE_KEY");
    private final HttpClient httpClient;

    public SupabaseClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Método genérico para buscar dados em uma tabela (Exemplo)
    public String fetchTable(String tableName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/" + tableName))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
