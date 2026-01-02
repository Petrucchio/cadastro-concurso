package com.empresa.concurso.service;

import com.empresa.concurso.dto.CandidatoDTO;
import com.empresa.concurso.dto.CandidatoResponseDTO;
import com.empresa.concurso.exception.EmailDuplicadoException;
import com.empresa.concurso.exception.InvalidFileException;
import com.empresa.concurso.model.Candidato;
import com.empresa.concurso.repository.CandidatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service para lógica de negócio relacionada a candidatos.
 * Responsável por validações, persistência e processamento de uploads.
 * 
 * @author DevSecOps Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatoService {

    private final CandidatoRepository candidatoRepository;
    private final StorageService storageService;

    private static final List<String> TIPOS_PERMITIDOS = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png"
    );
    private static final long TAMANHO_MAXIMO = 5_000_000; // 5MB

    /**
     * Salva um novo candidato com validações de segurança.
     */
    @Transactional
    public CandidatoResponseDTO salvarCandidato(CandidatoDTO dto, MultipartFile imagem) {
        log.info("Iniciando processo de cadastro para email: {}", dto.getEmail());

        // Validação de email duplicado
        validarEmailUnico(dto.getEmail());

        // Validação do arquivo
        validarImagem(imagem);

        // Processar upload
        String nomeArquivoSalvo = storageService.salvarArquivo(imagem);
        log.debug("Imagem salva com nome: {}", nomeArquivoSalvo);

        // Criar entidade
        Candidato candidato = construirCandidato(dto, nomeArquivoSalvo);

        // Persistir
        Candidato candidatoSalvo = candidatoRepository.save(candidato);
        log.info("Candidato persistido com ID: {}", candidatoSalvo.getId());

        return mapearParaResponse(candidatoSalvo);
    }

    /**
     * Lista todos os candidatos cadastrados.
     */
    @Transactional(readOnly = true)
    public List<CandidatoResponseDTO> listarTodos() {
        return candidatoRepository.findAll().stream()
                .map(this::mapearParaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca candidato por ID.
     */
    @Transactional(readOnly = true)
    public Optional<CandidatoResponseDTO> buscarPorId(Long id) {
        return candidatoRepository.findById(id)
                .map(this::mapearParaResponse);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarEmailUnico(String email) {
        if (candidatoRepository.existsByEmail(email)) {
            log.warn("Tentativa de cadastro com email duplicado: {}", email);
            throw new EmailDuplicadoException("Email já cadastrado no sistema");
        }
    }

    private void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new InvalidFileException("Imagem é obrigatória");
        }

        // Validar tipo
        String contentType = imagem.getContentType();
        if (!TIPOS_PERMITIDOS.contains(contentType)) {
            log.warn("Tipo de arquivo inválido: {}", contentType);
            throw new InvalidFileException(
                "Apenas imagens JPG/PNG são permitidas. Tipo recebido: " + contentType
            );
        }

        // Validar tamanho
        if (imagem.getSize() > TAMANHO_MAXIMO) {
            log.warn("Arquivo muito grande: {} bytes", imagem.getSize());
            throw new InvalidFileException(
                String.format("Imagem deve ter no máximo 5MB. Tamanho recebido: %.2f MB", 
                    imagem.getSize() / 1_000_000.0)
            );
        }

        log.debug("Imagem validada: tipo={}, tamanho={}KB", 
            contentType, imagem.getSize() / 1024);
    }

    private Candidato construirCandidato(CandidatoDTO dto, String nomeArquivo) {
        Candidato candidato = new Candidato();
        candidato.setNome(dto.getNome().trim());
        candidato.setEmail(dto.getEmail().toLowerCase().trim());
        candidato.setTelefone(dto.getTelefone().replaceAll("\\D", "")); // Remove não-dígitos
        candidato.setConcursosAprovados(
            Arrays.stream(dto.getConcursos().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
        );
        candidato.setImagemFilename(nomeArquivo);
        candidato.setCriadoEm(LocalDateTime.now());
        return candidato;
    }

    private CandidatoResponseDTO mapearParaResponse(Candidato candidato) {
        CandidatoResponseDTO response = new CandidatoResponseDTO();
        response.setId(candidato.getId());
        response.setNome(candidato.getNome());
        response.setEmail(candidato.getEmail());
        response.setTelefone(candidato.getTelefone());
        response.setConcursosAprovados(candidato.getConcursosAprovados());
        response.setImagemUrl("/uploads/" + candidato.getImagemFilename());
        response.setCriadoEm(candidato.getCriadoEm());
        return response;
    }
}