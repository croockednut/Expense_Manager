package com.expence_manager.ExpenceManagerApp.service;

import com.expence_manager.ExpenceManagerApp.entity.Expense;
import com.expence_manager.ExpenceManagerApp.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

    public Expense add(Expense e) {

        String category = CATEGORY_KEYWORDS.getOrDefault(e.getVendorName().toLowerCase(), "Other");
        e.setCategoryName(category);


        if (isDuplicate(e)) {
            throw new RuntimeException("Duplicate expense detected!");
        }

        BigDecimal avg = repo.avgByCategory(category);
        if (avg != null) {
            BigDecimal limit = avg.multiply(BigDecimal.valueOf(3));
            if (e.getAmount().compareTo(limit) > 0) {
                e.setAnomaly(true);
            }
        }

        return repo.save(e);
    }

    public void upload(MultipartFile file) throws Exception {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        );

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.trim().isEmpty()) continue;

            String[] data = line.split(",");
            if (data.length < 4) continue;

            Expense expense = new Expense();
            expense.setExpenseDate(LocalDate.parse(data[0].trim()));
            expense.setAmount(new BigDecimal(data[1].trim()));
            expense.setVendorName(data[2].trim());
            expense.setDescription(data[3].trim());

            add(expense);
        }
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

//    public String spendingAlertThisMonth() {
//        List<Object[]> data = repo.categorySummaryMonthly();
//        BigDecimal total = data.stream()
//                                .map(o -> (BigDecimal) o[1])
//                                .filter(Objects::nonNull)
//                                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        System.out.println(data);
//
//        for (Object[] row : data) {
//            String category = (String) row[2];
//            BigDecimal amount = (BigDecimal) row[1];
//            BigDecimal ratio = amount.divide(total, 4, RoundingMode.HALF_UP);
//            if (ratio.compareTo(BigDecimal.valueOf(0.4)) > 0) {
//                return "You're overspending on " + category ;
//            }
//
//        }
//        return null;
//    }

}