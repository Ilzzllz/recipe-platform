package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.CounterStatsDto;
import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RaceConditionDemoResultDto;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeFilterDto;
import com.example.recipeplatform.service.NutritionReportService;
import com.example.recipeplatform.service.RecipeService;
import com.example.recipeplatform.service.RecipeViewCounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;
    @Mock
    private RecipeViewCounterService viewCounterService;
    @Mock
    private NutritionReportService nutritionReportService;

    private RecipeController controller;

    @BeforeEach
    void setUp() {
        controller = new RecipeController(recipeService, viewCounterService, nutritionReportService);
    }

    @Test
    void crudEndpointsDelegateToRecipeService() {
        RecipeDto dto = new RecipeDto();
        RecipeCreateDto request = new RecipeCreateDto();
        when(recipeService.findAll()).thenReturn(List.of(dto));
        when(recipeService.getById(1L)).thenReturn(dto);
        when(recipeService.searchByTitle("борщ")).thenReturn(List.of(dto));
        when(recipeService.create(request)).thenReturn(dto);
        when(recipeService.update(1L, request)).thenReturn(dto);
        when(recipeService.createBulk(List.of(request))).thenReturn(List.of(dto));
        when(recipeService.createBulkWithoutTransaction(List.of(request))).thenReturn(List.of(dto));

        assertThat(controller.getAll()).containsExactly(dto);
        assertThat(controller.getById(1L)).isSameAs(dto);
        assertThat(controller.getByTitle("борщ")).containsExactly(dto);
        assertThat(controller.create(request)).isSameAs(dto);
        assertThat(controller.update(1L, request)).isSameAs(dto);
        assertThat(controller.createBulk(List.of(request))).containsExactly(dto);
        assertThat(controller.createBulkWithoutTransaction(List.of(request))).containsExactly(dto);
        controller.delete(1L);

        verify(viewCounterService).recordView();
        verify(recipeService).delete(1L);
    }

    @Test
    void demoAndFilterEndpointsDelegateToServices() {
        NPlusOneDemoResponse problem = new NPlusOneDemoResponse();
        NPlusOneDemoResponse solution = new NPlusOneDemoResponse();
        Pageable pageable = PageRequest.of(0, 5);
        Page<RecipeFilterDto> page = new PageImpl<>(List.of(new RecipeFilterDto()));
        CounterStatsDto stats = new CounterStatsDto();
        RaceConditionDemoResultDto race = mock(RaceConditionDemoResultDto.class);
        when(recipeService.demonstrateNPlusOneProblem()).thenReturn(problem);
        when(recipeService.demonstrateNPlusOneSolution()).thenReturn(solution);
        when(recipeService.findByAuthorAndCategoryJPQL("anna", "Супы", pageable)).thenReturn(page);
        when(recipeService.findByAuthorAndCategoryNative("anna", "Супы", pageable)).thenReturn(page);
        when(viewCounterService.getStats()).thenReturn(stats);
        when(viewCounterService.demonstrateRaceCondition(50, 100)).thenReturn(race);

        assertThat(controller.showNPlusOneProblem()).isSameAs(problem);
        assertThat(controller.showNPlusOneSolution()).isSameAs(solution);
        assertThat(controller.filterByAuthorAndCategoryJPQL("anna", "Супы", pageable)).isSameAs(page);
        assertThat(controller.filterByAuthorAndCategoryNative("anna", "Супы", pageable)).isSameAs(page);
        assertThat(controller.getViewCounterStats()).isSameAs(stats);
        assertThat(controller.demonstrateRaceCondition(50, 100)).isSameAs(race);

        ResponseEntity<Void> reset = controller.resetViewCounters();
        assertThat(reset.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(viewCounterService).reset();
    }

    @Test
    void nutritionReportEndpointsReturnTaskState() {
        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        when(nutritionReportService.startNutritionReport(1L)).thenReturn(taskId);
        when(nutritionReportService.getTaskStatus(taskId)).thenReturn(task);
        when(nutritionReportService.pollTaskStatus(taskId)).thenReturn(task);

        ResponseEntity<AsyncTaskResponseDto> started = controller.startNutritionReport(1L);

        assertThat(started.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(started.getBody()).isSameAs(task);
        assertThat(controller.getNutritionReportStatus(taskId)).isSameAs(task);
    }
}
