package ma.it6.portefeuille.controller;

import jakarta.validation.Valid;
import ma.it6.portefeuille.dto.EtapeRequest;
import ma.it6.portefeuille.dto.EtapeResponse;
import ma.it6.portefeuille.service.EtapeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST des étapes d'une initiative.
 *
 * <p>Lecture : {@code INITIATIVE_CONSULTER} (rattrapée par le GET des
 * initiatives). Écriture : {@code ETAPE_GERER}.</p>
 */
@RestController
@RequestMapping("/api")
public class EtapeController {

    private final EtapeService etapeService;

    public EtapeController(EtapeService etapeService) {
        this.etapeService = etapeService;
    }

    @GetMapping("/initiatives/{idInitiative}/etapes")
    public ResponseEntity<List<EtapeResponse>> lister(@PathVariable Long idInitiative) {
        return ResponseEntity.ok(etapeService.lister(idInitiative));
    }

    @PostMapping("/initiatives/{idInitiative}/etapes")
    public ResponseEntity<EtapeResponse> creer(@PathVariable Long idInitiative,
                                               @Valid @RequestBody EtapeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etapeService.creer(idInitiative, request));
    }

    @PutMapping("/etapes/{idEtape}")
    public ResponseEntity<EtapeResponse> modifier(@PathVariable Long idEtape,
                                                  @Valid @RequestBody EtapeRequest request) {
        return ResponseEntity.ok(etapeService.modifier(idEtape, request));
    }

    @DeleteMapping("/etapes/{idEtape}")
    public ResponseEntity<Void> supprimer(@PathVariable Long idEtape) {
        etapeService.supprimer(idEtape);
        return ResponseEntity.noContent().build();
    }

    /**
     * Réordonnancement : body = liste des identifiants dans l'ordre souhaité.
     */
    @PutMapping("/initiatives/{idInitiative}/etapes/ordre")
    public ResponseEntity<List<EtapeResponse>> reordonner(@PathVariable Long idInitiative,
                                                          @RequestBody List<Long> idsOrdonnees) {
        return ResponseEntity.ok(etapeService.reordonner(idInitiative, idsOrdonnees));
    }
}
