# RecipeFlow — O Meu Diário Gastronómico

O **RecipeFlow** é uma aplicação Android nativa desenvolvida em **Java**, concebida para centralizar, gerir e descobrir receitas culinárias. O projeto serve como um mostruário técnico de boas práticas de arquitetura móvel, integrando persistência relacional local, consumo de APIs RESTful e inteligência artificial diretamente no dispositivo (On-Device AI).

---

## 🚀 Principais Funcionalidades

* **Gestão Completa de Receitas (CRUD):** Registo manual com título, modo de preparação, notas pessoais, classificação por estrelas e controlo de exclusão lógica (Soft Delete).
* **Pesquisa em Tempo Real:** Filtragem dinâmica e responsiva por título, tags ou ingredientes específicos, integrada na interface via `SearchView`.
* **Descoberta Internacional (API REST):** Integração com a API pública **TheMealDB** para procurar e sugerir receitas aleatórias de culinárias de todo o mundo.
* **Tradução Inteligente On-Device (Google ML Kit):** Motor de tradução offline embutido que converte automaticamente receitas internacionais de Inglês para Português de forma assíncrona, processando títulos, instruções e múltiplos ingredientes simultaneamente através de execuções paralelas.
* **Modo Cozinha (Kitchen Mode):** Experiência de utilização otimizada para o momento de preparação, mantendo o ecrã do dispositivo sempre ativo (`FLAG_KEEP_SCREEN_ON`) e ampliando consideravelmente a tipografia para facilitar a leitura à distância.
* **Temporizador de Preparação Integrado:** Contador decrescente gerido em segundo plano para auxiliar o utilizador no controlo exato do tempo de cozedura das receitas.

---

## 🏗️ Arquitetura e Padrões de Projeto

O projeto adota uma arquitetura estruturada de forma a garantir a separação de conceitos (Separation of Concerns), facilidade de manutenção e extensibilidade:

* **Service Pattern:** Camada de serviço (`ReceitaService`) dedicada à centralização das regras de negócio (filtros, sorteios e fluxos de tradução), desacoplando totalmente a lógica de tratamento de dados das `Activities`.
* **Repository Pattern & Room DB:** Abstração completa da base de dados SQLite utilizando a **Room Framework**. Implementa relacionamentos relacionais complexos do tipo **Muitos-para-Muitos (N:N)** entre `Receita` e `Ingrediente` usando tabelas associativas (`CrossRef`) e conversores personalizados (`TypeConverters`).
* **Singleton Pattern:** Garantia de uma instância única e segura contra concorrência (Thread-Safe com sincronismo duplo) para o acesso à base de dados `AppDatabase`.
* **Programação Assíncrona e Paralelismo:** Operações de escrita/leitura na base de dados executadas fora da *Main Thread*. O fluxo de tradução utiliza a **Tasks API** do Google Play Services para gerir chamadas paralelas concorrentes (`Tasks.whenAllSuccess`), unificando os resultados de múltiplos ingredientes sem bloquear a interface de utilizador.

---

## 🛠️ Stack Tecnológica

* **Linguagem:** Java 21 / Android SDK
* **Interface Gráfica:** Material 3 Components, CoordinatorLayout, CollapsingToolbarLayout adaptável para rolagem fluida.
* **Persistência Local:** Room Database Framework
* **Comunicação HTTP:** Retrofit 2 & Gson Converter Factory
* **Processamento de Imagens:** Glide (Carregamento performático e gestão de cache de multimédia externa)
* **Inteligência Artificial / NLP:** Google ML Kit Translation API (Modelos locais em formato offline)

---

## 🔧 Configuração e Instalação

1. Clone o repositório na sua máquina local:
   ```bash
   git clone https://github.com/seu-usuario/recipeflow.git
Abra o projeto no Android Studio (Koala ou superior recomendado).

Certifique-se de que o Gradle sincroniza com sucesso todas as dependências especificadas no build.gradle (app).

Execute a aplicação num dispositivo físico ou emulador (API 31 ou superior).

Nota: Na primeira execução da funcionalidade de tradução, a aplicação fará o download silencioso e automático do modelo de linguagem de ~30MB fornecido pelo Google ML Kit.