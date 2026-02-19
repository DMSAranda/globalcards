package com.bank.globalcards.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardUploadResult {

    private String fileName;
    private int recordsProcessed;
    private int recordsFailed;
}
