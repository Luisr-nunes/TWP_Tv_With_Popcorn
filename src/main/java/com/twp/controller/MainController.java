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

public class MainController {
    @FXML private TextField searchField;
    @FXML private FlowPane resultsPane;
    @FXML private FlowPane libraryPane;
    
    @FXML private VBox libraryTab;
    @FXML private VBox searchTab;
    @FXML private Button libraryTabBtn;
    @FXML private Button searchTabBtn;

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private ImageView modalPoster;
    @FXML private Label modalTitle;
    @FXML private Label modalType;
    @FXML private Label modalOverview;
    @FXML private Button modalAddBtn;
    @FXML private Label modalStatus;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w200";
    private static final String TMDB_IMAGE_LARGE = "https://image.tmdb.org/t/p/w500";

    @FXML
    public void initialize() {
        showLibraryTab();
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

        new Thread(() -> {
            try {
                String json = supabaseClient.getUserLibrary();
                JsonNode root = mapper.readTree(json);

                Platform.runLater(() -> {
                    libraryPane.getChildren().clear();
                    if (root.isArray() && root.size() > 0) {
                        for (JsonNode item : root) {
                            String title = item.path("title").asText();
                            String type = item.path("media_type").asText();
                            String poster = item.path("poster_path").asText("");
                            
                            VBox card = createShowCard(item.path("tmdb_id").asText(), title, type, poster, "", true);
                            libraryPane.getChildren().add(card);
                        }
                    } else {
                        Label emptyLabel = new Label("Sua biblioteca está vazia. Vá em Buscar para adicionar!");
                        emptyLabel.setStyle("-fx-text-fill: #888;");
                        libraryPane.getChildren().add(emptyLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    libraryPane.getChildren().clear();
                    Label err = new Label("Erro ao carregar: " + e.getMessage());
                    err.setStyle("-fx-text-fill: #E53935;");
                    libraryPane.getChildren().add(err);
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query.isEmpty()) return;

        resultsPane.getChildren().clear();
        Label loadingLabel = new Label("Buscando...");
        loadingLabel.setStyle("-fx-text-fill: white;");
        resultsPane.getChildren().add(loadingLabel);

        new Thread(() -> {
            try {
                String jsonResponse = tmdbClient.searchMulti(query);
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode results = root.path("results");

                Platform.runLater(() -> {
                    resultsPane.getChildren().clear();
                    if (results.isArray()) {
                        for (JsonNode item : results) {
                            String title = item.path("title").asText(item.path("name").asText("Sem Nome"));
                            String mediaType = item.path("media_type").asText("unknown");
                            String posterPath = item.path("poster_path").asText("");
                            String overview = item.path("overview").asText("Sem sinopse disponível.");
                            String id = item.path("id").asText();

                            if (!mediaType.equals("person") && !posterPath.isEmpty() && !posterPath.equals("null")) {
                                VBox card = createShowCard(id, title, mediaType, posterPath, overview, false);
                                resultsPane.getChildren().add(card);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsPane.getChildren().clear();
                    Label err = new Label("Erro na busca: " + e.getMessage());
                    err.setStyle("-fx-text-fill: #E53935;");
                    resultsPane.getChildren().add(err);
                });
            }
        }).start();
    }

    private VBox createShowCard(String tmdbId, String title, String type, String posterPath, String overview, boolean inLibrary) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 10px; -fx-background-radius: 8px; -fx-min-width: 150px;");
        card.getStyleClass().add("card-hover");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(130);
        imageView.setFitHeight(195);
        imageView.setPreserveRatio(true);
        
        if (posterPath != null && !posterPath.isEmpty()) {
            Image image = new Image(TMDB_IMAGE_URL + posterPath, true);
            imageView.setImage(image);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);

        card.getChildren().addAll(imageView, titleLabel);

        // Click event on card
        card.setOnMouseClicked(e -> {
            openModal(tmdbId, title, type, posterPath, overview, inLibrary);
        });

        return card;
    }

    private void openModal(String tmdbId, String title, String type, String posterPath, String overview, boolean inLibrary) {
        modalTitle.setText(title);
        modalType.setText(type.equals("movie") ? "Filme" : "Série");
        modalOverview.setText(overview);
        modalStatus.setText("");
        
        if (posterPath != null && !posterPath.isEmpty()) {
            modalPoster.setImage(new Image(TMDB_IMAGE_LARGE + posterPath, true));
        } else {
            modalPoster.setImage(null);
        }

        if (inLibrary) {
            modalAddBtn.setText("Já adicionado");
            modalAddBtn.setDisable(true);
        } else {
            modalAddBtn.setText("Adicionar à Lista");
            modalAddBtn.setDisable(false);
            modalAddBtn.setOnAction(e -> {
                saveToLibrary(tmdbId, title, type, posterPath);
            });
        }

        modalOverlay.setVisible(true);
    }

    private void saveToLibrary(String tmdbId, String title, String type, String posterPath) {
        modalAddBtn.setDisable(true);
        modalStatus.setText("Adicionando...");

        new Thread(() -> {
            try {
                boolean success = supabaseClient.addShowToLibrary(tmdbId, title, type, posterPath);
                Platform.runLater(() -> {
                    if (success) {
                        modalStatus.setText("Salvo!");
                        modalAddBtn.setText("Já adicionado");
                    } else {
                        modalStatus.setText("Falha ao salvar.");
                        modalAddBtn.setDisable(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    modalStatus.setText("Erro: " + e.getMessage());
                    modalAddBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void closeModal() {
        modalOverlay.setVisible(false);
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
