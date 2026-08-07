package com.expence_manager.ExpenceManagerApp.repository;


import com.expence_manager.ExpenceManagerApp.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.categoryName = :category")
    BigDecimal avgByCategory(String category);

    // Group by Year and Month for monthly totals per category
    @Query(value = """
        SELECT EXTRACT(YEAR FROM e.expense_date) AS year, EXTRACT(MONTH FROM e.expense_date) AS month, e.category_name AS category, 
     SUM(e.amount) AS total FROM expense e GROUP BY year, month, category """, nativeQuery = true)
    List<Object[]> categorySummaryMonthly();

    @Query("SELECT e.vendorName, SUM(e.amount) FROM Expense e GROUP BY e.vendorName ORDER BY SUM(e.amount) DESC")
    List<Object[]> topVendors(Pageable pageable);

    Page<Expense> findByAnomalyTrue(Pageable pageable);

    boolean existsByAmountAndVendorNameAndExpenseDate(BigDecimal amount, String vendorName, LocalDate expenseDate);

}
