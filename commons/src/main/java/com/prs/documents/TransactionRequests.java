package com.prs.documents;

import com.prs.constans.TransactionStatus;
import com.prs.constans.TransactionType;
import com.prs.dtos.TransactionRequestDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
public class TransactionRequests {
    @Id
    private String id;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private TransactionRequestDTO payload;
    private String workflowId;
    private PatientCreateMetaData patientCreateMetaData;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Getter
    @Setter
    public static class PatientCreateMetaData {
        private String createdPatientId;
        private String createdMedicalRecordId;
        private String createdInsuranceDetailId;
    }
}
