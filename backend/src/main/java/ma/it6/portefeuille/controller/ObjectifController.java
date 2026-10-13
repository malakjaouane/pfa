package ma.it6.portefeuille.controller;

import jakarta.validation.Valid;
import ma.it6.portefeuille.dto.ObjectifDto;
import ma.it6.portefeuille.dto.ObjectifRequest;
import ma.it6.portefeuille.service.ObjectifService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST des objectifs stratégiques.
 */
@RestController
@RequestMapping("/api/objectifs")
public class ObjectifController {

    private final ObjectifService objectifService;

    public ObjectifController(ObjectifService objectifService) {
        this.objectifService = objectifService;
    }

    @GetMapping
    public ResponseEntity<List<ObjectifDto>> lister() {
        return ResponseEntity.ok(objectifService.lister());
    }

    @PostMapping
    public ResponseEntity<ObjectifDto> creer(@Valid @RequestBody ObjectifRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(objectifService.creer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObjectifDto> modifier(@PathVariable Long id,
                                                @Valid @RequestBody ObjectifRequest request) {
        return ResponseEntity.ok(objectifService.modifier(id, request));
    }
}