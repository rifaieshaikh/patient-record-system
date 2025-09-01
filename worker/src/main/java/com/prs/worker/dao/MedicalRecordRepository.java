package com.prs.worker.dao;

import com.prs.worker.configs.FeignConfig;
import com.prs.worker.models.MedicalRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "medicalRecordClient",
        url = "${api.medical-record.base-url}",
        configuration = FeignConfig.class
)
public interface MedicalRecordRepository {

    @PostMapping("")
    MedicalRecord create(@RequestBody MedicalRecord medicalRecord);

    @GetMapping("/{id}")
    MedicalRecord get(@PathVariable("id") String id);

    @PutMapping("/{id}")
    void update(@PathVariable("id") String id, @RequestBody MedicalRecord patientRecord);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") String id);

}
