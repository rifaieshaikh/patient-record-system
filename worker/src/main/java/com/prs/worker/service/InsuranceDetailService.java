package com.prs.worker.service;

import com.prs.worker.models.InsuranceDetail;
import org.springframework.web.bind.annotation.*;

public interface InsuranceDetailService {
    InsuranceDetail create(@RequestBody InsuranceDetail insuranceDetail);
    InsuranceDetail get(@PathVariable String id);
    void update(@PathVariable String id, @RequestBody InsuranceDetail insuranceDetail);
    void delete(@PathVariable String id);
}
