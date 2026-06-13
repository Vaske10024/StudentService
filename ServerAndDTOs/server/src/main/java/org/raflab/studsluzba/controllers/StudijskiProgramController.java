package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.StudijskiProgramDTO;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/studprogram")
@RequiredArgsConstructor
public class StudijskiProgramController {
    private final StudijskiProgramRepository repo;
    private final EntityMappers entityMappers;

    @GetMapping("/all/sorted")
    public List<StudijskiProgramDTO> allSorted() {
        return repo.getAllSortedByGodinaDesc().stream()
                .map(entityMappers::fromStudijskiProgramToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<StudijskiProgramDTO> all() {
        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .map(entityMappers::fromStudijskiProgramToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/oznaka/{oznaka}")
    public List<StudijskiProgramDTO> byOznaka(@PathVariable String oznaka) {
        return repo.findByOznaka(oznaka).stream()
                .map(entityMappers::fromStudijskiProgramToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public StudijskiProgramDTO one(@PathVariable Long id) {
        StudijskiProgram sp = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Studijski program ne postoji: " + id));
        return entityMappers.fromStudijskiProgramToDTO(sp);
    }
}
