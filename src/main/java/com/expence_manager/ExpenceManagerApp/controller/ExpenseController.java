package com.expence_manager.ExpenceManagerApp.controller;


import com.expence_manager.ExpenceManagerApp.entity.Expense;
import com.expence_manager.ExpenceManagerApp.service.ExpenseService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws Exception {
        service.upload(file);
        return "OK";
    }

    @GetMapping("/dashboard")
    public Map<String,Object> dashboard(@RequestParam(name = "page") int page , @RequestParam(name = "size") int size) {
        return service.dashboard(page,size);
    }

//    @GetMapping("/alerts")
//    public Map<String, Object> alerts() {
//        String alert = service.spendingAlertThisMonth();
//        Map<String, Object> result = new HashMap<>();
//        result.put("alert", alert);
//        return result;
//    }
}
