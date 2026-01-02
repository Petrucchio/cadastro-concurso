package com.empresa.concurso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidatos", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_criado_em", columnList = "criado_em")
})
public class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 15)
    private String telefone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "candidato_concursos",
        joinColumns = @JoinColumn(name = "candidato_id")
    )
    @Column(name = "concurso", length = 200)
    private List<String> concursosAprovados;

    @Column(nullable = false, length = 255)
    private String imagemFilename;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}