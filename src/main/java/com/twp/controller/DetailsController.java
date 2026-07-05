package com.twp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.twp.service.SupabaseClient;
import com.twp.service.TmdbClient;
import com.twp.util.AsyncManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.util.HashSet;
import java.util.Set;

public class DetailsController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backdropImage;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private ImageView posterImage;
    @FXML private Label directedByLabel;
    @FXML private Label overviewLabel;
    @FXML private HBox genresBox;
    @FXML private Button addBtn;
    @FXML private Label ratingLabel;
    @FXML private TilePane galleryPane;
    @FXML private VBox episodesSection;
    @FXML private ComboBox<String> seasonCombo;
    @FXML private VBox episodesBox;
    @FXML private VBox castBox;
    @FXML private HBox providersBox;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/";
    private String currentTmdbId;
    private String currentType;
    private String currentTitle;
    private String currentPosterPath;
    private Set<Integer> watchedEpisodes = new HashSet<>();

    public void loadDetails(String tmdbId, String type, boolean inLibrary, String watchedEpisodesJson) {
        this.currentTmdbId = tmdbId;
        this.currentType = type;
        
        // Parse watched episodes
        this.watchedEpisodes.clear();
        try {
            JsonNode arr = mapper.readTree(watchedEpisodesJson);
            if (arr.isArray()) {
                for (JsonNode ep : arr) {
                    this.watchedEpisodes.add(ep.asInt());
                }
            }
        } catch(Exception ignored) {}

        // Reset view
        backdropImage.setImage(null);
        posterImage.setImage(null);
        titleLabel.setText("");
        metaLabel.setText("");
        directedByLabel.setText("");
        overviewLabel.setText("Carregando...");
        genresBox.getChildren().clear();
        ratingLabel.setText("");
        galleryPane.getChildren().clear();
        castBox.getChildren().clear();
        providersBox.getChildren().clear();
        seasonCombo.getItems().clear();
        episodesBox.getChildren().clear();

        backdropImage.fitWidthProperty().bind(rootPane.widthProperty());

        if (inLibrary) {
            addBtn.setVisible(false);
            addBtn.setManaged(false);
            if (type.equals("tv")) {
                episodesSection.setVisible(true);
                episodesSection.setManaged(true);
            } else {
                episodesSection.setVisible(false);
                episodesSection.setManaged(false);
            }
        } else {
            addBtn.setVisible(true);
            addBtn.setManaged(true);
            addBtn.setText("Adicionar à Minha Lista");
            addBtn.setDisable(false);
            episodesSection.setVisible(false);
            episodesSection.setManaged(false);
        }

        AsyncManager.runAsync(() -> {
            String jsonResponse = tmdbClient.getDetails(tmdbId, type);
            return mapper.readTree(jsonResponse);
        }).thenAcceptAsync(root -> {
            String backdropPath = root.path("backdrop_path").asText("");
            if (!backdropPath.isEmpty() && !backdropPath.equals("null")) {
                backdropImage.setImage(new Image(TMDB_IMAGE_BASE + "w1280" + backdropPath, true));
            }
            
            currentPosterPath = root.path("poster_path").asText("");
            if (!currentPosterPath.isEmpty() && !currentPosterPath.equals("null")) {
                posterImage.setImage(new Image(TMDB_IMAGE_BASE + "w342" + currentPosterPath, true));
            }

            currentTitle = root.path(type.equals("movie") ? "title" : "name").asText();
            titleLabel.setText(currentTitle);
            overviewLabel.setText(root.path("overview").asText("Sem sinopse disponível."));
            
            double rating = root.path("vote_average").asDouble();
            ratingLabel.setText(String.format("%.1f", rating));

            // Meta Label
            String year = root.path(type.equals("movie") ? "release_date" : "first_air_date").asText("").split("-")[0];
            String language = root.path("original_language").asText().toUpperCase();
            String eps = type.equals("movie") ? root.path("runtime").asInt() + " min" : root.path("number_of_seasons").asInt() + " Temporadas (" + root.path("number_of_episodes").asInt() + " eps)";
            metaLabel.setText(String.format("%s | %s | Idioma: %s", year, eps, language));

            // Genres
            for (JsonNode genre : root.path("genres")) {
                Label gLabel = new Label(genre.path("name").asText());
                gLabel.getStyleClass().add("genre-pill");
                genresBox.getChildren().add(gLabel);
            }

            // Directed By
            if (type.equals("movie")) {
                for (JsonNode crew : root.path("credits").path("crew")) {
                    if (crew.path("job").asText().equals("Director")) {
                        directedByLabel.setText("Direção: " + crew.path("name").asText());
                        break;
                    }
                }
            } else {
                JsonNode createdBy = root.path("created_by");
                if (createdBy.isArray() && createdBy.size() > 0) {
                    directedByLabel.setText("Criação: " + createdBy.get(0).path("name").asText());
                }
            }

            // Gallery
            JsonNode backdrops = root.path("images").path("backdrops");
            int count = 0;
            for (JsonNode b : backdrops) {
                if (count >= 6) break; // Limit gallery to 6 images
                ImageView gv = new ImageView(new Image(TMDB_IMAGE_BASE + "w300" + b.path("file_path").asText(), true));
                gv.setFitWidth(280);
                gv.setPreserveRatio(true);
                gv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");
                galleryPane.getChildren().add(gv);
                count++;
            }

            // Cast
            JsonNode cast = root.path("credits").path("cast");
            int castCount = 0;
            for (JsonNode actor : cast) {
                if (castCount >= 5) break;
                Label actorLabel = new Label(actor.path("name").asText() + " como " + actor.path("character").asText());
                actorLabel.setStyle("-fx-text-fill: #DDDDDD;");
                castBox.getChildren().add(actorLabel);
                castCount++;
            }

            // Providers
            JsonNode brProviders = root.path("watch/providers").path("results").path("BR");
            if (!brProviders.isMissingNode()) {
                JsonNode flatrate = brProviders.path("flatrate");
                if (flatrate.isMissingNode()) flatrate = brProviders.path("rent");
                if (!flatrate.isMissingNode()) {
                    for (JsonNode provider : flatrate) {
                        String logo = provider.path("logo_path").asText();
                        ImageView logoView = new ImageView(new Image(TMDB_IMAGE_BASE + "w92" + logo, true));
                        logoView.setFitWidth(50);
                        logoView.setPreserveRatio(true);
                        providersBox.getChildren().add(logoView);
                    }
                }
            }

            // Seasons Combo
            if (type.equals("tv") && inLibrary) {
                seasonCombo.setOnAction(null);
                seasonCombo.getItems().clear();
                for (JsonNode s : root.path("seasons")) {
                    int snum = s.path("season_number").asInt();
                    if (snum > 0) {
                        seasonCombo.getItems().add("Temporada " + snum);
                    }
                }
                seasonCombo.setOnAction(e -> handleSeasonChange());
                if (!seasonCombo.getItems().isEmpty()) {
                    seasonCombo.getSelectionModel().selectFirst();
                }
            }

        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> overviewLabel.setText("Erro ao carregar detalhes: " + e.getMessage()));
            return null;
        });
    }

    private void handleSeasonChange() {
        String selected = seasonCombo.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int seasonNumber = Integer.parseInt(selected.replace("Temporada ", ""));

        episodesBox.getChildren().clear();
        Label loadingLabel = new Label("Carregando episódios...");
        loadingLabel.setStyle("-fx-text-fill: white;");
        episodesBox.getChildren().add(loadingLabel);

        AsyncManager.runAsync(() -> {
            String json = tmdbClient.getSeasonDetails(currentTmdbId, seasonNumber);
            return mapper.readTree(json);
        }).thenAcceptAsync(root -> {
            episodesBox.getChildren().clear();
            JsonNode episodes = root.path("episodes");
            for (JsonNode ep : episodes) {
                int epId = ep.path("id").asInt();
                int epNum = ep.path("episode_number").asInt();
                String epName = ep.path("name").asText();
                
                CheckBox cb = new CheckBox(epNum + ". " + epName);
                cb.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
                cb.setSelected(watchedEpisodes.contains(epId));
                
                cb.setOnAction(ev -> {
                    if (cb.isSelected()) watchedEpisodes.add(epId);
                    else watchedEpisodes.remove(epId);
                    saveEpisodesToCloud();
                });
                
                episodesBox.getChildren().add(cb);
            }
        }, Platform::runLater);
    }

    private void saveEpisodesToCloud() {
        try {
            ArrayNode arr = mapper.createArrayNode();
            for (Integer id : watchedEpisodes) arr.add(id);
            String jsonArray = arr.toString();
            
            AsyncManager.runAsync(() -> {
                return supabaseClient.updateWatchedEpisodes(currentTmdbId, jsonArray);
            }).exceptionally(e -> {
                System.err.println("Erro ao salvar episódios: " + e.getMessage());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        addBtn.setDisable(true);
        addBtn.setText("Adicionando...");
        AsyncManager.runAsync(() -> {
            return supabaseClient.addShowToLibrary(currentTmdbId, currentTitle, currentType, currentPosterPath);
        }).thenAcceptAsync(success -> {
            if (success) {
                addBtn.setText("Adicionado com Sucesso!");
            } else {
                addBtn.setText("Erro. Tente novamente");
                addBtn.setDisable(false);
            }
        }, Platform::runLater);
    }

    @FXML
    private void handleBack() {
        rootPane.setVisible(false);
    }
}
