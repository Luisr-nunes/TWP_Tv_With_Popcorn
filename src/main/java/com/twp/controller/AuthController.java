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

public class AuthController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ImageView bannerImage;
    @FXML private javafx.scene.layout.StackPane leftPane;

    private final SupabaseClient supabaseClient = new SupabaseClient();

    @FXML
    public void initialize() {
        // Ajusta a imagem dinamicamente ao tamanho da tela
        bannerImage.fitWidthProperty().bind(leftPane.widthProperty());
        bannerImage.fitHeightProperty().bind(leftPane.heightProperty());
        
        // Carrega o poster do filme Interstellar como fundo do login (tamanho w780 do TMDB)
        bannerImage.setImage(new Image("https://image.tmdb.org/t/p/w780/gEU2QlsEOW3XZ101rMWiP3OPXWE.jpg", true));
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
