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

    private final SupabaseClient supabaseClient = new SupabaseClient();

    @FXML
    public void initialize() {
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

        new Thread(() -> {
            try {
                boolean success = supabaseClient.signIn(email, password);
                Platform.runLater(() -> {
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
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erro na conexão: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #E53935;");
                });
            }
        }).start();
    }

    @FXML
    private void handleSignup() {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Preencha todos os campos.");
            statusLabel.setStyle("-fx-text-fill: #E53935;");
            return;
        }

        statusLabel.setText("Criando conta...");
        statusLabel.setStyle("-fx-text-fill: #FFFFFF;");

        new Thread(() -> {
            try {
                boolean success = supabaseClient.signUp(email, password);
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setText("Conta criada! Confirme seu email ou faça login.");
                        statusLabel.setStyle("-fx-text-fill: #FFD54F;");
                    } else {
                        statusLabel.setText("Falha ao criar conta. Tente novamente.");
                        statusLabel.setStyle("-fx-text-fill: #E53935;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erro na conexão: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #E53935;");
                });
            }
        }).start();
    }
}
