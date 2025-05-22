package com.nttdata.product.model;

import lombok.Data;

@Data
public class CustomerDTO {
    private String id;
    private String name;
    private String documentNumber;
    private CustomerType type;
}
