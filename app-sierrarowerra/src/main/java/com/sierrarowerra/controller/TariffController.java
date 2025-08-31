package com.sierrarowerra.controller;

import com.sierrarowerra.model.dto.TariffDto;
import com.sierrarowerra.services.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tariffs")
@PreAuthorize("hasRole('ADMIN')") // All methods in this controller are for admins only
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;

    @Operation(summary = "Get a paginated list of all tariffs (Admin only)")
    @GetMapping
    public Page<TariffDto> getAllTariffs(@ParameterObject @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return tariffService.findAll(pageable);
    }

    @Operation(summary = "Get a specific tariff by its ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<TariffDto> getTariffById(@PathVariable Long id) {
        return tariffService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new tariff (Admin only)")
    @PostMapping
    public ResponseEntity<TariffDto> createTariff(@Valid @RequestBody TariffDto tariffDto) {
        TariffDto newTariff = tariffService.createTariff(tariffDto);
        return new ResponseEntity<>(newTariff, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing tariff (Admin only)")
    @PutMapping("/{id}")
    public ResponseEntity<TariffDto> updateTariff(@PathVariable Long id, @Valid @RequestBody TariffDto tariffDto) {
        return tariffService.updateTariff(id, tariffDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a tariff (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTariff(@PathVariable Long id) {
        tariffService.deleteTariff(id);
        return ResponseEntity.noContent().build();
    }
}
