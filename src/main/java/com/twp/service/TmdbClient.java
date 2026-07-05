package com.twp.service;

import com.twp.util.EnvConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TmdbClient {
    private static final String API_TOKEN = EnvConfig.get("TMDB_API_TOKEN");
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final HttpClient httpClient;

    public TmdbClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Busca unificada por Filmes e Séries
    public String searchMulti(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/search/multi?query=" + encodedQuery + "&language=pt-BR"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
