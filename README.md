````md
<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen" />
  <img src="https://img.shields.io/badge/Maven-Wrapper-blue" />
  <img src="https://img.shields.io/badge/Database-H2-lightgrey" />
  <img src="https://img.shields.io/badge/License-MIT-green" />
</p>

<h1 align="center">📋 Sistema de Cadastro de Aprovados em Concursos Públicos</h1>

<p align="center">
API REST moderna desenvolvida em <b>Java 17 + Spring Boot</b>, com foco em boas práticas de engenharia de software, arquitetura limpa e mentalidade <b>DevSecOps</b>.
</p>

<p align="center">
Projeto ideal para <b>portfólio backend</b>, estudos avançados ou como base para sistemas reais.
</p>

---

## 🎯 Objetivo do Projeto

Demonstrar, de forma prática e profissional, a construção de uma **API REST robusta**, abordando:

- Arquitetura em camadas
- Validação e sanitização de dados
- Upload seguro de arquivos
- Persistência com JPA / Hibernate
- Tratamento global de exceções
- Boas práticas de logging
- Padronização de build com Maven Wrapper

---

## 🛠️ Stack Tecnológica

| Tecnologia | Uso |
|-----------|-----|
| Java 17 | Linguagem principal |
| Spring Boot 3.2 | Framework backend |
| Maven / Maven Wrapper | Build e dependências |
| H2 Database | Banco em memória (dev) |
| Docker | Containerização |
| HTML + CSS + JS | Frontend simples |

📄 **Licença:** MIT

---

## 🚀 Quick Start

```bash
git clone https://github.com/Petrucchio/cadastro-concurso
cd cadastro-concurso
````

### ▶️ Executar a aplicação

#### Linux / macOS

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

#### Windows

```bash
mvnw.cmd spring-boot:run
```

Acesse:

* 🌐 Aplicação: [http://localhost:8080](http://localhost:8080)
* 🔌 API: [http://localhost:8080/api/candidatos](http://localhost:8080/api/candidatos)
* 🗄️ H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## 🧯 Fallback — Execução sem Maven Wrapper

```bash
mvn spring-boot:run
```

Requisitos:

* Java 17+
* Maven 3.8+
* `JAVA_HOME` configurado

---

## 🔄 Funcionamento Interno

Ao iniciar, a aplicação:

1. Carrega configurações (`application.yml`)
2. Inicializa banco H2 em memória
3. Configura JPA / Hibernate
4. Cria tabelas automaticamente
5. Inicializa serviço de upload
6. Expõe endpoints REST
7. Inicia servidor HTTP (porta 8080)

⚠️ Todos os dados são perdidos ao reiniciar.

---

## 🖼️ Upload de Imagens

* Tipos aceitos: **JPG / PNG**
* Tamanho máximo: **5MB**
* Armazenamento: filesystem local (`/uploads`)
* Pasta criada automaticamente na primeira execução

---

## 🔌 Endpoints Principais

```
POST /api/candidatos
GET  /api/candidatos
GET  /api/candidatos/{id}
GET  /api/candidatos/health
```

### POST `/api/candidatos`

`multipart/form-data`

Campos obrigatórios:

* nome
* email
* telefone
* concursos
* imagem

---

## 🔐 Validações e Segurança

### Validações

* Nome mínimo de 3 caracteres
* Email válido e único
* Telefone sanitizado
* Imagem validada por tipo e tamanho

### Segurança

* Normalização de dados
* Logs sem exposição de informações sensíveis
* Tratamento centralizado de exceções

---

## 🧱 Estrutura do Projeto

```
src/main/java
├── controller
├── service
├── repository
├── model
├── dto
├── exception
└── config
```

Separação clara entre **API**, **domínio** e **infraestrutura**.

---

## 🧪 Testes

```bash
./mvnw test
```

* Testes unitários e de integração
* Cobertura aproximada: **85%**

---

## 🐳 Docker

```bash
docker build -t cadastro-concurso .
docker run -p 8080:8080 cadastro-concurso
```

Executa a aplicação sem necessidade de Java instalado localmente.

---

## 🧠 Decisões Arquiteturais

* **Spring Boot:** produtividade e ecossistema maduro
* **DTOs:** desacoplamento entre API e domínio
* **H2:** agilidade no desenvolvimento
* **Filesystem:** simplicidade para MVP
* **Maven Wrapper:** padronização de ambiente

---

## 🔮 Próximos Passos

* Autenticação JWT
* PostgreSQL
* Swagger / OpenAPI
* CI/CD com GitHub Actions
* Observabilidade (logs e métricas)
* Kubernetes

---

## 👨‍💻 Autor

**Victor José Costa Farias**

* GitHub: [https://github.com/Petrucchio](https://github.com/Petrucchio)
* LinkedIn: [https://www.linkedin.com/in/victorjosecostafarias/](https://www.linkedin.com/in/victorjosecostafarias/)

---

⭐ Se este projeto foi útil, considere deixar uma estrela!

