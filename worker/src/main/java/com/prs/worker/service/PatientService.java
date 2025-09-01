package com.prs.worker.service;

import com.prs.worker.models.PatientRecord;
import org.springframework.web.bind.annotation.*;

public interface PatientService {
    PatientRecord create(@RequestBody PatientRecord patientRecord);
    PatientRecord get(@PathVariable String id);
    void update(@PathVariable String id, @RequestBody PatientRecord patientRecord);
    void delete(@PathVariable String id);
}
