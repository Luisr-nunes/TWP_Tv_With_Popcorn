<div align="center">

<img src="src/main/resources/icon.png" alt="Logo TV With Popcorn" width="150"/>

# TV With Popcorn (TWP)

<!-- badges -->
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-663399?style=flat-square&logo=css&logoColor=white) ![Python](https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=python&logoColor=white) ![JavaFX](https://img.shields.io/badge/JavaFX-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) ![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?style=flat-square&logo=supabase&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)

[Read in English](README.en.md)

Aplicativo desktop em JavaFX para acompanhar filmes, séries e animes, criado como alternativa ao TV Time.

[Baixar o instalador para Windows](https://github.com/Luisr-nunes/TWP_Tv_With_Popcorn/releases/latest/download/TV_With_Popcorn_Installer.exe)

</div>

## Sobre

O TWP permite pesquisar títulos, montar uma biblioteca pessoal e registrar o progresso de cada série, com conta própria sincronizada na nuvem. O visual segue plataformas de streaming, com a paleta Aurora Night (roxo escuro e destaques em magenta).

## Funcionalidades

- Catálogo de filmes, séries e animes em alta e busca por título, via API do TMDB (em português).
- Cadastro e login de usuários com Supabase Auth.
- Biblioteca pessoal com status Para assistir, Assistindo e Finalizado, e progresso por episódio (por exemplo, S01E05).
- Estatísticas de quanto tempo você já passou assistindo.
- Importação do histórico do TV Time a partir da exportação de dados (GDPR): basta selecionar a pasta extraída, que contém `tracking-prod-records-v2.csv`.
- Interface com efeito de vidro, carrossel animado e transições entre painéis.

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 17 |
| Interface | JavaFX 21 com FXML e CSS |
| Dados | Supabase (Auth e REST sobre PostgreSQL, com Row Level Security) |
| Catálogo | API do TMDB |
| JSON | Jackson |
| Build e instalador | Maven (wrapper incluído), maven-shade-plugin, jpackage e WiX |

## Estrutura

```
TWP_Tv_With_Popcorn/
├── src/main/java/com/twp/
│   ├── App.java, Launcher.java
│   ├── controller/      # AuthController, MainController, DetailsController
│   ├── service/         # SupabaseClient, TmdbClient, TvTimeImporter
│   └── util/            # AsyncManager, EnvConfig, Session
├── src/main/resources/  # telas FXML, styles.css e ícones
├── supabase_schema.sql  # tabela user_shows e políticas de acesso
└── TODO.md              # bugs conhecidos
```

## Instalação (usuário)

1. Baixe o instalador pelo link acima e execute `TV_With_Popcorn_Installer.exe`.
2. Siga o assistente; o atalho aparece no Menu Iniciar e na Área de Trabalho.

O instalador já inclui o Java, então não é preciso instalar nada antes. Disponível apenas para Windows.

## Como executar (desenvolvimento)

Pré-requisitos: JDK 17+, uma chave da API do TMDB e um projeto no Supabase.

```bash
git clone https://github.com/Luisr-nunes/TWP_Tv_With_Popcorn.git
cd TWP_Tv_With_Popcorn
```

1. Crie as tabelas no Supabase executando `supabase_schema.sql` no SQL Editor.
2. Crie o arquivo `src/main/resources/config.properties` (já ignorado pelo Git):

   ```properties
   TMDB_API_TOKEN=seu_token_de_leitura_do_tmdb
   SUPABASE_URL=https://seu-projeto.supabase.co
   SUPABASE_KEY=sua_chave_anon
   ```

3. Rode:

   ```bash
   ./mvnw clean javafx:run      # Linux/macOS
   mvnw.cmd clean javafx:run    # Windows
   ```

## Gerar o executável

```bash
./mvnw clean package
jpackage --type app-image --name "TV With Popcorn" --input target/ \
  --main-jar twp-desktop-1.0-SNAPSHOT-shaded.jar --main-class com.twp.Launcher --dest build
```

O executável fica em `build/TV With Popcorn/`. O `config.properties` é empacotado junto, então o token do TMDB e a chave anon do Supabase vão dentro do instalador.

## Bugs conhecidos

- O sistema de avaliação por estrelas ainda não registra nem exibe as notas corretamente (ver [TODO.md](TODO.md)).

## Autor

Luís Nunes — [GitHub](https://github.com/Luisr-nunes) · [LinkedIn](https://www.linkedin.com/in/luis-nunes-dev/)
