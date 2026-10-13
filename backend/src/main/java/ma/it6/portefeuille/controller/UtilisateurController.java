package ma.it6.portefeuille.controller;

import jakarta.validation.Valid;
import ma.it6.portefeuille.dto.UserRequest;
import ma.it6.portefeuille.dto.UserResponse;
import ma.it6.portefeuille.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST de gestion des utilisateurs (profil ADMINISTRATEUR).
 */
@RestController
@RequestMapping("/api/users")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> lister() {
        return ResponseEntity.ok(utilisateurService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> trouver(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.trouver(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> creer(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> modifier(@PathVariable Long id,
                                                 @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }
}