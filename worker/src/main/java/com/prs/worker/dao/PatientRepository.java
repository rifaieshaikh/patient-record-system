package com.prs.worker.dao;

import com.prs.worker.configs.FeignConfig;
import com.prs.worker.models.PatientRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "patientClient",
        url = "${api.patient.base-url}",
        configuration = FeignConfig.class
)
public interface PatientRepository {

    @PostMapping("")
    PatientRecord create(@RequestBody PatientRecord patientRecord);

    @GetMapping("/{id}")
    PatientRecord get(@PathVariable("id") String id);

    @PutMapping("/{id}")
    void update(@PathVariable("id") String id, @RequestBody PatientRecord patientRecord);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") String id);
}
