package com.orangetoolztech.assessment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    @Id
    private String email;//6
    private String phoneNumber;//5
    private String name;//0

    private String col1;
    private String col2;
    private String col3;
    private String col4;
    private String col7;
}
