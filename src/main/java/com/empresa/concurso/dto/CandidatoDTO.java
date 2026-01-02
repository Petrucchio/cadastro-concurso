package com.empresa.concurso.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CandidatoDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(
        regexp = "^\\d{10,11}$",
        message = "Telefone deve conter 10 ou 11 dígitos numéricos"
    )
    private String telefone;

    @NotBlank(message = "Concursos aprovados são obrigatórios")
    @Size(max = 500, message = "Lista de concursos muito extensa")
    private String concursos;
}
