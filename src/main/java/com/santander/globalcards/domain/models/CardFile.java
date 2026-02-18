package com.santander.globalcards.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardFile {

    private String fileName;
    private long size;
    private int totalRecords;
    private boolean multipart;
}
