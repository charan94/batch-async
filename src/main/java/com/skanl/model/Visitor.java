package com.skanl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "visitors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Visitor implements Serializable {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDateTime visitDate;

    @Transient
    private String strVisitDate;
}
