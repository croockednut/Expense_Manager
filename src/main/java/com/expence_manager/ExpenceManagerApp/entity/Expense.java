package com.expence_manager.ExpenceManagerApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private String vendorName;
    private String categoryName;
    private String description;
    private boolean anomaly;
}
