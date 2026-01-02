# 🎯 Decisões Arquiteturais

Este documento detalha as principais decisões técnicas tomadas no projeto, seus trade-offs e justificativas.

---

## 📐 Arquitetura Geral

### Decisão: Arquitetura em Camadas (Layered Architecture)

**Escolha:** Controller → Service → Repository

**Justificativa:**
- ✅ Separação clara de responsabilidades
- ✅ Facilita testes unitários (mockando dependências)
- ✅ Padrão bem conhecido pela comunidade Java
- ✅ Escalável para MVPs e aplicações de médio porte

**Alternativas Consideradas:**
- ❌ **Arquitetura Hexagonal**: Over-engineering para o escopo atual
- ❌ **Microserviços**: Desnecessário para um único domínio

**Trade-offs:**
- ⚠️ Pode se tornar muito acoplada em aplicações muito complexas
- ⚠️ Necessita refatoração se crescer muito (migrar para DDD/Hexagonal)

---

## 🗄️ Persistência

### Decisão: H2 Database (Desenvolvimento)

**Escolha:** Banco em memória H2

**Justificativa:**
- ✅ Zero configuração (embedded)
- ✅ Perfeito para testes e desenvolvimento
- ✅ Console web integrado (`/h2-console`)
- ✅ Reduz barreiras de entrada para avaliadores

**Para Produção:**
```yaml
# Migração recomendada
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/concurso
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # NUNCA use create-drop em produção!
```

**Por que PostgreSQL em produção?**
- ✅ Open source e robusto
- ✅ Suporte a JSON (útil para campos flexíveis)
- ✅ Excelente performance
- ✅ Amplamente suportado em clouds (AWS RDS, Google Cloud SQL, etc.)

---

## 📤 Upload de Arquivos

### Decisão: Filesystem Local (MVP)

**Escolha:** Salvar arquivos no diretório `uploads/` com nomes UUID

**Justificativa:**
- ✅ Simplicidade para MVP
- ✅ Sem dependências externas (AWS SDK, MinIO, etc.)
- ✅ Fácil de testar localmente

**Implementação:**
```java
String nomeUnico = UUID.randomUUID().toString() + extensao;
Path destinoPath = uploadPath.resolve(nomeUnico);
Files.copy(file.getInputStream(), destinoPath);
```

**Para Produção:**
Migrar para **S3/MinIO + CloudFront (CDN)**

```java
// Exemplo com AWS S3
@Service
public class S3StorageService implements StorageService {
    
    @Autowired
    private AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    public String salvarArquivo(MultipartFile file) {
        String key = UUID.randomUUID().toString() + getExtension(file);
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        
        s3Client.putObject(
            bucketName,
            key,
            file.getInputStream(),
            metadata
        );
        
        return s3Client.getUrl(bucketName, key).toString();
    }
}
```

**Trade-offs:**
- ⚠️ Filesystem não escala horizontalmente (múltiplas instâncias)
- ⚠️ Backup manual necessário
- ⚠️ Sem CDN (latência maior para usuários distantes)

---

## 🔒 Segurança

### 1. Validação de Upload

**Decisão:** Validação multi-camada

```java
// 1. Validação de tipo MIME
if (!TIPOS_PERMITIDOS.contains(file.getContentType())) {
    throw new InvalidFileException();
}

// 2. Validação de tamanho
if (file.getSize() > TAMANHO_MAXIMO) {
    throw new InvalidFileException();
}

// 3. Nome único (UUID) - previne path traversal
String nomeUnico = UUID.randomUUID().toString() + extensao;
```

**Por que MIME + extensão?**
- ⚠️ MIME type pode ser falsificado pelo cliente
- ✅ Validação dupla aumenta segurança
- ✅ Em produção, adicionar magic bytes check (Apache Tika)

**Melhorias para Produção:**
```java
// Validação robusta com Apache Tika
Tika tika = new Tika();
String detectedType = tika.detect(file.getInputStream());

if (!detectedType.startsWith("image/")) {
    throw new InvalidFileException("Arquivo não é uma imagem");
}
```

### 2. Sanitização de Inputs

**Decisão:** Sanitizar todos os inputs do usuário

```java
candidato.setNome(dto.getNome().trim());
candidato.setEmail(dto.getEmail().toLowerCase().trim());
candidato.setTelefone(dto.getTelefone().replaceAll("\\D", ""));
```

**Justificativa:**
- ✅ Previne inconsistências (espaços extras)
- ✅ Normaliza dados (email lowercase)
- ✅ Remove caracteres inválidos (telefone)

### 3. Email Duplicado

**Decisão:** Constraint único no banco + validação no service

```java
// Entity
@Column(unique = true)
private String email;

// Service
if (candidatoRepository.existsByEmail(email)) {
    throw new EmailDuplicadoException();
}
```

**Por que validar duas vezes?**
- ✅ Constraint no banco: última linha de defesa
- ✅ Validação no service: melhor mensagem de erro
- ✅ Race condition protection

### 4. Logs Seguros

**Decisão:** NUNCA logar dados sensíveis

```java
// ❌ ERRADO
log.info("Senha recebida: {}", senha);

// ✅ CORRETO
log.info("Cadastro iniciado para: {}", email);
log.debug("Validação de imagem: tipo={}, tamanho={}KB", type, size);
```

**Dados sensíveis:**
- ❌ Senhas (óbvio)
- ❌ Tokens de autenticação
- ❌ Dados pessoais completos (LGPD/GDPR)
- ✅ IDs, tipos de arquivo, tamanhos

---

## 📦 DTOs vs Entities

### Decisão: Separação completa

**Estrutura:**
```
CandidatoDTO         → Input (validado com Bean Validation)
CandidatoResponseDTO → Output (o que o cliente recebe)
Candidato            → Entity (domínio interno)
```

**Justificativa:**
- ✅ Controle total sobre API externa
- ✅ Entity pode evoluir sem quebrar contratos
- ✅ Validações diferentes (input vs output)
- ✅ Segurança (não expor campos internos)

**Exemplo de Evolução:**
```java
// Entity ganha novo campo interno
@Entity
public class Candidato {
    // ...
    @Column
    private String hashSenha; // Não queremos expor isso!
}

// DTO de resposta continua igual
public class CandidatoResponseDTO {
    // hashSenha não aparece aqui
}
```

**Alternativa Descartada:**
- ❌ Usar Entity diretamente no controller
  - Expõe campos internos
  - Dificulta evolução
  - Quebra encapsulamento

---

## 🛡️ Tratamento de Erros

### Decisão: GlobalExceptionHandler centralizado

**Escolha:** `@RestControllerAdvice` com handlers específicos

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleEmailDuplicado(...) {
        return ResponseEntity.status(409).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(...) {
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

**Vantagens:**
- ✅ Respostas de erro consistentes
- ✅ Lógica de tratamento centralizada
- ✅ Facilita manutenção
- ✅ Logs automáticos

**Estrutura de Resposta Padronizada:**
```json
{
  "status": 400,
  "message": "Erro de validação",
  "errors": {
    "email": "Email inválido",
    "telefone": "Telefone deve ter 10-11 dígitos"
  },
  "timestamp": "01/12/2024 14:30:00"
}
```

---

## 🧪 Testabilidade

### Decisão: Injeção de Dependências (Constructor Injection)

**Escolha:** `@RequiredArgsConstructor` (Lombok)

```java
@Service
@RequiredArgsConstructor
public class CandidatoService {
    private final CandidatoRepository repository;  // final = imutável
    private final StorageService storageService;
}
```

**Vantagens:**
- ✅ Imutabilidade (campos `final`)
- ✅ Facilita testes (passar mocks via construtor)
- ✅ Obriga a definir dependências (fail-fast)

**Exemplo de Teste:**
```java
@Test
void deveSalvarCandidato() {
    // Mock das dependências
    var mockRepo = mock(CandidatoRepository.class);
    var mockStorage = mock(StorageService.class);
    
    // Service testável
    var service = new CandidatoService(mockRepo, mockStorage);
    
    // Teste isolado
    service.salvarCandidato(dto, file);
    
    verify(mockRepo).save(any());
}
```

**Alternativa Descartada:**
- ❌ `@Autowired` em campos
  - Dificulta testes
  - Permite dependências nulas
  - Quebra imutabilidade

---

## 🌐 CORS (Cross-Origin)

### Decisão: Liberado para desenvolvimento

```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

**Justificativa:**
- ✅ Facilita testes locais
- ✅ Frontend pode estar em porta diferente

**Para Produção:**
```java
@CrossOrigin(
    origins = {
        "https://meusite.com.br",
        "https://www.meusite.com.br"
    },
    methods = {RequestMethod.GET, RequestMethod.POST},
    maxAge = 3600
)
```

---

## 📊 Observabilidade

### Decisão: Logs estruturados com SLF4J

**Níveis de Log:**
```java
log.error("Erro crítico", exception);    // Erro que requer ação
log.warn("Email duplicado: {}", email);  // Comportamento suspeito
log.info("Candidato {} cadastrado", id); // Evento de negócio
log.debug("Validação: tipo={}", type);   // Debugging
```

**Para Produção:**
- Adicionar **correlation ID** (tracing de requisições)
- Integrar com **ELK Stack** (Elasticsearch, Logstash, Kibana)
- Configurar **alertas** (email duplicado > 10/min)

---

## 🚀 Performance

### Decisão: FetchType.EAGER para concursos

```java
@ElementCollection(fetch = FetchType.EAGER)
private List<String> concursosAprovados;
```

**Justificativa:**
- ✅ Sempre precisamos dos concursos ao buscar candidato
- ✅ Lista pequena (média: 2-3 concursos)
- ⚠️ Se lista crescer muito, migrar para LAZY

**Otimização Futura:**
```java
// Se lista ficar grande (10+ concursos)
@ElementCollection(fetch = FetchType.LAZY)
private List<String> concursosAprovados;

// Usar DTO projection
@Query("SELECT new CandidatoDTO(c.id, c.nome, c.email) FROM Candidato c")
List<CandidatoDTO> findAllLight();
```

---

## 🔑 Configurações

### Decisão: Externalizadas em application.yml

**Estrutura:**
```yaml
# Valores sensíveis via variáveis de ambiente
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:testdb}  # Fallback para H2
    username: ${DB_USER:sa}
    password: ${DB_PASS:}

# Configurações de negócio
app:
  upload:
    dir: ${UPLOAD_DIR:uploads}
    max-size: 5MB
```

**Vantagens:**
- ✅ Não comitar credenciais no Git
- ✅ Fácil mudança entre ambientes (dev/prod)
- ✅ Valores padrão sensatos

---

## 🎨 Frontend

### Decisão: HTML/CSS/JS Vanilla (sem frameworks)

**Justificativa:**
- ✅ Zero dependências
- ✅ Carregamento rápido
- ✅ Foco no backend (teste backend-centric)
- ✅ Funciona em qualquer browser moderno

**Para Produção:**
Considerar migrar para **React/Vue** se crescer

---

## 📋 Checklist de Produção

Antes de ir para produção, implementar:

- [ ] **Autenticação**: JWT/OAuth2
- [ ] **Rate Limiting**: Prevenção de abuso
- [ ] **HTTPS**: TLS/SSL obrigatório
- [ ] **Backup**: Banco + Storage
- [ ] **Monitoring**: Prometheus + Grafana
- [ ] **Secrets Management**: Vault/AWS Secrets
- [ ] **CI/CD**: Pipeline automatizado
- [ ] **Testes**: >90% cobertura

---

## 🤔 Decisões Pendentes

### Autenticação

**Opções:**
1. **JWT Stateless**: Token no header
2. **Session-based**: Cookie + Redis
3. **OAuth2**: Login social (Google, GitHub)

**Recomendação:** JWT + Refresh Token

### Cache

**Quando implementar?**
- Lista de candidatos acessada >100x/min
- Consultas repetitivas

**Solução:** Redis + `@Cacheable`

---

## 📚 Referências

- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [RESTful API Design](https://restfulapi.net/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [12 Factor App](https://12factor.net/)

---

**Última atualização:** 01/12/2024  
**Autor:** DevSecOps Team