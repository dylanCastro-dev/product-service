package com.nttdata.product.model;

import com.nttdata.product.model.Details.ProductDetails;
import com.nttdata.product.model.Type.ProductStatus;
import com.nttdata.product.model.Type.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Representa un producto bancario (Ahorro, Cuenta Corriente, Plazo Fijo, Credito)
public class BankProduct {

    @Id
    private String id;

    private String customerId;

    private ProductType type;

    private ProductStatus status;

    private String name;

    private BigDecimal balance;

    private ProductDetails details;

    private List<String> holders;

    private List<String> signers;

}
