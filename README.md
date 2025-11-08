# 🏛️ FECAP - Fundação de Comércio Álvares Penteado

<p align="center">
  <a href="https://www.fecap.br/">
    <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhZPrRa89Kma0ZZogxm0pi-tCn_TLKeHGVxywp-LXAFGR3B1DPouAJYHgKZGV0XTEf4AE&usqp=CAU" 
         alt="FECAP - Fundação de Comércio Álvares Penteado" width="200"/>
  </a>
</p>

---

# 🍲 Comedoria da Tia

Um aplicativo **mobile em Java (Android)** desenvolvido para otimizar o processo de **pedidos e pagamentos na cantina da FECAP**, permitindo que os alunos acessem o cardápio atualizado, façam seus pedidos antecipadamente e evitem filas.

---

## 👥 Grupo: Yanketes (Desenvolvedores da Comedoria da Tia)

### 👩‍💻 Integrantes:
- <a href="https://github.com/Smug303">Bruno Eduardo</a>
- <a href="https://github.com/eric-bittu">Eric Bittu</a>
- <a href="https://github.com/vivikari">Vivian Umaki</a>
- <a href="https://github.com/Yanko-dev">Yanko Lee</a>

---

## 🧑‍🏫 Professores Orientadores:
- <a href="https://www.linkedin.com/in/katia-bossi/?originalSubdomain=br">Kátia Bossi</a>
- Marco Aurélio Lima Barbosa
- <a href="https://github.com/roddai">Rodrigo da Rosa</a>
- <a href="https://www.linkedin.com/in/victorbarq/">Victor Bruno Alexander Rosetti de Quiroz</a>

---

## 🎯 1. Apresentação do Projeto
A **Comedoria da Tia** é a cantina da **FECAP**, responsável por oferecer refeições, lanches e bebidas aos estudantes.  
Atualmente, o grande volume de alunos durante os intervalos gera **filas extensas** e **reduz o tempo disponível para alimentação**.

Para resolver esse problema, o projeto propõe o desenvolvimento de um **aplicativo mobile**, onde os alunos podem:
- Visualizar o **cardápio atualizado**;
- **Realizar pedidos antecipadamente**;
- **Efetuar pagamentos diretamente** pelo aplicativo;
- E apenas **retirar os produtos prontos** no balcão.

🔗 **Protótipo Figma:** [Clique aqui](https://www.figma.com/design/42LDeA0zJmhq2FkGQArkOI/App-Cantina-da-Tia?node-id=0-1&p=f&t=31TGZIHMWUBwtBGs-0)

---

## 📂 2. Estrutura de Pastas

```bash
📦 Projeto_ComedoriaDaTia
┣ 📂 Documentos
┃ ┣ 📂 Entrega1
┃ ┣ 📂 Entrega2
┃ ┣ ┣📂 Projeto Interdisciplinar Aplicativos Moveis
┃ ┣ ┣ ┣📄 tia-cantina.apk
┃ ┣ 📄 Banner_FECAP_CCOMP3_Yanketes.pdf
┣ 📂 src
┃ ┣ 📂 Entrega1
┃ ┃ ┣ 📂 backend
┃ ┃ ┣ 📂 frontend
┃ ┣ 📂 Entrega2
┃ ┃ ┣ 📂 backend
┃ ┃ ┣ 📂 frontend
┣ 📄 README.md
┣ 📄 local.properties
```

## 🎯 3. Objetivos

### 🎓 Objetivo Geral
Desenvolver um aplicativo mobile que permita aos alunos da **FECAP** realizar pedidos e pagamentos antecipados na cantina **Comedoria da Tia**, aprimorando a experiência de consumo e a gestão interna.

---

### 🎯 Objetivos Específicos
- Reduzir filas durante os intervalos;
- Permitir o gerenciamento dinâmico do cardápio;
- Facilitar a visualização e retirada de pedidos;
- Armazenar dados de usuários e pedidos de forma segura;
- Proporcionar uma interface simples, intuitiva e agradável.

---

## ⚙️ 4. Requisitos Funcionais

### 👨‍🎓 Acesso do Aluno
- Cadastro e login de usuário;
- Visualização do cardápio completo;
- Realização de pedidos e seleção de itens;
- Pagamento via aplicativo;
- Histórico de pedidos realizados.

### 👩‍🍳 Acesso da Cantina
- Login administrativo;
- Cadastro, edição e exclusão de produtos;
- Visualização de pedidos em tempo real;
- Marcação de pedidos como “entregues”.

---

## 🔒 5. Requisitos Não Funcionais
- Compatibilidade com **Android (SDK 24+)**;
- Banco de dados hospedado no **Supabase**;
- Interface intuitiva, responsiva e agradável (**UI/UX**);
- Arquitetura modular em **Java (Android Studio)**;
- Comunicação segura com o banco de dados remoto (**REST API + HTTPS**).

---

## 🧰 6. Tecnologias Utilizadas

| Categoria | Ferramenta |
|------------|-------------|
| **Linguagem** | Java |
| **IDE** | Android Studio |
| **Banco de Dados** | Supabase (PostgreSQL + REST API) |
| **Design** | Figma |
| **Controle de Versão** | Git e GitHub |

---

## 🧩 7. Possíveis Extensões Futuras
- Notificações push para retirada pronta;
- Sistema de pontos e fidelidade;
- Dashboard web administrativo;
- Favoritos e agendamento de pedidos;
- Sugestões, avaliações e comentários de produtos.

---

## 💻 8. Instalação (Android)
1. Baixe o arquivo **tia-cantina.apk**;
2. Transfira para o seu celular;
3. Execute o instalador e permita instalações externas, se necessário;
4. Abra o app e faça login com sua conta de aluno.

---

## 🧪 9. Configuração para Desenvolvimento

### Passos para rodar localmente:

```bash
# Clonar o repositório
git clone https://github.com/2025-2-NCC3/Projeto3.git

# Abrir o projeto no Android Studio
# Configurar o arquivo build.gradle com as credenciais do Supabase
```
## Requisitos:
Android Studio (Koala 🐨 ou superior)

Java 11+

Gradle 8+

Emulador Android ou dispositivo físico

## 📄 10. Licença
Licenciado sob Creative Commons CC BY 4.0
Você pode compartilhar e adaptar, desde que dê os devidos créditos aos autores originais.

🔗 Saiba mais sobre a licença


## 🏫 Instituição
FECAP – Fundação de Comércio Álvares Penteado
Curso: Ciência da Computação – 3º Semestre (Turma Yanketes)

