package com.empresa.concurso.service;

import com.empresa.concurso.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service responsável pelo armazenamento físico de arquivos.
 * Em produção, poderia ser substituído por implementação com S3/MinIO.
 */
@Slf4j
@Service
public class StorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Diretório de upload criado: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Erro ao criar diretório de upload", e);
            throw new StorageException("Não foi possível inicializar o storage", e);
        }
    }

    /**
     * Salva arquivo no filesystem com nome único.
     * 
     * @return Nome do arquivo salvo (UUID + extensão)
     */
    public String salvarArquivo(MultipartFile file) {
        try {
            String nomeOriginal = file.getOriginalFilename();
            String extensao = extrairExtensao(nomeOriginal);
            String nomeUnico = UUID.randomUUID().toString() + extensao;

            Path destinoPath = uploadPath.resolve(nomeUnico);
            Files.copy(file.getInputStream(), destinoPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Arquivo salvo: {} -> {}", nomeOriginal, nomeUnico);
            return nomeUnico;

        } catch (IOException e) {
            log.error("Erro ao salvar arquivo", e);
            throw new StorageException("Falha ao salvar arquivo", e);
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return ".jpg"; // Fallback
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }
}