package com.nttdata.product.model.Dto;

import com.nttdata.product.model.Type.CustomerType;
import com.nttdata.product.model.Type.ProfileType;
import lombok.Data;

@Data
public class CustomerDTO {
    private String id;
    private String name;
    private String documentNumber;
    private CustomerType type;
    private ProfileType profile;
}
