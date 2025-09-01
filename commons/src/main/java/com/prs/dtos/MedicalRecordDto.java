package com.prs.dtos;

public record MedicalRecordDto(String allergies, String chronicConditions, String currentMedications,
                               String pastSurgeries, String emergencyContact) {
}
