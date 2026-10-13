package com.minsu.jpus.persona;

import com.minsu.jpus.persona.dto.CreatePersonaRequest;
import com.minsu.jpus.persona.dto.PersonaResponse;
import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserNotFoundException;
import com.minsu.jpus.user.UserRepository;
import com.minsu.jpus.user.UserService;
import com.minsu.jpus.user.dto.UserResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

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
        ()-> new UserNotFoundException("존재하지 않는 유저입니다. username: "+username)
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
}
