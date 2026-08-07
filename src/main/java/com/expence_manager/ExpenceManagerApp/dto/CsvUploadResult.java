package com.expence_manager.ExpenceManagerApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadResult {
    private int successCount;
    private int failureCount;
    private List<RowError> errors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String reason;
    }
}
