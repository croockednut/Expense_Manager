package com.expence_manager.ExpenceManagerApp.controller;


import com.expence_manager.ExpenceManagerApp.dto.CsvUploadResult;
import com.expence_manager.ExpenceManagerApp.entity.Expense;
import com.expence_manager.ExpenceManagerApp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping("/expenses")
    public ResponseEntity<?> add(@RequestBody Expense expense) {
        try {
            return ResponseEntity.ok(service.add(expense));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file) {
        try {
            CsvUploadResult result = service.upload(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process CSV: " + ex.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam(name = "page") int page,
                                         @RequestParam(name = "size") int size) {
        return service.dashboard(page, size);
    }
}
