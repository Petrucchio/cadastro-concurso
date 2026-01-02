package com.empresa.concurso.integration;

import com.empresa.concurso.model.Candidato;
import com.empresa.concurso.repository.CandidatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da API REST.
 * Testa o fluxo completo: Controller → Service → Repository → Database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Candidato API - Testes de Integração")
class CandidatoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @BeforeEach
    void setUp() {
        candidatoRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/candidatos - Deve criar candidato com sucesso")
    void devecriarCandidatoComSucesso() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpeg",
            "conteúdo da imagem".getBytes()
        );

        mockMvc.perform(multipart("/api/candidatos")
                .file(imagem)
                .param("nome", "João Silva")
                .param("email", "joao@email.com")
                .param("telefone", "11987654321")
                .param("concursos", "TRF 2023, Polícia Federal 2024"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nome").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao@email.com"))
            .andExpect(jsonPath("$.telefone").value("11987654321"))
            .andExpect(jsonPath("$.concursosAprovados").isArray())
            .andExpect(jsonPath("$.concursosAprovados", hasSize(2)))
            .andExpect(jsonPath("$.imagemUrl").value(startsWith("/uploads/")));
    }

    @Test
    @DisplayName("POST /api/candidatos - Deve retornar 400 quando dados inválidos")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpeg",
            "conteúdo".getBytes()
        );

        mockMvc.perform(multipart("/api/candidatos")
                .file(imagem)
                .param("nome", "Jo")  // Nome muito curto
                .param("email", "email-invalido")  // Email inválido
                .param("telefone", "123")  // Telefone inválido
                .param("concursos", ""))  // Vazio
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/candidatos - Deve retornar 409 quando email duplicado")
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        // Criar primeiro candidato
        Candidato candidato = new Candidato();
        candidato.setNome("João Silva");
        candidato.setEmail("joao@email.com");
        candidato.setTelefone("11987654321");
        candidato.setConcursosAprovados(java.util.Arrays.asList("TRF 2023"));
        candidato.setImagemFilename("uuid-123.jpg");
        candidatoRepository.save(candidato);

        // Tentar criar com mesmo email
        MockMultipartFile imagem = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpeg",
            "conteúdo".getBytes()
        );

        mockMvc.perform(multipart("/api/candidatos")
                .file(imagem)
                .param("nome", "Maria Silva")
                .param("email", "joao@email.com")  // Email duplicado
                .param("telefone", "11987654322")
                .param("concursos", "TCU 2024"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value(containsString("já cadastrado")));
    }

    @Test
    @DisplayName("GET /api/candidatos - Deve listar todos os candidatos")
    void deveListarTodosOsCandidatos() throws Exception {
        // Criar candidatos de teste
        Candidato c1 = new Candidato();
        c1.setNome("João Silva");
        c1.setEmail("joao@email.com");
        c1.setTelefone("11987654321");
        c1.setConcursosAprovados(java.util.Arrays.asList("TRF 2023"));
        c1.setImagemFilename("uuid-1.jpg");
        candidatoRepository.save(c1);

        Candidato c2 = new Candidato();
        c2.setNome("Maria Santos");
        c2.setEmail("maria@email.com");
        c2.setTelefone("11987654322");
        c2.setConcursosAprovados(java.util.Arrays.asList("TCU 2024"));
        c2.setImagemFilename("uuid-2.jpg");
        candidatoRepository.save(c2);

        mockMvc.perform(get("/api/candidatos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].nome").exists())
            .andExpect(jsonPath("$[1].nome").exists());
    }

    @Test
    @DisplayName("GET /api/candidatos/{id} - Deve buscar candidato por ID")
    void deveBuscarCandidatoPorId() throws Exception {
        Candidato candidato = new Candidato();
        candidato.setNome("João Silva");
        candidato.setEmail("joao@email.com");
        candidato.setTelefone("11987654321");
        candidato.setConcursosAprovados(java.util.Arrays.asList("TRF 2023"));
        candidato.setImagemFilename("uuid-123.jpg");
        candidato = candidatoRepository.save(candidato);

        mockMvc.perform(get("/api/candidatos/" + candidato.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(candidato.getId()))
            .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @DisplayName("GET /api/candidatos/{id} - Deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/api/candidatos/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/candidatos/health - Deve retornar status OK")
    void deveRetornarHealthOk() throws Exception {
        mockMvc.perform(get("/api/candidatos/health"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("funcionando")));
    }
}
