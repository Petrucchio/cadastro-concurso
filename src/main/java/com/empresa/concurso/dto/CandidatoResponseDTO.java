package com.empresa.concurso.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CandidatoResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private List<String> concursosAprovados;
    private String imagemUrl;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadoEm;
}