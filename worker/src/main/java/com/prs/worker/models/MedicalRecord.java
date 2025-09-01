package com.prs.worker.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prs.dtos.MedicalRecordDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecord {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias("_id")
    private String id;
    String allergies;
    String chronicConditions;
    String currentMedications;
    String pastSurgeries;
    String emergencyContact;

    public static MedicalRecord from(MedicalRecordDto medicalRecordDto) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAllergies(medicalRecordDto.allergies());
        medicalRecord.setChronicConditions(medicalRecordDto.chronicConditions());
        medicalRecord.setCurrentMedications(medicalRecordDto.currentMedications());
        medicalRecord.setPastSurgeries(medicalRecordDto.pastSurgeries());
        medicalRecord.setEmergencyContact(medicalRecordDto.emergencyContact());
        return medicalRecord;
    }
}
