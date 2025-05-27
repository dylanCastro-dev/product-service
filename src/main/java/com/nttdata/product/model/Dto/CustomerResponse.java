package com.nttdata.product.model.Dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {
    private int status;
    private String message;
    private List<CustomerDTO> customers;
}