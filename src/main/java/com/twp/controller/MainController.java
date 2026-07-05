package com.twp.controller;

import com.twp.App;
import com.twp.service.TmdbClient;
import com.twp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainController {
    @FXML private TextField searchField;
    @FXML private FlowPane resultsPane;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final ObjectMapper mapper = new ObjectMapper();

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
                            
                            if (!mediaType.equals("person")) {
                                VBox card = createShowCard(title, mediaType);
                                resultsPane.getChildren().add(card);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsPane.getChildren().clear();
                    Label errorLabel = new Label("Erro na busca: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #E53935;");
                    resultsPane.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private VBox createShowCard(String title, String type) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15px; -fx-background-radius: 8px; -fx-min-width: 150px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);

        Label typeLabel = new Label(type.equals("movie") ? "Filme" : "Série");
        typeLabel.setStyle("-fx-text-fill: #FFD54F; -fx-font-size: 10px;");

        card.getChildren().addAll(titleLabel, typeLabel);
        return card;
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
