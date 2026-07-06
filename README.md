<div align="center">
  <img src="src/main/resources/icon.png" alt="TV With Popcorn Logo" width="150"/>

  # TV With Popcorn (TWP) 🍿
  
  **O seu gerenciador definitivo de Filmes e Séries.**
  
  [![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
  [![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
  [![Maven](https://img.shields.io/badge/Maven-Build-red.svg)](https://maven.apache.org/)
  [![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
</div>

<br/>

**TV With Popcorn (TWP)** é uma aplicação Desktop moderna, elegante e de alto desempenho desenvolvida em JavaFX. Criada para substituir a necessidade de depender de plataformas de terceiros (como o TV Time), o TWP permite que você pesquise, gerencie e salve seu progresso nas suas séries, animes e filmes favoritos.

O visual foi inspirado em plataformas de streaming premium (estilo Amazon Prime / HBO Max), utilizando a paleta *Aurora Night* (foco em imersão com tons de Roxo Espacial e botões brilhantes em Magenta).

---

## ✨ Funcionalidades Principais

* 🎬 **Exploração de Catálogo**: Navegue pelos Filmes, Séries e Animes que estão "Em Alta", com integração direta à API do TMDB.
* 🍿 **Sistema de Busca**: Encontre qualquer produção rapidamente através da barra de pesquisa interativa.
* 📚 **Biblioteca Pessoal**: Gerencie seus títulos salvos (Para Assistir, Assistindo, Finalizado) e acompanhe seu progresso de episódios (ex: S01E05).
* ⏱️ **Estatísticas de Tempo**: O app calcula automaticamente quantas horas/dias da sua vida você já gastou assistindo séries e filmes!
* 🔄 **Migração de Dados (TV Time)**: Possui uma ferramenta de importação robusta para você extrair seus dados do `gdpr-data.zip` (backup do TV Time) e migrar todo o seu histórico com um único clique.
* 🎨 **Design Imersivo**: Glassmorphism, carrossel com deslizamento suave (Timelines do JavaFX) e transições visuais dinâmicas nos painéis principais.

---

## 🚀 Como Baixar e Usar (Versão Pronta)

Se você não é desenvolvedor e apenas quer usar o aplicativo, o jeito mais fácil é fazer o download direto:

<div align="center">
  <a href="https://github.com/Luisr-nunes/twp-desktop/releases/latest/download/TV_With_Popcorn_Release.zip">
    <img src="https://img.shields.io/badge/Download_Grátis_para_Windows-100000?style=for-the-badge&logo=windows&logoColor=white&labelColor=0D0B14&color=F9004D" alt="Download TWP" />
  </a>
</div>
<br/>

1. Clique no botão de download acima.
2. Extraia o arquivo `TV_With_Popcorn_Release.zip` para uma pasta no seu computador.
3. Execute o arquivo **`TV With Popcorn.exe`**. 
   > *Nota: Você não precisa ter o Java instalado, pois esta versão já inclui o motor necessário embutido!*

---

## 🛠️ Como Desenvolver e Compilar (Para Devs)

### Pré-requisitos
- **Java 17** (JDK) ou superior.
- **Maven** (O projeto já inclui o `mvnw` - Maven Wrapper).
- Uma chave de API gratuita do **TMDB**.

### Passos para rodar localmente

1. Clone o repositório:
```bash
git clone https://github.com/SeuUsuario/twp-desktop.git
cd twp-desktop
```

2. Crie o arquivo de variáveis de ambiente:
Crie um arquivo `.env` na raiz do projeto e adicione sua chave do TMDB:
```env
TMDB_API_KEY=sua_chave_aqui
```

3. Execute o projeto usando o Maven Wrapper:
```bash
.\mvnw clean javafx:run
```

### Como gerar o seu próprio `.exe` (Empacotamento)
Utilize o `jpackage` para gerar uma versão Standalone da aplicação:
```bash
# 1. Crie o arquivo fat jar usando o maven-shade-plugin
.\mvnw clean package

# 2. Gere o build com o jpackage
jpackage --type app-image --name "TV With Popcorn" --input target/ --main-jar twp-desktop-1.0-SNAPSHOT-shaded.jar --main-class com.twp.Launcher --dest build
```
O executável estará na pasta `build/TV With Popcorn/`.

---

## 🤝 Contribuindo

Sinta-se livre para abrir **Issues** relatando bugs ou sugerindo novas funcionalidades, ou envie um **Pull Request** se quiser melhorar o código!

<div align="center">
  <br/>
  Feito com 🍿 e ☕.
</div>
