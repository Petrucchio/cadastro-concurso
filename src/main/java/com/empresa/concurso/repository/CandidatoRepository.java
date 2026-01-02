package com.empresa.concurso.repository;

import com.empresa.concurso.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para acesso a dados de Candidatos.
 * Utiliza Spring Data JPA para operações de persistência.
 */
@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
    
    /**
     * Verifica se já existe candidato com o email informado.
     */
    boolean existsByEmail(String email);

    /**
     * Busca candidato por email.
     */
    Optional<Candidato> findByEmail(String email);
}
