package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.service.LabDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    private final LabDemoService labDemoService;

    public LabController(LabDemoService labDemoService) {
        this.labDemoService = labDemoService;
    }

    @GetMapping("/n-plus-one/problem")
    public NPlusOneDemoResponse showNPlusOneProblem() {
        return labDemoService.demonstrateNPlusOneProblem();
    }

    @GetMapping("/n-plus-one/solution")
    public NPlusOneDemoResponse showNPlusOneSolution() {
        return labDemoService.demonstrateFetchJoinSolution();
    }

    @PostMapping("/transactions/without-transactional")
    public TransactionDemoResponse demonstratePartialSave() {
        return labDemoService.demonstratePartialSaveWithoutTransactional();
    }

    @PostMapping("/transactions/with-transactional")
    public TransactionDemoResponse demonstrateRollback() {
        return labDemoService.demonstrateRollbackWithTransactional();
    }
}
