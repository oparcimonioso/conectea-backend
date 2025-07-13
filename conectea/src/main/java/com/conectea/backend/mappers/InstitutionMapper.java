package com.conectea.backend.mappers;

import com.conectea.backend.model.Institution;
import com.conectea.backend.dto.InstitutionDTO;
import java.util.stream.Collectors;

public class InstitutionMapper {

    public static InstitutionDTO toDTO(Institution institution) {
        if (institution == null) return null;
        InstitutionDTO dto = new InstitutionDTO();
        dto.setId(institution.getId());
        dto.setNome(institution.getNome());
        dto.setTelefone(institution.getTelefone());
        dto.setEndereco(institution.getEndereco());
        dto.setLatitude(institution.getLatitude());
        dto.setLongitude(institution.getLongitude());
        dto.setDescricao(institution.getDescricao());

        if (institution.getReviews() != null) {
            dto.setReviews(institution.getReviews()
                .stream()
                .map(ReviewMapper::toDTO)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    public static Institution toEntity(InstitutionDTO dto) {
        if (dto == null) return null;
        Institution inst = new Institution();
        inst.setId(dto.getId());
        inst.setNome(dto.getNome());
        inst.setTelefone(dto.getTelefone());
        inst.setEndereco(dto.getEndereco());
        inst.setLatitude(dto.getLatitude());
        inst.setLongitude(dto.getLongitude());
        inst.setDescricao(dto.getDescricao());
        return inst;
    }
}