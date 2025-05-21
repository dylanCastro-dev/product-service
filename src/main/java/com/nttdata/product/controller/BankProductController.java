package com.nttdata.product.controller;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.service.BankProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class BankProductController {

    private final BankProductService service;

    @Operation(summary = "Obtener todos los productos", description = "Devuelve la lista completa de productos bancarios")
    @GetMapping
    public Flux<BankProduct> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Obtener un producto por ID", description = "Busca un producto bancario usando su ID")
    @GetMapping("/{id}")
    public Mono<BankProduct> getById(
            @Parameter(description = "ID del producto a consultar", required = true)
            @PathVariable String id) {
        return service.getById(id);
    }

    @Operation(summary = "Crear un nuevo producto bancario", description = "Registra un nuevo producto (cuenta, crédito, tarjeta) asociado a un cliente")
    @PostMapping
    public Mono<BankProduct> create(
            @Parameter(description = "Datos del producto bancario a crear", required = true)
            @RequestBody BankProduct product) {
        return service.create(product);
    }

    @Operation(summary = "Actualizar un producto bancario", description = "Actualiza los datos de un producto existente mediante su ID")
    @PutMapping("/{id}")
    public Mono<BankProduct> update(
            @Parameter(description = "ID del producto a actualizar", required = true)
            @PathVariable String id,
            @Parameter(description = "Datos actualizados del producto", required = true)
            @RequestBody BankProduct product) {
        return service.update(id, product);
    }

    @Operation(summary = "Eliminar un producto bancario", description = "Elimina un producto por su ID")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(
            @Parameter(description = "ID del producto a eliminar", required = true)
            @PathVariable String id) {
        return service.delete(id);
    }
}
