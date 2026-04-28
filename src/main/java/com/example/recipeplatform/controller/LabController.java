package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.service.LabDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab")
@Tag(name = "Lab Demo", description = "Endpoints for demonstrating N+1 and transaction behavior")
public class LabController {

    private final LabDemoService labDemoService;

    public LabController(LabDemoService labDemoService) {
        this.labDemoService = labDemoService;
    }

    @GetMapping("/n-plus-one/problem")
    @Operation(summary = "Demonstrate the N+1 problem")
    public NPlusOneDemoResponse showNPlusOneProblem() {
        return labDemoService.demonstrateNPlusOneProblem();
    }

    @GetMapping("/n-plus-one/solution")
    @Operation(summary = "Show the fetch join solution for N+1")
    public NPlusOneDemoResponse showNPlusOneSolution() {
        return labDemoService.demonstrateFetchJoinSolution();
    }

    @PostMapping("/transactions/without-transactional")
    @Operation(summary = "Show partial save without @Transactional")
    public TransactionDemoResponse demonstratePartialSave() {
        return labDemoService.demonstratePartialSaveWithoutTransactional();
    }

    @PostMapping("/transactions/with-transactional")
    @Operation(summary = "Show rollback with @Transactional")
    public TransactionDemoResponse demonstrateRollback() {
        return labDemoService.demonstrateRollbackWithTransactional();
    }
}
