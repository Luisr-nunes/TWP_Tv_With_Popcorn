package com.twp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twp.service.SupabaseClient;
import com.twp.service.TmdbClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DetailsController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backdropImage;
    @FXML private ImageView posterImage;
    @FXML private Label titleLabel;
    @FXML private Label ratingLabel;
    @FXML private Label genresLabel;
    @FXML private Label overviewLabel;
    @FXML private Button addBtn;
    @FXML private HBox castBox;
    @FXML private HBox providersBox;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/";
    private String currentTmdbId;
    private String currentType;
    private String currentTitle;
    private String currentPosterPath;

    public void loadDetails(String tmdbId, String type, boolean inLibrary) {
        this.currentTmdbId = tmdbId;
        this.currentType = type;
        
        // Reset view
        backdropImage.setImage(null);
        posterImage.setImage(null);
        titleLabel.setText("Carregando...");
        overviewLabel.setText("");
        ratingLabel.setText("");
        genresLabel.setText("");
        castBox.getChildren().clear();
        providersBox.getChildren().clear();

        if (inLibrary) {
            addBtn.setText("Já adicionado");
            addBtn.setDisable(true);
        } else {
            addBtn.setText("Adicionar à Minha Lista");
            addBtn.setDisable(false);
        }

        new Thread(() -> {
            try {
                String json = tmdbClient.getDetails(tmdbId, type);
                JsonNode root = mapper.readTree(json);

                Platform.runLater(() -> {
                    currentTitle = root.path(type.equals("movie") ? "title" : "name").asText();
                    titleLabel.setText(currentTitle);
                    overviewLabel.setText(root.path("overview").asText("Sem sinopse disponível."));
                    
                    double rating = root.path("vote_average").asDouble();
                    ratingLabel.setText(String.format("★ %.1f", rating));

                    currentPosterPath = root.path("poster_path").asText("");
                    if (!currentPosterPath.isEmpty()) {
                        posterImage.setImage(new Image(TMDB_IMAGE_BASE + "w500" + currentPosterPath, true));
                    }

                    String backdropPath = root.path("backdrop_path").asText("");
                    if (!backdropPath.isEmpty()) {
                        backdropImage.setImage(new Image(TMDB_IMAGE_BASE + "original" + backdropPath, true));
                    }

                    // Genres
                    StringBuilder genres = new StringBuilder();
                    for (JsonNode g : root.path("genres")) {
                        genres.append(g.path("name").asText()).append(" • ");
                    }
                    if (genres.length() > 0) genresLabel.setText(genres.substring(0, genres.length() - 3));

                    // Cast
                    JsonNode cast = root.path("credits").path("cast");
                    for (int i = 0; i < Math.min(cast.size(), 12); i++) {
                        JsonNode actor = cast.get(i);
                        String profile = actor.path("profile_path").asText("");
                        if (!profile.isEmpty() && !profile.equals("null")) {
                            VBox actorBox = new VBox(10);
                            actorBox.setAlignment(javafx.geometry.Pos.TOP_CENTER);
                            
                            ImageView profImg = new ImageView(new Image(TMDB_IMAGE_BASE + "w200" + profile, true));
                            profImg.setFitWidth(100);
                            profImg.setFitHeight(150);
                            profImg.setPreserveRatio(true);
                            
                            Label name = new Label(actor.path("name").asText());
                            name.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                            name.setMaxWidth(100);
                            name.setWrapText(true);
                            name.setAlignment(javafx.geometry.Pos.CENTER);
                            
                            actorBox.getChildren().addAll(profImg, name);
                            castBox.getChildren().add(actorBox);
                        }
                    }

                    // Providers
                    JsonNode brProviders = root.path("watch/providers").path("results").path("BR");
                    if (brProviders.isMissingNode() || (!brProviders.has("flatrate") && !brProviders.has("rent"))) {
                        Label lbl = new Label("Não disponível em streaming no Brasil.");
                        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 16px;");
                        providersBox.getChildren().add(lbl);
                    } else {
                        JsonNode flatrate = brProviders.has("flatrate") ? brProviders.path("flatrate") : brProviders.path("rent");
                        for (JsonNode prov : flatrate) {
                            String logoPath = prov.path("logo_path").asText("");
                            if (!logoPath.isEmpty()) {
                                ImageView logo = new ImageView(new Image(TMDB_IMAGE_BASE + "w92" + logoPath, true));
                                logo.setFitWidth(60);
                                logo.setFitHeight(60);
                                providersBox.getChildren().add(logo);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> titleLabel.setText("Erro ao carregar detalhes."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAdd() {
        addBtn.setDisable(true);
        addBtn.setText("Adicionando...");
        new Thread(() -> {
            try {
                boolean success = supabaseClient.addShowToLibrary(currentTmdbId, currentTitle, currentType, currentPosterPath);
                Platform.runLater(() -> {
                    addBtn.setText("Já adicionado");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addBtn.setText("Erro. Tentar novamente");
                    addBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        rootPane.setVisible(false);
    }
}
