package com.conectea.backend.controller;

import com.conectea.backend.dto.InstitutionDTO;
import com.conectea.backend.mappers.InstitutionMapper;
import com.conectea.backend.model.Institution;
import com.conectea.backend.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public List<InstitutionDTO> getAllInstitutions() {
        return institutionService.findAll().stream()
                .map(InstitutionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionDTO> getInstitutionById(@PathVariable Long id) {
        return institutionService.findById(id)
                .map(value -> ResponseEntity.ok(InstitutionMapper.toDTO(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInstitution(@RequestBody Institution institution) {
        try {
            Institution saved = institutionService.save(institution);
            return ResponseEntity.ok(InstitutionMapper.toDTO(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar instituição: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> updateInstitution(
            @PathVariable Long id,
            @RequestBody Institution updatedInstitution
    ) {
        Institution inst = institutionService.updateInstitution(id, updatedInstitution);
        return ResponseEntity.ok(InstitutionMapper.toDTO(inst));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}