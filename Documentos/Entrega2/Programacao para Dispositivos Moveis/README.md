# 📱🍴 Comedoria da Tia — Aplicativo Mobile
Projeto acadêmico desenvolvido para as disciplinas de **Programação para Dispositivos Móveis** e **Projeto Interdisciplinar** da **FECAP**.

O sistema consiste em um **aplicativo Android** desenvolvido em **Java (Android Studio)**, conectado ao banco de dados **Supabase**, que disponibiliza o cardápio da Comedoria da Tia para que alunos possam visualizar e realizar pedidos de **salgados, doces e marmitas** de forma prática e rápida.

O objetivo é **diminuir filas e otimizar o atendimento**, facilitando a comunicação entre os alunos e a equipe da tia responsável pela comedoria.

---

## 👩‍💻 Integrantes
- [Bruno Eduardo](https://github.com/Smug303)
- [Eric Bittu](https://github.com/eric-bittu)
- [Vivian Umaki](https://github.com/vivikari)
- [Yanko Lee](https://github.com/Yanko-dev)

---

## 📚 Professores Orientadores
- Marco Aurélio Lima Barbosa
- [Rodrigo da Rosa](https://github.com/roddai)
- [Kátia Bossi](https://www.linkedin.com/in/katia-bossi/?originalSubdomain=br)
- [Victor Bruno Alexander Rosetti de Quiroz](https://www.linkedin.com/in/victorbarq/)

---

## 🏗️ Arquitetura do Projeto

O projeto é composto por um **único módulo mobile** desenvolvido em **Android Studio (Java)** com integração direta ao **Supabase**.

### 📱 Aplicativo Mobile (Android Studio - Java)
- **IDE:** Android Studio (2025.1.3 ou superior)
- **Linguagem:** Java
- **Framework:** Android SDK
- **Banco de Dados:** Supabase (PostgreSQL)

#### 📲 Funções principais:
- Autenticação de usuários
- Consulta e exibição dos produtos disponíveis (cardápio em tempo real)
- Integração com banco Supabase via API REST
- Layout padronizado 
- Otimização de pedidos e redução de filas

---

## 📂 Estrutura de Pastas

```bash
Raiz do Projeto
├── Documentos/
│   ├── Entrega1/
│   │   ├── Análise Descritiva de Dados/
│   │   ├── Programação Orientada a Objetos/
│   │   ├── Programação para Dispositivos Móveis/
│   │   └── Projeto Interdisciplinar Aplicativo/
│   ├── Entrega2/
│   │   ├── Análise Descritiva de Dados/
│   │   ├── Programação para Dispositivos Móveis/
│   │   ├── Programação Orientada a Objetos/
│   │   └── Projeto Interdisciplinar Aplicativo/
│   └── Banner_FECAP_CCOMP3_Yanketes.pdf
│
├── src/
│   ├── Entrega1/
│   │   ├── backend/
│   │   └── frontend/
│   ├── Entrega2/
│   │   ├── backend/
│   │   └── frontend/
│
├── README.md
└── local.properties
📁 backend/ → Código responsável pela integração com o Supabase (API, autenticação e banco de dados).
📁 frontend/ → Layouts XML, Activities e interface do usuário.
📁 documentos/ → Relatórios e materiais de entrega do projeto.

🔧 Tecnologias Utilizadas
Categoria	Tecnologia
IDE	Android Studio
Linguagem	Java
Banco de Dados	Supabase (PostgreSQL + API REST)
Controle de Versão	Git / GitHub

🚀 Como Executar o Projeto
📱 Mobile (Android)
bash
Copiar código
# Clonar o repositório
git clone https://github.com/2025-2-NCC3/Projeto3.git

# Abrir o projeto no Android Studio
File > Open > src/Entrega2/frontend

# Configurar credenciais do Supabase
# Adicionar URL e Anon Key no arquivo de conexão Java

# Rodar no emulador ou dispositivo físico
Run ▶️
O app será instalado e exibirá o cardápio atualizado diretamente do banco de dados Supabase.

📌 Status Atual
✅ Estrutura do projeto organizada
✅ Banco Supabase conectado
✅ Layout responsivo em Material Design 3
🚧 Implementação de novas telas e funcionalidades em andamento

🎯 Objetivo Acadêmico
Projeto desenvolvido para o curso de Ciência da Computação - FECAP
Turma CCOMP3 — Grupo Yanketes

