package com.bank.globalcards.domain.models;

import java.time.LocalDateTime;
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
    private LocalDateTime uploadTimestamp;
    private String s3Path;
    private String batchId;
}
