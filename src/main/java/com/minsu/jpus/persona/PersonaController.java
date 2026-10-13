package com.minsu.jpus.persona;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.persona.dto.CreatePersonaRequest;
import com.minsu.jpus.persona.dto.PersonaResponse;
import com.minsu.jpus.user.UserService;
import com.minsu.jpus.user.dto.UserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("persona")
public class PersonaController {
  private final PersonaService personaService;
  private final UserService userService;

  public PersonaController(PersonaService personaService, UserService userService) {
    this.personaService = personaService;
    this.userService = userService;
  }


  @PostMapping
  public ResponseEntity<PersonaResponse> creatPersona(@RequestBody CreatePersonaRequest request,
      Authentication authentication){
    PersonaResponse persona = personaService.createPersona(authentication.getName(),request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(persona);
  }

  @GetMapping
  public List<PersonaResponse> getPersonas(Authentication authentication){
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    System.out.println(userPrincipal.getId());
    return personaService.getPersonas(userPrincipal.getId());
  }

}
