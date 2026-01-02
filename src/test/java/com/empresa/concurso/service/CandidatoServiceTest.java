package com.empresa.concurso.service;

import com.empresa.concurso.dto.CandidatoDTO;
import com.empresa.concurso.dto.CandidatoResponseDTO;
import com.empresa.concurso.exception.EmailDuplicadoException;
import com.empresa.concurso.exception.InvalidFileException;
import com.empresa.concurso.model.Candidato;
import com.empresa.concurso.repository.CandidatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CandidatoService.
 * 
 * Utiliza Mockito para isolar a lógica de negócio das dependências.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandidatoService - Testes Unitários")
class CandidatoServiceTest {

    @Mock
    private CandidatoRepository candidatoRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private CandidatoService candidatoService;

    private CandidatoDTO candidatoDTO;
    private MultipartFile imagemValida;
    private Candidato candidatoEntity;

    @BeforeEach
    void setUp() {
        // DTO de entrada
        candidatoDTO = new CandidatoDTO();
        candidatoDTO.setNome("João Silva");
        candidatoDTO.setEmail("joao@email.com");
        candidatoDTO.setTelefone("11987654321");
        candidatoDTO.setConcursos("TRF 2023, Polícia Federal 2024");

        // Arquivo de imagem válido
        imagemValida = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpeg",
            "conteúdo da imagem".getBytes()
        );

        // Entity mockada
        candidatoEntity = new Candidato();
        candidatoEntity.setId(1L);
        candidatoEntity.setNome("João Silva");
        candidatoEntity.setEmail("joao@email.com");
        candidatoEntity.setTelefone("11987654321");
        candidatoEntity.setConcursosAprovados(Arrays.asList("TRF 2023", "Polícia Federal 2024"));
        candidatoEntity.setImagemFilename("uuid-123.jpg");
        candidatoEntity.setCriadoEm(LocalDateTime.now());
    }

    // ==================== TESTES DE SUCESSO ====================

    @Test
    @DisplayName("Deve salvar candidato com sucesso")
    void deveSalvarCandidatoComSucesso() {
        // Arrange
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any(Candidato.class))).thenReturn(candidatoEntity);

        // Act
        CandidatoResponseDTO response = candidatoService.salvarCandidato(candidatoDTO, imagemValida);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        assertThat(response.getImagemUrl()).isEqualTo("/uploads/uuid-123.jpg");

        // Verify
        verify(candidatoRepository).existsByEmail("joao@email.com");
        verify(storageService).salvarArquivo(imagemValida);
        verify(candidatoRepository).save(any(Candidato.class));
    }

    @Test
    @DisplayName("Deve converter email para lowercase ao salvar")
    void deveConverterEmailParaLowercase() {
        // Arrange
        candidatoDTO.setEmail("JOAO@EMAIL.COM");
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any(Candidato.class))).thenAnswer(invocation -> {
            Candidato c = invocation.getArgument(0);
            assertThat(c.getEmail()).isEqualTo("joao@email.com");
            return candidatoEntity;
        });

        // Act
        candidatoService.salvarCandidato(candidatoDTO, imagemValida);

        // Assert verificado no when().thenAnswer()
    }

    @Test
    @DisplayName("Deve remover espaços extras do nome")
    void deveRemoverEspacosExtrasDoNome() {
        // Arrange
        candidatoDTO.setNome("  João   Silva  ");
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any(Candidato.class))).thenAnswer(invocation -> {
            Candidato c = invocation.getArgument(0);
            assertThat(c.getNome()).isEqualTo("João   Silva");
            return candidatoEntity;
        });

        // Act
        candidatoService.salvarCandidato(candidatoDTO, imagemValida);

        // Assert verificado no when().thenAnswer()
    }

    @Test
    @DisplayName("Deve sanitizar telefone removendo caracteres não-numéricos")
    void deveSanitizarTelefone() {
        // Arrange
        candidatoDTO.setTelefone("(11) 98765-4321");
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any(Candidato.class))).thenAnswer(invocation -> {
            Candidato c = invocation.getArgument(0);
            assertThat(c.getTelefone()).isEqualTo("11987654321");
            return candidatoEntity;
        });

        // Act
        candidatoService.salvarCandidato(candidatoDTO, imagemValida);

        // Assert verificado no when().thenAnswer()
    }

    @Test
    @DisplayName("Deve listar todos os candidatos")
    void deveListarTodosOsCandidatos() {
        // Arrange
        List<Candidato> candidatos = Arrays.asList(candidatoEntity);
        when(candidatoRepository.findAll()).thenReturn(candidatos);

        // Act
        List<CandidatoResponseDTO> response = candidatoService.listarTodos();

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(1L);
        verify(candidatoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar candidato por ID com sucesso")
    void deveBuscarCandidatoPorId() {
        // Arrange
        when(candidatoRepository.findById(1L)).thenReturn(Optional.of(candidatoEntity));

        // Act
        Optional<CandidatoResponseDTO> response = candidatoService.buscarPorId(1L);

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
        verify(candidatoRepository).findById(1L);
    }

    // ==================== TESTES DE VALIDAÇÃO ====================

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // Arrange
        when(candidatoRepository.existsByEmail("joao@email.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemValida)
        )
        .isInstanceOf(EmailDuplicadoException.class)
        .hasMessageContaining("Email já cadastrado");

        // Verify
        verify(candidatoRepository).existsByEmail("joao@email.com");
        verify(storageService, never()).salvarArquivo(any());
        verify(candidatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando imagem é nula")
    void deveLancarExcecaoQuandoImagemNula() {
        // Arrange
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> 
            candidatoService.salvarCandidato(candidatoDTO, null)
        )
        .isInstanceOf(InvalidFileException.class)
        .hasMessageContaining("Imagem é obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando imagem está vazia")
    void deveLancarExcecaoQuandoImagemVazia() {
        // Arrange
        MultipartFile imagemVazia = new MockMultipartFile(
            "imagem", 
            "foto.jpg", 
            "image/jpeg", 
            new byte[0]
        );
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemVazia)
        )
        .isInstanceOf(InvalidFileException.class)
        .hasMessageContaining("Imagem é obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de arquivo é inválido")
    void deveLancarExcecaoQuandoTipoInvalido() {
        // Arrange
        MultipartFile imagemInvalida = new MockMultipartFile(
            "imagem",
            "documento.pdf",
            "application/pdf",
            "conteúdo do pdf".getBytes()
        );
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemInvalida)
        )
        .isInstanceOf(InvalidFileException.class)
        .hasMessageContaining("Apenas imagens JPG/PNG são permitidas");
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo excede tamanho máximo")
    void deveLancarExcecaoQuandoArquivoMuitoGrande() {
        // Arrange
        byte[] conteudoGrande = new byte[6_000_000]; // 6MB
        MultipartFile imagemGrande = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpeg",
            conteudoGrande
        );
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemGrande)
        )
        .isInstanceOf(InvalidFileException.class)
        .hasMessageContaining("Imagem deve ter no máximo 5MB");
    }

    @Test
    @DisplayName("Deve aceitar imagem JPG válida")
    void deveAceitarImagemJpgValida() {
        // Arrange
        MultipartFile imagemJpg = new MockMultipartFile(
            "imagem",
            "foto.jpg",
            "image/jpg",
            "conteúdo".getBytes()
        );
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any())).thenReturn(candidatoEntity);

        // Act & Assert (não deve lançar exceção)
        assertThatCode(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemJpg)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve aceitar imagem PNG válida")
    void deveAceitarImagemPngValida() {
        // Arrange
        MultipartFile imagemPng = new MockMultipartFile(
            "imagem",
            "foto.png",
            "image/png",
            "conteúdo".getBytes()
        );
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.png");
        when(candidatoRepository.save(any())).thenReturn(candidatoEntity);

        // Act & Assert (não deve lançar exceção)
        assertThatCode(() -> 
            candidatoService.salvarCandidato(candidatoDTO, imagemPng)
        ).doesNotThrowAnyException();
    }

    // ==================== TESTES DE EDGE CASES ====================

    @Test
    @DisplayName("Deve retornar lista vazia quando não há candidatos")
    void deveRetornarListaVaziaQuandoNaoHaCandidatos() {
        // Arrange
        when(candidatoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<CandidatoResponseDTO> response = candidatoService.listarTodos();

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando candidato não existe")
    void deveRetornarOptionalVazioQuandoCandidatoNaoExiste() {
        // Arrange
        when(candidatoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<CandidatoResponseDTO> response = candidatoService.buscarPorId(999L);

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Deve processar múltiplos concursos separados por vírgula")
    void deveProcessarMultiplosConcursos() {
        // Arrange
        candidatoDTO.setConcursos("TRF 2023, Polícia Federal 2024, TCU 2024");
        when(candidatoRepository.existsByEmail(anyString())).thenReturn(false);
        when(storageService.salvarArquivo(any())).thenReturn("uuid-123.jpg");
        when(candidatoRepository.save(any(Candidato.class))).thenAnswer(invocation -> {
            Candidato c = invocation.getArgument(0);
            assertThat(c.getConcursosAprovados()).hasSize(3);
            assertThat(c.getConcursosAprovados()).contains("TRF 2023", "Polícia Federal 2024", "TCU 2024");
            return candidatoEntity;
        });

        // Act
        candidatoService.salvarCandidato(candidatoDTO, imagemValida);

        // Assert verificado no when().thenAnswer()
    }
}
