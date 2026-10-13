package ma.it6.portefeuille.controller;

import jakarta.validation.Valid;
import ma.it6.portefeuille.dto.JalonRequest;
import ma.it6.portefeuille.dto.JalonResponse;
import ma.it6.portefeuille.service.JalonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST des jalons d'une initiative.
 *
 * <p>Lecture : {@code INITIATIVE_CONSULTER} (rattrapée par le GET des
 * initiatives). Écriture : {@code JALON_GERER}.</p>
 */
@RestController
@RequestMapping("/api")
public class JalonController {

    private final JalonService jalonService;

    public JalonController(JalonService jalonService) {
        this.jalonService = jalonService;
    }

    @GetMapping("/initiatives/{idInitiative}/jalons")
    public ResponseEntity<List<JalonResponse>> lister(@PathVariable Long idInitiative) {
        return ResponseEntity.ok(jalonService.lister(idInitiative));
    }

    @PostMapping("/initiatives/{idInitiative}/jalons")
    public ResponseEntity<JalonResponse> creer(@PathVariable Long idInitiative,
                                               @Valid @RequestBody JalonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jalonService.creer(idInitiative, request));
    }

    @PutMapping("/jalons/{idJalon}")
    public ResponseEntity<JalonResponse> modifier(@PathVariable Long idJalon,
                                                  @Valid @RequestBody JalonRequest request) {
        return ResponseEntity.ok(jalonService.modifier(idJalon, request));
    }

    @DeleteMapping("/jalons/{idJalon}")
    public ResponseEntity<Void> supprimer(@PathVariable Long idJalon) {
        jalonService.supprimer(idJalon);
        return ResponseEntity.noContent().build();
    }
}
