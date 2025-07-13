package com.conectea.backend.service;

import com.conectea.backend.model.Institution;
import com.conectea.backend.repository.InstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstitutionService {

    private static final Logger logger = LoggerFactory.getLogger(InstitutionService.class);
    
    private final InstitutionRepository repository;
    private final GeocodingService geocodingService;

    public InstitutionService(InstitutionRepository repository, 
                             GeocodingService geocodingService) {
        this.repository = repository;
        this.geocodingService = geocodingService;
    }

    public List<Institution> findAll() {
        return repository.findAll();
    }

    public Optional<Institution> findById(Long id) {
        return repository.findById(id);
    }

    public Institution save(Institution institution) {
        if (institution.getEndereco() == null || institution.getEndereco().trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }
        
        logger.info("Buscando coordenadas para: {}", institution.getEndereco());
        double[] coords = geocodingService.getCoordinates(institution.getEndereco());
        
        if (coords != null) {
            logger.info("Coordenadas obtidas: lat={}, lon={}", coords[0], coords[1]);
            institution.setLatitude(coords[0]);
            institution.setLongitude(coords[1]);
        } else {
            logger.warn("Não foi possível obter coordenadas para o endereço");
        }
        return repository.save(institution);
    }

    public Institution updateInstitution(Long id, Institution updatedInstitution) {
        return repository.findById(id)
                .map(institution -> {
                    institution.setNome(updatedInstitution.getNome());
                    institution.setTelefone(updatedInstitution.getTelefone());
                    institution.setEndereco(updatedInstitution.getEndereco());
                    institution.setDescricao(updatedInstitution.getDescricao());
                    
                    if (updatedInstitution.getEndereco() != null && 
                        !updatedInstitution.getEndereco().equals(institution.getEndereco())) {
                        double[] coords = geocodingService.getCoordinates(updatedInstitution.getEndereco());
                        if (coords != null) {
                            institution.setLatitude(coords[0]);
                            institution.setLongitude(coords[1]);
                        }
                    }
                    return repository.save(institution);
                })
                .orElseGet(() -> {
                    updatedInstitution.setId(id);
                    return repository.save(updatedInstitution);
                });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}