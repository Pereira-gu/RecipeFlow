# 🍳 RecipeFlow — Seu Diário Gastronômico

O **RecipeFlow** é um aplicativo Android nativo desenvolvido em **Java**, projetado para ser o companheiro ideal na cozinha. Ele permite que você organize suas receitas favoritas, descubra novos pratos internacionais e facilite o processo de preparo com ferramentas integradas.

---

## 🚀 Funcionalidades Principais

### 📖 Gestão de Receitas
- **Catálogo Pessoal:** Adicione, edite e organize suas próprias receitas com fotos, notas e avaliações.
- **Persistência Room:** Armazenamento local robusto utilizando banco de dados SQLite para acesso offline.
- **Busca Dinâmica:** Encontre receitas instantaneamente por nome, tags ou ingredientes.

### 🌎 Descoberta e Tradução
- **Integração TheMealDB:** Explore receitas aleatórias de diversas culturas através de uma API externa.
- **Tradução Automática:** Tradução inteligente de receitas (Inglês ➔ Português) integrada via **MyMemory API**, garantindo que barreiras linguísticas não impeçam seu próximo prato.

### 👨‍🍳 Auxílio no Preparo
- **Modo Cozinha:** Interface otimizada que mantém a tela ligada e aumenta as fontes para facilitar a leitura enquanto você cozinha.
- **Vídeos Integrados:** Link direto para tutoriais no YouTube quando disponíveis na receita.
- **Temporizador:** Timer rápido para controle preciso do tempo de preparo.

### 🛠️ Utilitários Extras
- **Lista de Compras:** Gere checklists de ingredientes diretamente das receitas.
- **Conversor de Unidades:** Ferramenta para conversões rápidas de medidas culinárias.

---

## 🏗️ Arquitetura e Tecnologias

- **Linguagem:** Java
- **Arquitetura:** Service & Repository Pattern
- **Persistência:** Room Database
- **Networking:** Retrofit 2 + GSON
- **Imagens:** Glide
- **Tradução:** MyMemory API (Translated.net)

---

## ⚙️ Instalação

1. Clone este repositório.
2. Abra no **Android Studio**.
3. Sincronize o **Gradle** e execute em um dispositivo/emulador (API 31+).

---
*Desenvolvido como um projeto prático para demonstrar competências em Android Nativo.*
