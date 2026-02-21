package com.saathi.backend.controller;

import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkerController {

    private final WorkerService workerService;

    // GET /api/workers — get all available workers
    @GetMapping
    public ResponseEntity<List<WorkerProfile>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    // GET /api/workers/search?skill=plumber&city=Pune
    @GetMapping("/search")
    public ResponseEntity<List<WorkerProfile>> searchWorkers(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(workerService.searchWorkers(skill, city));
    }

    // GET /api/workers/{id} — get single worker profile
    @GetMapping("/{id}")
    public ResponseEntity<WorkerProfile> getWorkerById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    // PUT /api/workers/{id}/availability — toggle online/offline
    @PutMapping("/{id}/availability")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable Long id
    ) {
        workerService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }
}