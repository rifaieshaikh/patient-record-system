package com.prs.worker.dao;

import com.prs.worker.configs.FeignConfig;
import com.prs.worker.models.InsuranceDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "insuranceDetailClient",
        url = "${api.insurance-detail.base-url}",
        configuration = FeignConfig.class
)
public interface InsuranceDetailRepository {

    @PostMapping("")
    InsuranceDetail create(@RequestBody InsuranceDetail insuranceDetail);

    @GetMapping("/{id}")
    InsuranceDetail get(@PathVariable("id") String id);

    @PutMapping("/{id}")
    void update(@PathVariable("id") String id, @RequestBody InsuranceDetail insuranceDetail);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") String id);

}
