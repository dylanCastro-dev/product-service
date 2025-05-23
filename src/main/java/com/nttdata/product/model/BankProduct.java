package com.nttdata.product.model;

import com.nttdata.product.model.Type.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Representa un producto bancario (cuenta, crédito, tarjeta)")
public class BankProduct {

    @Id
    @Schema(description = "ID autogenerado del producto", example = "663018e0ac82a12a8445a9b0", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "ID del cliente asociado al producto", example = "cust1234", required = true)
    private String customerId;

    @Schema(description = "Tipo de producto bancario", example = "AHORRO", required = true)
    private ProductType type;

    @Schema(description = "Nombre descriptivo del producto", example = "Cuenta Ahorro Soles")
    private String name;

    @Schema(description = "Saldo actual del producto", example = "1000.50")
    private BigDecimal balance;

    @Schema(description = "Indica el monto de comisión de mantenimiento", example = "5.00")
    private Double maintenanceFee;

    @Schema(description = "Límite mensual de movimientos permitidos", example = "5")
    private Integer monthlyLimit;

    @Schema(description = "Límite de crédito para tarjetas", example = "5000.00")
    private BigDecimal creditLimit;

    @Schema(description = "Titulares de la cuenta bancaria empresarial", example = "[\"12345678\", \"87654321\"]")
    private List<String> holders;

    @Schema(description = "Firmantes autorizados (opcional)", example = "[\"99887766\"]")
    private List<String> signers;

    @Schema(description = "Día del mes permitido para operar (1-31)", example = "15")
    private Integer allowedTransactionDay;
}
