package com.minsu.jpus.persona;

import com.minsu.jpus.persona.dto.CreatePersonaRequest;
import com.minsu.jpus.persona.dto.PersonaResponse;
import com.minsu.jpus.persona.dto.UpdatePersonaRequest;
import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserNotFoundException;
import com.minsu.jpus.user.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PersonaService {
  private final PersonaRepository personaRepository;
  private final UserRepository userRepository;

  public PersonaService(PersonaRepository personaRepository, UserRepository userRepository) {
    this.personaRepository = personaRepository;
    this.userRepository = userRepository;
  }


  public PersonaResponse createPersona(String username,CreatePersonaRequest request){
    User user = userRepository.findByUsername(username).orElseThrow(
        ()-> new UserNotFoundException("username",username)
    );

    return PersonaResponse.from(personaRepository.save(
        new Persona(user,request.displayName(), request.bio())
    ));
  }

  public List<PersonaResponse> getPersonas(Long userId){
    return personaRepository.findAllByUser_IdAndDeletedAtIsNull(userId)
        .stream()
        .map(PersonaResponse::from)
        .toList();
  }
  @Transactional
  public PersonaResponse patchPersona(
      Long personaId,
      Long userId,
      UpdatePersonaRequest request
  ){
    Persona  persona =
    personaRepository.findByIdAndUser_IdAndDeletedAtIsNull(personaId,userId).orElseThrow(
        ()-> new PersonaNotFoundException("persona id",personaId.toString())
        );

    persona.updateProfile(request.displayName(), request.bio());
    return PersonaResponse.from(persona);
  }

  @Transactional
  public PersonaResponse deletePersona(
      Long personaId,
      Long userId
  ){
    Persona  persona =
        personaRepository.findByIdAndUser_IdAndDeletedAtIsNull(personaId,userId).orElseThrow(
            ()-> new PersonaNotFoundException("persona id",personaId.toString())
        );

    persona.delete();

    return PersonaResponse.from(persona);

  }
}
