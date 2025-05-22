package com.nttdata.product.service;


import com.nttdata.product.model.BankProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankProductService {
    /**
     * Obtiene todos los productos bancarios registrados en el sistema.
     *
     * @return Flux con la lista de productos bancarios
     */
    public Flux<BankProduct> getAll();

    /**
     * Busca un producto bancario por su identificador único.
     *
     * @param id Identificador del producto bancario
     * @return Mono con el producto encontrado o vacío si no existe
     */
    public Mono<BankProduct> getById(String id);

    /**
     * Crea un nuevo producto bancario después de aplicar las validaciones de negocio correspondientes.
     *
     * @param product Producto bancario a registrar
     * @return Mono con el producto creado
     */
    public Mono<BankProduct> create(BankProduct product);

    /**
     * Actualiza la información de un producto bancario existente.
     *
     * @param id Identificador del producto a actualizar
     * @param product Datos nuevos del producto
     * @return Mono con el producto actualizado
     */
    public Mono<BankProduct> update(String id, BankProduct product);

    /**
     * Elimina un producto bancario por su identificador.
     *
     * @param id Identificador del producto a eliminar
     * @return Mono vacío cuando la eliminación se completa
     */
    public Mono<Void> delete(String id);
}
