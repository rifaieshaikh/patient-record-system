package com.prs.worker.service;

import com.prs.worker.models.MedicalRecord;
import org.springframework.web.bind.annotation.*;

public interface MedicalRecordService {
    MedicalRecord create(@RequestBody MedicalRecord medicalRecord);
    MedicalRecord get(@PathVariable String id);
    void update(@PathVariable String id, @RequestBody MedicalRecord patientRecord);
    void delete(@PathVariable String id);
}
