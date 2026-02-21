package com.saathi.backend.service;

import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;

    // Get all available workers
    public List<WorkerProfile> getAllWorkers() {
        return workerRepository.findByIsAvailableTrue();
    }

    // Search by skill and/or city
    public List<WorkerProfile> searchWorkers(String skill, String city) {

        // both provided
        if (skill != null && city != null) {
            return workerRepository.findBySkillAndCity(skill, city);
        }

        // only skill provided
        if (skill != null) {
            return workerRepository.findBySkillsContaining(skill);
        }

        // only city provided
        if (city != null) {
            return workerRepository.findByCityAndIsAvailableTrue(city);
        }

        // nothing provided — return all available
        return workerRepository.findByIsAvailableTrue();
    }

    // Get single worker by ID
    public WorkerProfile getWorkerById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));
    }

    // Toggle availability on/off
    public void toggleAvailability(Long id) {
        WorkerProfile worker = workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));

        worker.setIsAvailable(!worker.getIsAvailable());
        workerRepository.save(worker);
    }
}