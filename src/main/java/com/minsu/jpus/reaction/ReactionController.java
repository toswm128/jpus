package com.minsu.jpus.reaction;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.reaction.dto.ReactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/reaction")
public class ReactionController {

  private final ReactionService reactionService;


  public ReactionController(ReactionService reactionService) {
    this.reactionService = reactionService;
  }

  @GetMapping
  public ReactionResponse getReactions(@PathVariable("postId") Long postId,
      Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return reactionService.getReactions(userPrincipal.getId(), postId);
  }

  @PostMapping
  public ResponseEntity<Void> reactionPost(@PathVariable("postId") Long postId,
      Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    reactionService.reactionPost(userPrincipal.getId(), postId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteReaction(@PathVariable("postId") Long postId,
      Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    reactionService.deleteReactions(userPrincipal.getId(), postId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

}
