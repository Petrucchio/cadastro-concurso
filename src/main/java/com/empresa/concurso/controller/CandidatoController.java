package com.empresa.concurso.controller;

import com.empresa.concurso.dto.CandidatoDTO;
import com.empresa.concurso.dto.CandidatoResponseDTO;
import com.empresa.concurso.service.CandidatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST para gerenciamento de candidatos aprovados em concursos.
 * 
 * @author DevSecOps Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/candidatos")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CandidatoController {

    private final CandidatoService candidatoService;

    /**
     * Cria um novo candidato com upload de imagem.
     * 
     * @param dto Dados do candidato (validados)
     * @param imagem Arquivo de imagem (JPG/PNG, máx 5MB)
     * @return Candidato criado com status 201
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidatoResponseDTO> criarCandidato(
            @Valid @ModelAttribute CandidatoDTO dto,
            @RequestParam("imagem") MultipartFile imagem) {
        
        log.info("Recebendo requisição de cadastro para: {}", dto.getEmail());
        
        CandidatoResponseDTO response = candidatoService.salvarCandidato(dto, imagem);
        
        log.info("Candidato {} cadastrado com sucesso. ID: {}", dto.getEmail(), response.getId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Lista todos os candidatos cadastrados.
     * 
     * @return Lista de candidatos
     */
    @GetMapping
    public ResponseEntity<List<CandidatoResponseDTO>> listarTodos() {
        log.info("Listando todos os candidatos");
        return ResponseEntity.ok(candidatoService.listarTodos());
    }

    /**
     * Busca um candidato específico por ID.
     * 
     * @param id ID do candidato
     * @return Candidato encontrado ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidatoResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("Buscando candidato com ID: {}", id);
        
        return candidatoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint de health check.
     * 
     * @return Status OK
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Candidatos está funcionando!");
    }
}