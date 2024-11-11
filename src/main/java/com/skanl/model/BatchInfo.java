package com.skanl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "batchInfo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchInfo {
    @Id
    private String id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String message;

}
