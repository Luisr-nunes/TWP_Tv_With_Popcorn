package com.twp.service;

import com.twp.util.EnvConfig;
import com.twp.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class SupabaseClient {
    private static final String SUPABASE_URL = EnvConfig.get("SUPABASE_URL");
    private static final String SUPABASE_KEY = EnvConfig.get("SUPABASE_KEY");
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public SupabaseClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean signUp(String email, String password) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/auth/v1/signup"))
                .header("apikey", SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    public boolean signIn(String email, String password) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/auth/v1/token?grant_type=password"))
                .header("apikey", SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            Session.accessToken = root.path("access_token").asText();
            Session.userId = root.path("user").path("id").asText();
            return true;
        }
        return false;
    }

    public String fetchTable(String tableName) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/" + tableName))
                .header("apikey", SUPABASE_KEY)
                .GET();
                
        if (Session.accessToken != null) {
            requestBuilder.header("Authorization", "Bearer " + Session.accessToken);
        } else {
            requestBuilder.header("Authorization", "Bearer " + SUPABASE_KEY);
        }
        
        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public boolean addShowToLibrary(String tmdbId, String title, String mediaType, String posterPath) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", Session.userId);
        payload.put("tmdb_id", tmdbId);
        payload.put("title", title);
        payload.put("media_type", mediaType);
        payload.put("poster_path", posterPath);
        
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows"))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            throw new Exception("HTTP " + response.statusCode() + " - " + response.body());
        }
        return true;
    }

    public String getUserLibrary() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows?user_id=eq." + Session.userId + "&order=created_at.desc"))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public boolean updateProgress(String tmdbId, int progress) throws Exception {
        String jsonBody = String.format("{\"progress\": %d}", progress);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows?user_id=eq." + Session.userId + "&tmdb_id=eq." + tmdbId))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao atualizar progresso: HTTP " + response.statusCode());
        }
        return true;
    }

    public boolean updateWatchedEpisodes(String tmdbId, String jsonArrayStr) throws Exception {
        String jsonBody = String.format("{\"watched_episodes\": %s}", jsonArrayStr);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows?user_id=eq." + Session.userId + "&tmdb_id=eq." + tmdbId))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao atualizar episódios: HTTP " + response.statusCode());
        }
        return true;
    }

    public boolean removeShowFromLibrary(String tmdbId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows?user_id=eq." + Session.userId + "&tmdb_id=eq." + tmdbId))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .DELETE()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao remover show: HTTP " + response.statusCode());
        }
        return true;
    }

    public boolean updateRating(String tmdbId, int rating) throws Exception {
        String jsonBody = String.format("{\"user_rating\": %d}", rating);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/user_shows?user_id=eq." + Session.userId + "&tmdb_id=eq." + tmdbId))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + Session.accessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao atualizar nota: HTTP " + response.statusCode());
        }
        return true;
    }
}
