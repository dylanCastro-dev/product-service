package com.nttdata.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "debitCard")
public class DebitCard {

    @Id
    private String id;

    private String cardNumber;

    private String customerId;

    private String primaryAccountId;

    private List<String> linkedAccountIds;

    private boolean active;
}
