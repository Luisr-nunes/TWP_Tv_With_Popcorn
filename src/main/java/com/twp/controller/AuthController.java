package com.twp.controller;

import com.twp.App;
import com.twp.service.SupabaseClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.binding.Bindings;
import com.twp.service.TmdbClient;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private javafx.scene.layout.StackPane leftPane;

    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final TmdbClient tmdbClient = new TmdbClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        leftPane.setStyle("-fx-background-color: #050505;");
        
        com.twp.util.AsyncManager.runAsync(() -> {
            String jsonResponse = tmdbClient.getTrending();
            return mapper.readTree(jsonResponse);
        }).thenAcceptAsync(root -> {
            try {
                JsonNode results = root.path("results");
                if (results.isArray() && results.size() > 0) {
                    String backdrop = results.get(0).path("backdrop_path").asText("");
                    if (!backdrop.isEmpty() && !backdrop.equals("null")) {
                        Image bg = new Image("https://image.tmdb.org/t/p/w1280" + backdrop, true);
                        bg.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() == 1.0) {
                                leftPane.setBackground(new Background(new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
                            }
                        });
                    }
                }
            } catch (Exception e) {}
        }, Platform::runLater);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Preencha todos os campos.");
            statusLabel.setStyle("-fx-text-fill: #E53935;");
            return;
        }

        statusLabel.setText("Conectando...");
        statusLabel.setStyle("-fx-text-fill: #FFFFFF;");

        com.twp.util.AsyncManager.runAsync(() -> {
            return supabaseClient.signIn(email, password);
        }).thenAcceptAsync(success -> {
            if (success) {
                try {
                    App.setRoot("main"); // Direciona para o Dashboard
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                statusLabel.setText("Email ou senha inválidos.");
                statusLabel.setStyle("-fx-text-fill: #E53935;");
            }
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Erro na conexão: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #E53935;");
            });
            return null;
        });
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Preencha todos os campos.");
            statusLabel.setStyle("-fx-text-fill: #E53935;");
            return;
        }

        statusLabel.setText("Criando conta...");
        statusLabel.setStyle("-fx-text-fill: #FFFFFF;");

        com.twp.util.AsyncManager.runAsync(() -> {
            return supabaseClient.signUp(email, password);
        }).thenAcceptAsync(success -> {
            if (success) {
                statusLabel.setText("Conta criada! Confirme seu email ou faça login.");
                statusLabel.setStyle("-fx-text-fill: #FFD54F;");
            } else {
                statusLabel.setText("Falha ao criar conta. Tente novamente.");
                statusLabel.setStyle("-fx-text-fill: #E53935;");
            }
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Erro na conexão: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #E53935;");
            });
            return null;
        });
    }
}
