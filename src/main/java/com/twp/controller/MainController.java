package com.twp.controller;

import com.twp.App;
import com.twp.service.SupabaseClient;
import com.twp.service.TmdbClient;
import com.twp.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.layout.VBox;
import com.twp.util.AsyncManager;

public class MainController {
    @FXML private TextField searchField;
    @FXML private FlowPane resultsPane;
    @FXML private FlowPane libraryPane;
    
    @FXML private VBox libraryTab;
    @FXML private VBox searchTab;
    @FXML private Button libraryTabBtn;
    @FXML private Button searchTabBtn;

    // Details View
    @FXML private StackPane details;
    @FXML private DetailsController detailsController;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w200";
    private static final String TMDB_IMAGE_LARGE = "https://image.tmdb.org/t/p/w500";

    @FXML
    public void initialize() {
        showSearchTab();
        handleSearch(); // Carrega os Trending automaticamente
    }

    @FXML
    private void showLibraryTab() {
        libraryTab.setVisible(true);
        searchTab.setVisible(false);
        libraryTabBtn.getStyleClass().add("tab-btn-active");
        searchTabBtn.getStyleClass().remove("tab-btn-active");
        loadLibrary();
    }

    @FXML
    private void showSearchTab() {
        searchTab.setVisible(true);
        libraryTab.setVisible(false);
        searchTabBtn.getStyleClass().add("tab-btn-active");
        libraryTabBtn.getStyleClass().remove("tab-btn-active");
    }

    private void loadLibrary() {
        libraryPane.getChildren().clear();
        Label loadingLabel = new Label("Carregando sua biblioteca...");
        loadingLabel.setStyle("-fx-text-fill: white;");
        libraryPane.getChildren().add(loadingLabel);

        AsyncManager.runAsync(() -> {
            String json = supabaseClient.getUserLibrary();
            return mapper.readTree(json);
        }).thenAcceptAsync(root -> {
            libraryPane.getChildren().clear();
            if (root.isArray() && root.size() > 0) {
                for (JsonNode item : root) {
                    String title = item.path("title").asText();
                    String type = item.path("media_type").asText();
                    String poster = item.path("poster_path").asText("");
                    int progress = item.path("progress").asInt(0);
                    
                    VBox card = createShowCard(item.path("tmdb_id").asText(), title, type, poster, "", true, progress);
                    libraryPane.getChildren().add(card);
                }
            } else {
                Label emptyLabel = new Label("Sua biblioteca está vazia. Vá em Buscar para adicionar!");
                emptyLabel.setStyle("-fx-text-fill: #888;");
                libraryPane.getChildren().add(emptyLabel);
            }
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                libraryPane.getChildren().clear();
                Label err = new Label("Erro ao carregar: " + e.getMessage());
                err.setStyle("-fx-text-fill: #E53935;");
                libraryPane.getChildren().add(err);
            });
            return null;
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        resultsPane.getChildren().clear();
        Label loadingLabel = new Label(query.isEmpty() ? "Carregando destaques do dia..." : "Buscando...");
        loadingLabel.setStyle("-fx-text-fill: white;");
        resultsPane.getChildren().add(loadingLabel);

        AsyncManager.runAsync(() -> {
            String jsonResponse = query.isEmpty() ? tmdbClient.getTrending() : tmdbClient.searchMulti(query);
            return mapper.readTree(jsonResponse);
        }).thenAcceptAsync(root -> {
            resultsPane.getChildren().clear();
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode item : results) {
                    String title = item.path("title").asText(item.path("name").asText("Sem Nome"));
                    String mediaType = item.path("media_type").asText("unknown");
                    String posterPath = item.path("poster_path").asText("");
                    String overview = item.path("overview").asText("Sem sinopse disponível.");
                    String id = item.path("id").asText();

                    if (!mediaType.equals("person") && !posterPath.isEmpty() && !posterPath.equals("null")) {
                        VBox card = createShowCard(id, title, mediaType, posterPath, overview, false, 0);
                        resultsPane.getChildren().add(card);
                    }
                }
            }
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                resultsPane.getChildren().clear();
                Label err = new Label("Erro na busca: " + e.getMessage());
                err.setStyle("-fx-text-fill: #E53935;");
                resultsPane.getChildren().add(err);
            });
            return null;
        });
    }

    private VBox createShowCard(String tmdbId, String title, String type, String posterPath, String overview, boolean inLibrary, int progress) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-min-width: 150px;");
        card.getStyleClass().add("card-hover");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(225);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 12px;");
        
        if (posterPath != null && !posterPath.isEmpty()) {
            Image image = new Image(TMDB_IMAGE_URL + posterPath, true);
            imageView.setImage(image);
        }

        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(imageView);

        if (!inLibrary) {
            Button quickAddBtn = new Button("+");
            quickAddBtn.setStyle("-fx-background-color: rgba(229, 57, 53, 0.9); -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;");
            StackPane.setAlignment(quickAddBtn, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(quickAddBtn, new javafx.geometry.Insets(10));
            
            quickAddBtn.setOnAction(e -> {
                e.consume(); // Não clica no card
                saveToLibraryQuick(tmdbId, title, type, posterPath, quickAddBtn);
            });
            imageContainer.getChildren().add(quickAddBtn);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 0 0 5px;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(150);

        card.getChildren().addAll(imageContainer, titleLabel);

        // Click event on card
        card.setOnMouseClicked(e -> {
            openDetails(tmdbId, type, inLibrary, progress);
        });

        return card;
    }

    private void openDetails(String tmdbId, String type, boolean inLibrary, int progress) {
        detailsController.loadDetails(tmdbId, type, inLibrary, progress);
        details.setVisible(true);
    }

    private void saveToLibraryQuick(String tmdbId, String title, String type, String posterPath, Button btn) {
        btn.setDisable(true);
        AsyncManager.runAsync(() -> {
            supabaseClient.addShowToLibrary(tmdbId, title, type, posterPath);
            return null;
        }).thenRunAsync(() -> {
            btn.setText("✓");
            btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-min-width: 40px; -fx-min-height: 40px;");
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                btn.setDisable(false);
                System.err.println(e.getMessage());
            });
            return null;
        });
    }

    @FXML
    private void handleLogout() {
        Session.accessToken = null;
        Session.userId = null;
        try {
            App.setRoot("auth");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
