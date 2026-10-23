<div align="center">

<img src="src/main/resources/icon.png" alt="TV With Popcorn logo" width="150"/>

# TV With Popcorn (TWP)

<!-- badges -->
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-663399?style=flat-square&logo=css&logoColor=white) ![Python](https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=python&logoColor=white) ![JavaFX](https://img.shields.io/badge/JavaFX-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) ![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?style=flat-square&logo=supabase&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)

[Ler em Português](README.md)

A JavaFX desktop app to track movies, TV shows and anime, built as an alternative to TV Time.

[Download the Windows installer](https://github.com/Luisr-nunes/TWP_Tv_With_Popcorn/releases/latest/download/TV_With_Popcorn_Installer.exe)

</div>

## About

TWP lets you search titles, build a personal library and log your progress on each show, with your own account synced to the cloud. The look follows streaming platforms, using the Aurora Night palette (deep purple with magenta highlights).

## Features

- Trending movies, shows and anime, plus title search, from the TMDB API (in Portuguese).
- User sign-up and login with Supabase Auth.
- Personal library with To watch, Watching and Finished statuses, and per-episode progress (for example, S01E05).
- Stats on how much time you have spent watching.
- TV Time history import from its data export (GDPR): select the extracted folder, which contains `tracking-prod-records-v2.csv`.
- Glass-style UI, animated carousel and panel transitions.

The UI is in Portuguese.

## Tech stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| UI | JavaFX 21 with FXML and CSS |
| Data | Supabase (Auth and REST over PostgreSQL, with Row Level Security) |
| Catalog | TMDB API |
| JSON | Jackson |
| Build and installer | Maven (wrapper included), maven-shade-plugin, jpackage and WiX |

## Structure

```
TWP_Tv_With_Popcorn/
├── src/main/java/com/twp/
│   ├── App.java, Launcher.java
│   ├── controller/      # AuthController, MainController, DetailsController
│   ├── service/         # SupabaseClient, TmdbClient, TvTimeImporter
│   └── util/            # AsyncManager, EnvConfig, Session
├── src/main/resources/  # FXML screens, styles.css and icons
├── supabase_schema.sql  # user_shows table and access policies
└── TODO.md              # known bugs
```

## Installing (users)

1. Download the installer from the link above and run `TV_With_Popcorn_Installer.exe`.
2. Follow the wizard; the shortcut appears in the Start menu and on the desktop.

The installer bundles Java, so nothing needs to be installed beforehand. Windows only.

## Running (development)

Requirements: JDK 17+, a TMDB API key and a Supabase project.

```bash
git clone https://github.com/Luisr-nunes/TWP_Tv_With_Popcorn.git
cd TWP_Tv_With_Popcorn
```

1. Create the tables in Supabase by running `supabase_schema.sql` in the SQL Editor.
2. Create `src/main/resources/config.properties` (already ignored by Git):

   ```properties
   TMDB_API_TOKEN=your_tmdb_read_access_token
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_KEY=your_anon_key
   ```

3. Run:

   ```bash
   ./mvnw clean javafx:run      # Linux/macOS
   mvnw.cmd clean javafx:run    # Windows
   ```

## Building the executable

```bash
./mvnw clean package
jpackage --type app-image --name "TV With Popcorn" --input target/ \
  --main-jar twp-desktop-1.0-SNAPSHOT-shaded.jar --main-class com.twp.Launcher --dest build
```

The executable ends up in `build/TV With Popcorn/`. `config.properties` is packaged along with it, so the TMDB token and the Supabase anon key ship inside the installer.

## Known bugs

- Star ratings are not yet saved or displayed correctly (see [TODO.md](TODO.md)).

## Author

Luís Nunes — [GitHub](https://github.com/Luisr-nunes) · [LinkedIn](https://www.linkedin.com/in/luis-nunes-dev/)
