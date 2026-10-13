package com.minsu.jpus.persona;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository
    extends JpaRepository<Persona, Long> {
  List<Persona> findAllByUser_IdAndDeletedAtIsNull(Long userId);
  Optional<Persona> findByIdAndUser_IdAndDeletedAtIsNull(
      Long personaId,
      Long userId
  );
}
