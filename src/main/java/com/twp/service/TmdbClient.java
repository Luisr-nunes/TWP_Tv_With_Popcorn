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

    private String getApiToken() {
        return API_TOKEN;
    }

    // Busca unificada por Filmes e Séries
    public String searchMulti(String query) throws Exception {
        String url = String.format("%s/search/multi?query=%s&language=pt-BR&page=1", BASE_URL, URLEncoder.encode(query, StandardCharsets.UTF_8));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getApiToken())
                .header("accept", "application/json")
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getTrending() throws Exception {
        String url = String.format("%s/trending/all/day?language=pt-BR", BASE_URL);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getApiToken())
                .header("accept", "application/json")
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getDetails(String id, String mediaType) throws Exception {
        String url = String.format("%s/%s/%s?append_to_response=credits,images,watch/providers&language=pt-BR", BASE_URL, mediaType, id);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getApiToken())
                .header("accept", "application/json")
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getSeasonDetails(String tmdbId, int seasonNumber) throws Exception {
        String url = String.format("%s/tv/%s/season/%d?language=pt-BR", BASE_URL, tmdbId, seasonNumber);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getApiToken())
                .header("accept", "application/json")
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
