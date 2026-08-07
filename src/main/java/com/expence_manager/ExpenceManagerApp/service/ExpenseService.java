package com.expence_manager.ExpenceManagerApp.service;

import com.expence_manager.ExpenceManagerApp.dto.CsvUploadResult;
import com.expence_manager.ExpenceManagerApp.entity.Expense;
import com.expence_manager.ExpenceManagerApp.repository.ExpenseRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repo;

    private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("swiggy", "Food"),
            Map.entry("zomato", "Food"),
            Map.entry("uber", "Transport"),
            Map.entry("ola", "Transport"),
            Map.entry("amazon", "Shopping"),
            Map.entry("flipkart", "Shopping"),
            Map.entry("netflix", "Entertainment"),
            Map.entry("electricity", "Bills")
    );

    /** Single source of truth for vendor → category mapping. Trims and case-insensitive. */
    public String categorize(String vendorName) {
        if (vendorName == null) return "Other";
        return CATEGORY_KEYWORDS.getOrDefault(vendorName.trim().toLowerCase(), "Other");
    }

    public Expense add(Expense e) {
        if (e.getVendorName() == null || e.getVendorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name is required.");
        }
        if (e.getAmount() == null || e.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        if (e.getExpenseDate() == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        String category = categorize(e.getVendorName());
        e.setCategoryName(category);

        if (isDuplicate(e)) {
            throw new RuntimeException("Duplicate expense detected!");
        }

        // Calculate average BEFORE saving so new expense doesn't skew its own baseline
        BigDecimal avg = repo.avgByCategory(category);
        if (avg != null) {
            BigDecimal limit = avg.multiply(BigDecimal.valueOf(3));
            if (e.getAmount().compareTo(limit) > 0) {
                e.setAnomaly(true);
            }
        }

        return repo.save(e);
    }

    public CsvUploadResult upload(MultipartFile file) throws Exception {
        CsvUploadResult result = new CsvUploadResult();
        result.setErrors(new ArrayList<>());

        // CSVReader handles quoted fields (descriptions with commas)
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header;
            try {
                header = reader.readNext();
            } catch (CsvValidationException e) {
                throw new IllegalArgumentException("Could not read CSV header.");
            }

            if (header == null) {
                throw new IllegalArgumentException("CSV file is empty.");
            }

            // Validate expected columns (case-insensitive)
            String[] expectedColumns = {"date", "amount", "vendor", "description"};
            if (header.length < 4) {
                throw new IllegalArgumentException(
                        "CSV must have at least 4 columns: Date, Amount, Vendor, Description.");
            }
            for (int i = 0; i < expectedColumns.length; i++) {
                if (!header[i].trim().equalsIgnoreCase(expectedColumns[i])) {
                    throw new IllegalArgumentException(
                            "Unexpected column at position " + (i + 1) + ": expected '"
                                    + expectedColumns[i] + "', got '" + header[i].trim() + "'.");
                }
            }

            String[] row;
            int rowNumber = 1; // 1-based; header was row 0
            while (true) {
                try {
                    row = reader.readNext();
                } catch (CsvValidationException e) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber,
                            "Malformed CSV row: " + e.getMessage()));
                    result.setFailureCount(result.getFailureCount() + 1);
                    rowNumber++;
                    continue;
                }
                if (row == null) break;
                rowNumber++;

                // Skip blank lines
                if (row.length == 0 || Arrays.stream(row).allMatch(s -> s.trim().isEmpty())) {
                    continue;
                }

                if (row.length < 3) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber,
                            "Row has fewer than 3 columns (Date, Amount, Vendor required)."));
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }

                // Validate date
                String dateStr = row[0].trim();
                LocalDate expenseDate;
                try {
                    expenseDate = LocalDate.parse(dateStr);
                } catch (DateTimeParseException ex) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber,
                            "Invalid date '" + dateStr + "'. Expected format: YYYY-MM-DD."));
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }

                // Validate amount
                String amountStr = row[1].trim();
                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountStr);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException("non-positive");
                    }
                } catch (NumberFormatException ex) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber,
                            "Invalid amount '" + amountStr + "'. Must be a positive number."));
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }

                // Validate vendor
                String vendor = row[2].trim();
                if (vendor.isEmpty()) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber,
                            "Vendor name is empty."));
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }

                String description = row.length >= 4 ? row[3].trim() : "";

                Expense expense = new Expense();
                expense.setExpenseDate(expenseDate);
                expense.setAmount(amount);
                expense.setVendorName(vendor);
                expense.setDescription(description);

                try {
                    add(expense);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (RuntimeException ex) {
                    result.getErrors().add(new CsvUploadResult.RowError(rowNumber, ex.getMessage()));
                    result.setFailureCount(result.getFailureCount() + 1);
                }
            }
        }

        return result;
    }

    public Map<String, Object> dashboard(int page, int size) {

        Map<String, Object> result = new HashMap<>();

        result.put("categories", repo.categorySummaryMonthly());
        result.put("vendors", repo.topVendors(PageRequest.of(0, 5)));

        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> anomalyPage = repo.findByAnomalyTrue(pageable);

        result.put("anomalies", anomalyPage.getContent());
        result.put("anomalyCount", anomalyPage.getTotalElements());
        result.put("totalPages", anomalyPage.getTotalPages());
        result.put("currentPage", anomalyPage.getNumber());

        return result;
    }


    public boolean isDuplicate(Expense e) {
        return repo.existsByAmountAndVendorNameAndExpenseDate(
                e.getAmount(),
                e.getVendorName(),
                e.getExpenseDate()
        );
    }
}
