package com.twp.service;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.twp.util.AsyncManager;
import javafx.application.Platform;

public class TvTimeImporter {

    private final TmdbClient tmdbClient;
    private final SupabaseClient supabaseClient;
    private final ObjectMapper mapper;

    public TvTimeImporter() {
        this.tmdbClient = new TmdbClient();
        this.supabaseClient = new SupabaseClient();
        this.mapper = new ObjectMapper();
    }

    public void importFromFolder(File folder, Consumer<String> progressCallback, Consumer<Boolean> onComplete) {
        AsyncManager.runAsync(() -> {
            try {
                File trackingFile = new File(folder, "tracking-prod-records-v2.csv");
                if (!trackingFile.exists()) {
                    Platform.runLater(() -> progressCallback.accept("Erro: Arquivo tracking-prod-records-v2.csv não encontrado na pasta selecionada."));
                    Platform.runLater(() -> onComplete.accept(false));
                    return;
                }

                Platform.runLater(() -> progressCallback.accept("Lendo histórico de episódios (isso pode levar alguns instantes)..."));
                List<String> lines = Files.readAllLines(trackingFile.toPath());
                
                if (lines.isEmpty()) {
                    throw new Exception("O arquivo CSV está vazio.");
                }
                
                String headerLine = lines.get(0);
                String[] headers = headerLine.split(",");
                int sidIndex = -1, seasonIndex = -1, epNumIndex = -1;
                for (int i=0; i<headers.length; i++) {
                    if (headers[i].equals("s_id")) sidIndex = i;
                    if (headers[i].equals("season_number")) seasonIndex = i;
                    if (headers[i].equals("episode_number")) epNumIndex = i;
                }
                
                if (sidIndex == -1 || seasonIndex == -1 || epNumIndex == -1) {
                    throw new Exception("Formato inválido. As colunas 's_id', 'season_number' e 'episode_number' não foram encontradas.");
                }

                Map<String, Map<Integer, Set<Integer>>> showsData = new HashMap<>();

                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.trim().isEmpty()) continue;
                    String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    
                    if (cols.length > Math.max(sidIndex, Math.max(seasonIndex, epNumIndex))) {
                        String sId = cols[sidIndex].replace("\"", "").trim();
                        String seasonStr = cols[seasonIndex].replace("\"", "").trim();
                        String epStr = cols[epNumIndex].replace("\"", "").trim();
                        
                        if (!sId.isEmpty() && !seasonStr.isEmpty() && !epStr.isEmpty()) {
                            try {
                                int season = Integer.parseInt(seasonStr);
                                int ep = Integer.parseInt(epStr);
                                
                                showsData.putIfAbsent(sId, new HashMap<>());
                                showsData.get(sId).putIfAbsent(season, new HashSet<>());
                                showsData.get(sId).get(season).add(ep);
                            } catch(Exception ignored) {}
                        }
                    }
                }
                
                int totalShows = showsData.size();
                int currentShow = 0;
                
                for (Map.Entry<String, Map<Integer, Set<Integer>>> entry : showsData.entrySet()) {
                    String tvdbId = entry.getKey();
                    Map<Integer, Set<Integer>> seasons = entry.getValue();
                    currentShow++;
                    
                    final int showNum = currentShow;
                    Platform.runLater(() -> progressCallback.accept(String.format("Processando série %d de %d...", showNum, totalShows)));
                    
                    try {
                        String findJson = tmdbClient.findTmdbIdByTvdbId(tvdbId);
                        JsonNode findRoot = mapper.readTree(findJson);
                        JsonNode tvResults = findRoot.path("tv_results");
                        if (tvResults.isArray() && tvResults.size() > 0) {
                            JsonNode showNode = tvResults.get(0);
                            String tmdbId = showNode.path("id").asText();
                            String title = showNode.path("name").asText();
                            String posterPath = showNode.path("poster_path").asText("");
                            
                            Platform.runLater(() -> progressCallback.accept(String.format("Mapeando histórico: %s", title)));
                            
                            String detailsJson = tmdbClient.getDetails(tmdbId, "tv");
                            JsonNode detailsRoot = mapper.readTree(detailsJson);
                            int runtime = 45;
                            JsonNode epTimes = detailsRoot.path("episode_run_time");
                            if (epTimes.isArray() && epTimes.size() > 0) {
                                runtime = epTimes.get(0).asInt(45);
                            } else if (detailsRoot.has("last_episode_to_air") && !detailsRoot.path("last_episode_to_air").path("runtime").isNull()) {
                                runtime = detailsRoot.path("last_episode_to_air").path("runtime").asInt(45);
                            } else if (detailsRoot.has("next_episode_to_air") && !detailsRoot.path("next_episode_to_air").path("runtime").isNull()) {
                                runtime = detailsRoot.path("next_episode_to_air").path("runtime").asInt(45);
                            }
                            
                            supabaseClient.addShowToLibrary(tmdbId, title, "tv", posterPath, "WATCHING", runtime);
                            
                            Set<Integer> watchedTmdbEpIds = new HashSet<>();
                            for (Map.Entry<Integer, Set<Integer>> seasonEntry : seasons.entrySet()) {
                                int seasonNumber = seasonEntry.getKey();
                                Set<Integer> watchedEps = seasonEntry.getValue();
                                
                                try {
                                    String seasonJson = tmdbClient.getSeasonDetails(tmdbId, seasonNumber);
                                    JsonNode seasonRoot = mapper.readTree(seasonJson);
                                    JsonNode epsNode = seasonRoot.path("episodes");
                                    if (epsNode.isArray()) {
                                        for (JsonNode epNode : epsNode) {
                                            int epNum = epNode.path("episode_number").asInt();
                                            if (watchedEps.contains(epNum)) {
                                                watchedTmdbEpIds.add(epNode.path("id").asInt());
                                            }
                                        }
                                    }
                                } catch(Exception e) {
                                    // Ignorar temporada indisponível no TMDB
                                }
                            }
                            
                            ArrayNode arr = mapper.createArrayNode();
                            for (Integer id : watchedTmdbEpIds) arr.add(id);
                            supabaseClient.updateWatchedEpisodes(tmdbId, arr.toString());
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao processar TVDB " + tvdbId + ": " + ex.getMessage());
                    }
                }

                Platform.runLater(() -> progressCallback.accept("Migração finalizada com sucesso! Seus dados foram salvos."));
                Platform.runLater(() -> onComplete.accept(true));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> progressCallback.accept("Falha na importação: " + e.getMessage()));
                Platform.runLater(() -> onComplete.accept(false));
            }
        });
    }
}
