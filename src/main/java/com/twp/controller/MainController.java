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
import com.twp.util.AsyncManager;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;

public class MainController {
    @FXML private StackPane rootPane;
    @FXML private StackPane mainBgPane;
    @FXML private TextField searchField;
    
    // Navigation Tabs
    @FXML private Button tabAll;
    @FXML private Button tabMovies;
    @FXML private Button tabTv;
    @FXML private Button tabAnime;

    // Scrolls
    @FXML private ScrollPane homeScroll;
    @FXML private ScrollPane libraryScroll;
    @FXML private ScrollPane searchScroll;
    @FXML private ScrollPane settingsScroll;
    @FXML private Label settingsEmailLabel;
    @FXML private Label importProgressLabel;

    // Home Area
    @FXML private StackPane heroPane;
    @FXML private HBox heroGenres;
    @FXML private Label heroTitle;
    @FXML private Label heroOverview;
    @FXML private Label carouselTitle;
    @FXML private HBox recommendationsBox;

    // Grid Areas
    @FXML private FlowPane libraryPane;
    @FXML private FlowPane resultsPane;
    
    // Stats Area
    @FXML private Label libraryTitle;
    @FXML private HBox statsBox;
    @FXML private Label statsTime;
    @FXML private Label statsEps;
    @FXML private Label statsMovies;

    // Details View
    @FXML private StackPane details;
    @FXML private DetailsController detailsController;

    private final TmdbClient tmdbClient = new TmdbClient();
    private final SupabaseClient supabaseClient = new SupabaseClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w200";
    private static final String TMDB_IMAGE_LARGE = "https://image.tmdb.org/t/p/w1280";

    private String heroTmdbId;
    private String heroType;
    private String heroPoster;

    @FXML
    public void initialize() {
        detailsController.setOnCloseCallback(() -> {
            if (libraryScroll.isVisible()) {
                boolean isLib = libraryTitle.getText().equals("Minha Biblioteca");
                loadLibrary(isLib);
            }
        });

        showHome();
        loadAll(); // Carrega Trending All por padrão
    }

    @FXML
    private void showHome() {
        homeScroll.setVisible(true);
        libraryScroll.setVisible(false);
        searchScroll.setVisible(false);
        if (settingsScroll != null) settingsScroll.setVisible(false);
    }

    @FXML
    private void showLibrary() {
        homeScroll.setVisible(false);
        libraryScroll.setVisible(true);
        searchScroll.setVisible(false);
        if (settingsScroll != null) settingsScroll.setVisible(false);
        setActiveTab(null);
        
        libraryTitle.setText("Minha Biblioteca");
        statsBox.setVisible(true);
        statsBox.setManaged(true);
        
        loadLibrary(true); // true = library, false = wishlist
    }

    @FXML
    private void showWishlist() {
        homeScroll.setVisible(false);
        libraryScroll.setVisible(true);
        searchScroll.setVisible(false);
        if (settingsScroll != null) settingsScroll.setVisible(false);
        setActiveTab(null);
        
        libraryTitle.setText("Lista de Desejos");
        statsBox.setVisible(false);
        statsBox.setManaged(false);
        
        loadLibrary(false);
    }

    @FXML
    private void handleLogout() throws Exception {
        Session.clear();
        App.setRoot("auth");
    }
    
    @FXML
    private void showSettings() {
        homeScroll.setVisible(false);
        libraryScroll.setVisible(false);
        searchScroll.setVisible(false);
        if (settingsScroll != null) settingsScroll.setVisible(true);
        setActiveTab(null);
        if (settingsEmailLabel != null && Session.userId != null) {
            settingsEmailLabel.setText("ID do Usuário: " + Session.userId.substring(0, Math.min(8, Session.userId.length())) + "...");
        }
    }
    
    @FXML
    private void handleImportTvTime() {
        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta gdpr-data do TV Time");
        
        java.io.File selectedDirectory = directoryChooser.showDialog(rootPane.getScene().getWindow());
        
        if (selectedDirectory != null) {
            importProgressLabel.setText("Iniciando importação...");
            com.twp.service.TvTimeImporter importer = new com.twp.service.TvTimeImporter();
            importer.importFromFolder(selectedDirectory, 
                msg -> importProgressLabel.setText(msg),
                success -> {
                    if (success) {
                        importProgressLabel.setText("Importação finalizada! Seus dados estão na biblioteca.");
                        loadLibrary(true);
                    }
                }
            );
        }
    }

    private void setActiveTab(Button activeBtn) {
        tabAll.getStyleClass().remove("nav-tab-active"); tabAll.getStyleClass().add("nav-tab");
        tabMovies.getStyleClass().remove("nav-tab-active"); tabMovies.getStyleClass().add("nav-tab");
        tabTv.getStyleClass().remove("nav-tab-active"); tabTv.getStyleClass().add("nav-tab");
        tabAnime.getStyleClass().remove("nav-tab-active"); tabAnime.getStyleClass().add("nav-tab");
        
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("nav-tab");
            activeBtn.getStyleClass().add("nav-tab-active");
        }
    }

    @FXML
    private void loadAll() {
        setActiveTab(tabAll);
        showHome();
        carouselTitle.setText("Em Alta");
        fetchCategory(() -> tmdbClient.getTrending());
    }

    @FXML
    private void loadMovies() {
        setActiveTab(tabMovies);
        showHome();
        carouselTitle.setText("Filmes em Alta");
        fetchCategory(() -> tmdbClient.getTrendingMovies());
    }

    @FXML
    private void loadTv() {
        setActiveTab(tabTv);
        showHome();
        carouselTitle.setText("Séries em Alta");
        fetchCategory(() -> tmdbClient.getTrendingTv());
    }

    @FXML
    private void loadAnime() {
        setActiveTab(tabAnime);
        showHome();
        carouselTitle.setText("Animes em Alta");
        fetchCategory(() -> tmdbClient.getAnime());
    }

    private interface Fetcher {
        String fetch() throws Exception;
    }

    private void fetchCategory(Fetcher fetcher) {
        recommendationsBox.getChildren().clear();
        Label loading = new Label("Carregando...");
        loading.setStyle("-fx-text-fill: white;");
        recommendationsBox.getChildren().add(loading);

        AsyncManager.runAsync(fetcher::fetch)
            .thenAcceptAsync(jsonResponse -> {
                try {
                    JsonNode root = mapper.readTree(jsonResponse);
                    JsonNode results = root.path("results");
                    
                    recommendationsBox.getChildren().clear();
                    
                    if (results.isArray() && results.size() > 0) {
                        // Set Hero Banner using the first result
                        JsonNode hero = results.get(0);
                        setupHeroBanner(hero);
                        
                        // Populate carousel with the rest
                        for (int i = 1; i < results.size(); i++) {
                            JsonNode item = results.get(i);
                            String id = item.path("id").asText();
                            String title = item.path("title").isMissingNode() ? item.path("name").asText() : item.path("title").asText();
                            String mediaType = item.path("media_type").asText("tv"); // Default to TV for anime discover
                            String posterPath = item.path("poster_path").asText("");
                            
                            if (!posterPath.isEmpty() && !posterPath.equals("null")) {
                                VBox card = createShowCard(id, title, mediaType, posterPath, "", null, "[]", 0);
                                recommendationsBox.getChildren().add(card);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, Platform::runLater);
    }

    private void setupHeroBanner(JsonNode item) {
        heroTmdbId = item.path("id").asText();
        heroType = item.path("media_type").asText("tv");
        heroPoster = item.path("poster_path").asText("");
        
        String title = item.path("title").isMissingNode() ? item.path("name").asText() : item.path("title").asText();
        String overview = item.path("overview").asText();
        String backdrop = item.path("backdrop_path").asText("");
        
        heroTitle.setText(title);
        heroOverview.setText(overview.length() > 200 ? overview.substring(0, 200) + "..." : overview);
        
        if (!backdrop.isEmpty() && !backdrop.equals("null")) {
            String fullUrl = TMDB_IMAGE_LARGE + backdrop;
            Image bgImg = new Image(fullUrl, true);
            bgImg.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() == 1.0) {
                    BackgroundSize coverSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
                    Background bg = new Background(new BackgroundImage(bgImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, coverSize));
                    heroPane.setBackground(bg);
                    mainBgPane.setBackground(bg);
                }
            });
        }
        
        heroGenres.getChildren().clear();
        Label genreLabel = new Label("Trending");
        genreLabel.getStyleClass().add("genre-pill");
        heroGenres.getChildren().add(genreLabel);
    }

    @FXML
    private void openHeroDetails() {
        if (heroTmdbId != null) {
            openDetails(heroTmdbId, heroType, null, "[]", 0);
        }
    }

    @FXML
    private void addHeroToLibrary() {
        if (heroTmdbId != null) {
            AsyncManager.runAsync(() -> {
                // Ao adicionar pelo banner, salva como WATCHING. O runtime não temos fácil aqui, envia 0.
                return supabaseClient.addShowToLibrary(heroTmdbId, heroTitle.getText(), heroType, heroPoster, "WATCHING", 0);
            }).thenAcceptAsync(success -> {
                if (success) {
                    System.out.println("Adicionado à biblioteca!");
                }
            }, Platform::runLater);
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showHome();
            return;
        }

        homeScroll.setVisible(false);
        libraryScroll.setVisible(false);
        searchScroll.setVisible(true);
        if (settingsScroll != null) settingsScroll.setVisible(false);
        setActiveTab(null);
        resultsPane.getChildren().clear();

        Label loading = new Label("Pesquisando...");
        loading.setStyle("-fx-text-fill: white;");
        resultsPane.getChildren().add(loading);

        AsyncManager.runAsync(() -> {
            return tmdbClient.searchMulti(query);
        }).thenAcceptAsync(jsonResponse -> {
            try {
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode results = root.path("results");
                resultsPane.getChildren().clear();

                if (results.isArray()) {
                    for (JsonNode item : results) {
                        String mediaType = item.path("media_type").asText();
                        String title = item.path("title").isMissingNode() ? item.path("name").asText() : item.path("title").asText();
                        String posterPath = item.path("poster_path").asText("");
                        String overview = item.path("overview").asText("");
                        String id = item.path("id").asText();

                        if (!mediaType.equals("person") && !posterPath.isEmpty() && !posterPath.equals("null")) {
                            VBox card = createShowCard(id, title, mediaType, posterPath, overview, null, "[]", 0);
                            resultsPane.getChildren().add(card);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Platform::runLater);
    }

    private void loadLibrary(boolean isLibrary) {
        libraryPane.getChildren().clear();
        
        Label loading = new Label("Carregando...");
        loading.setStyle("-fx-text-fill: white;");
        libraryPane.getChildren().add(loading);

        AsyncManager.runAsync(() -> {
            if (isLibrary) return supabaseClient.getUserLibrary();
            else return supabaseClient.getUserWishlist();
        }).thenAcceptAsync(jsonResponse -> {
            try {
                JsonNode root = mapper.readTree(jsonResponse);
                libraryPane.getChildren().clear();
                
                int totalRuntimeMinutes = 0;
                int totalEpsWatched = 0;
                int totalMovies = 0;

                if (root.isArray()) {
                    for (JsonNode item : root) {
                        String title = item.path("title").asText();
                        String type = item.path("media_type").asText();
                        String poster = item.path("poster_path").asText("");
                        int runtime = item.path("runtime").asInt(0);
                        String status = item.path("status").asText("WATCHING");
                        
                        JsonNode watchedNode = item.path("watched_episodes");
                        String watchedEpisodes = watchedNode.isMissingNode() ? "[]" : watchedNode.toString();
                        if (watchedNode.isTextual()) {
                            watchedEpisodes = watchedNode.asText();
                        }
                        
                        JsonNode ratingNode = item.path("user_rating");
                        int userRating = ratingNode.isMissingNode() || ratingNode.isNull() ? 0 : ratingNode.asInt();
                        
                        if (isLibrary) {
                            if (type.equals("movie")) {
                                totalMovies++;
                                totalRuntimeMinutes += runtime;
                            } else {
                                int epsCount = 0;
                                if (watchedNode.isArray()) {
                                    epsCount = watchedNode.size();
                                } else if (watchedNode.isTextual()) {
                                    try {
                                        JsonNode parsed = mapper.readTree(watchedNode.asText());
                                        if (parsed.isArray()) epsCount = parsed.size();
                                    } catch (Exception ignored) {}
                                }
                                totalEpsWatched += epsCount;
                                totalRuntimeMinutes += (epsCount * (runtime > 0 ? runtime : 45)); // Fallback para shows antigos com runtime 0
                            }
                        }
                        
                        VBox card = createShowCard(item.path("tmdb_id").asText(), title, type, poster, "", status, watchedEpisodes, userRating);
                        libraryPane.getChildren().add(card);
                    }
                }
                
                if (isLibrary) {
                    statsEps.setText(String.valueOf(totalEpsWatched));
                    statsMovies.setText(String.valueOf(totalMovies));
                    
                    int hours = totalRuntimeMinutes / 60;
                    int remainingMinutes = totalRuntimeMinutes % 60;
                    int days = hours / 24;
                    int remainingHours = hours % 24;
                    int months = days / 30;
                    int remainingDays = days % 30;
                    
                    StringBuilder timeStr = new StringBuilder();
                    if (months > 0) timeStr.append(months).append(" Meses ");
                    if (remainingDays > 0) timeStr.append(remainingDays).append(" Dias ");
                    if (remainingHours > 0) timeStr.append(remainingHours).append(" Horas ");
                    if (remainingMinutes > 0 || timeStr.length() == 0) timeStr.append(remainingMinutes).append(" Minutos");
                    
                    statsTime.setText(timeStr.toString().trim());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Platform::runLater);
    }

    private VBox createShowCard(String tmdbId, String title, String type, String posterPath, String overview, String libraryStatus, String watchedEpisodes, int userRating) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-min-width: 150px;");
        card.getStyleClass().add("card-hover");

        ImageView imageView = new ImageView();
        if (posterPath != null && !posterPath.isEmpty()) {
            imageView.setImage(new Image(TMDB_IMAGE_URL + posterPath, true));
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(225);
        imageView.setPreserveRatio(true);
        
        Rectangle clip = new Rectangle(150, 225);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setMaxWidth(150);

        Label typeLabel = new Label(type.equals("movie") ? "Filme" : "Série");
        typeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        card.getChildren().addAll(imageView, titleLabel, typeLabel);

        card.setOnMouseClicked(e -> {
            openDetails(tmdbId, type, libraryStatus, watchedEpisodes, userRating);
        });

        return card;
    }

    private void openDetails(String tmdbId, String type, String libraryStatus, String watchedEpisodes, int userRating) {
        detailsController.loadDetails(tmdbId, type, libraryStatus, watchedEpisodes, userRating);
        details.setVisible(true);
    }
}
