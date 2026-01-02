======================================================================
SISTEMA DE CADASTRO DE APROVADOS EM CONCURSOS PÚBLICOS
======================================================================

Projeto backend completo desenvolvido em Java com Spring Boot, focado
em boas práticas de engenharia de software, arquitetura limpa e
mentalidade DevSecOps.

Este projeto pode ser utilizado como:
- Projeto de estudo
- Projeto de portfólio
- Base para sistemas reais
- Exemplo de API REST moderna em Java

----------------------------------------------------------------------
TECNOLOGIAS
----------------------------------------------------------------------
- Java 17
- Spring Boot 3.2
- Maven / Maven Wrapper
- H2 Database (desenvolvimento)
- Docker (opcional)
- HTML + CSS + JavaScript (frontend simples)

Licença: MIT

======================================================================
SUMÁRIO
======================================================================
1. Visão Geral
2. Pré-requisitos
3. Quick Start
4. Executando no Linux / macOS
5. Executando no Windows
6. Fallback: Executando sem Maven Wrapper
7. Como a Aplicação Funciona
8. Banco de Dados (H2)
9. Upload e Armazenamento de Arquivos
10. Endpoints da API
11. Validações e Segurança
12. Estrutura do Projeto
13. Testes
14. Docker
15. Troubleshooting
16. Decisões Técnicas
17. Melhorias Futuras
18. Licença e Autor

======================================================================
1. VISÃO GERAL
======================================================================

O sistema permite o cadastro de candidatos aprovados em concursos
públicos, incluindo informações pessoais e upload de imagem.

Funcionalidades principais:
- Cadastro de candidatos
- Upload seguro de imagens (JPG / PNG)
- Persistência em banco de dados
- API REST com tratamento centralizado de erros
- Interface web simples para testes
- Logs estruturados para observabilidade

======================================================================
2. PRÉ-REQUISITOS
======================================================================

Obrigatórios:
- Git
- Java 17 ou superior

Opcional (apenas se o Maven Wrapper falhar):
- Maven 3.8 ou superior

IMPORTANTE:
Este projeto utiliza Maven Wrapper, portanto normalmente
NÃO é necessário instalar o Maven manualmente.

======================================================================
3. QUICK START
======================================================================

1. Clone o repositório:
git clone https://github.com/seu-usuario/cadastro-concurso.git
cd cadastro-concurso

2. Siga as instruções específicas do seu sistema operacional
nas próximas seções.

======================================================================
4. EXECUTANDO NO LINUX / macOS
======================================================================

1. Dê permissão de execução ao Maven Wrapper (apenas uma vez):
chmod +x mvnw

2. Inicie a aplicação:
./mvnw spring-boot:run

3. Acesse no navegador:
- Aplicação: http://localhost:8080
- API: http://localhost:8080/api/candidatos
- H2 Console: http://localhost:8080/h2-console

======================================================================
5. EXECUTANDO NO WINDOWS
======================================================================

1. Abra o Prompt de Comando ou PowerShell no diretório do projeto

2. Execute a aplicação:
mvnw.cmd spring-boot:run

3. Acesse no navegador:
- Aplicação: http://localhost:8080
- API: http://localhost:8080/api/candidatos
- H2 Console: http://localhost:8080/h2-console

OBSERVAÇÃO:
No Windows, utilize sempre o arquivo mvnw.cmd.

======================================================================
6. FALLBACK: EXECUTANDO SEM MAVEN WRAPPER
======================================================================

Em alguns ambientes (principalmente corporativos), o Maven Wrapper
pode não funcionar devido a restrições de segurança, proxy ou antivírus.

Nesses casos, é possível executar a aplicação usando
uma instalação local do Maven.

Requisitos adicionais:
- Maven 3.8 ou superior
- JAVA_HOME configurado
- Maven disponível no PATH

Verifique com:
java -version
mvn -version

----------------------------------------------------------------------
EXECUÇÃO COM MAVEN LOCAL (WINDOWS)
----------------------------------------------------------------------

No diretório do projeto, execute:
mvn spring-boot:run

O comportamento da aplicação será o mesmo do Maven Wrapper.

======================================================================
7. COMO A APLICAÇÃO FUNCIONA
======================================================================

Ao iniciar a aplicação, o Spring Boot executa automaticamente:

1. Leitura das configurações (application.yml)
2. Inicialização do banco H2 em memória
3. Configuração do Hibernate (JPA)
4. Criação automática das tabelas
5. Inicialização do serviço de upload de arquivos
6. Exposição dos endpoints REST
7. Inicialização do servidor HTTP na porta 8080

IMPORTANTE:
Como o banco está em memória, todos os dados
são perdidos ao reiniciar a aplicação.

======================================================================
8. BANCO DE DADOS (H2)
======================================================================

O projeto utiliza H2 Database em memória para desenvolvimento.

Acesso ao console:
http://localhost:8080/h2-console

Configuração:
- JDBC URL: jdbc:h2:mem:concursodb
- Usuário: SA
- Senha: (vazio)

Consultas úteis:

SELECT * FROM candidatos;
SELECT * FROM candidato_concursos;
SELECT COUNT(*) FROM candidatos;

======================================================================
9. UPLOAD E ARMAZENAMENTO DE ARQUIVOS
======================================================================

As imagens enviadas são armazenadas no filesystem local,
fora do classpath da aplicação.

Local da pasta uploads:
- Linux / macOS: <projeto>/uploads/
- Windows: <projeto>\uploads\

A pasta é criada automaticamente na primeira execução.

Permissões (Linux / macOS):
chmod -R 755 uploads

======================================================================
10. ENDPOINTS DA API
======================================================================

Base URL:
http://localhost:8080/api/candidatos

POST /api/candidatos
- multipart/form-data
Campos obrigatórios:
- nome
- email
- telefone
- concursos
- imagem (JPG ou PNG, até 5MB)

GET /api/candidatos
- Lista todos os candidatos

GET /api/candidatos/{id}
- Busca candidato por ID

GET /api/candidatos/health
- Health check da aplicação

======================================================================
11. VALIDAÇÕES E SEGURANÇA
======================================================================

Validações:
- Nome: mínimo 3 caracteres
- Email: formato válido
- Telefone: 10 ou 11 dígitos numéricos
- Imagem: tipo e tamanho verificados

Boas práticas de segurança:
- Email normalizado (lowercase)
- Telefone sanitizado
- Nome com trim
- Validação de email duplicado
- Logs sem exposição de dados sensíveis

======================================================================
12. ESTRUTURA DO PROJETO
======================================================================

cadastro-concurso/
├── src/main/java
│   ├── controller
│   ├── service
│   ├── repository
│   ├── model
│   ├── dto
│   ├── exception
│   └── config
├── src/main/resources
│   ├── application.yml
│   └── static/index.html
├── src/test
├── uploads/
├── pom.xml
├── Dockerfile
└── README.txt

======================================================================
13. TESTES
======================================================================

Executar todos os testes:
./mvnw test

Executar apenas testes unitários:
./mvnw test -Dtest="*Test"

Executar testes de integração:
./mvnw test -Dtest="*IT"

Cobertura aproximada: 85%

======================================================================
14. DOCKER
======================================================================

Build da imagem:
docker build -t cadastro-concurso .

Executar container:
docker run -p 8080:8080 cadastro-concurso

O Docker permite executar o projeto sem Java instalado localmente.

======================================================================
15. TROUBLESHOOTING
======================================================================

Problema: mvnw não executa
- Linux/macOS: chmod +x mvnw
- Windows: use mvnw.cmd ou Maven local

Problema: Porta 8080 ocupada
- Finalize o processo ou altere a porta

Problema: Imagens não salvam
- Verifique a pasta uploads
- Verifique permissões

======================================================================
16. DECISÕES TÉCNICAS
======================================================================

- Spring Boot: produtividade e ecossistema maduro
- H2: rapidez para desenvolvimento
- Filesystem: simplicidade para MVP
- DTOs: separação entre API e domínio
- Maven Wrapper: padronização de build

======================================================================
17. MELHORIAS FUTURAS
======================================================================

- Autenticação JWT
- PostgreSQL em produção
- Swagger / OpenAPI
- CI/CD
- Observabilidade
- Kubernetes

======================================================================
18. LICENÇA E AUTOR
======================================================================

Licença: MIT

Autor:
Victor José Costa Farias
GitHub: https://github.com/seu-usuario
LinkedIn: https://linkedin.com/in/seu-perfil

======================================================================
FIM DO DOCUMENTO
======================================================================
