package com.prs.worker.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prs.dtos.PatientDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientRecord {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias("_id")
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;
    private String bloodGroup;

    public static PatientRecord from(PatientDto patientDto) {
        PatientRecord patientRecord = new PatientRecord();
        patientRecord.setFirstName(patientDto.firstName());
        patientRecord.setLastName(patientDto.lastName());
        patientRecord.setDateOfBirth(patientDto.dateOfBirth());
        patientRecord.setGender(patientDto.gender());
        patientRecord.setPhoneNumber(patientDto.phoneNumber());
        patientRecord.setEmail(patientDto.email());
        patientRecord.setAddress(patientDto.address());
        patientRecord.setBloodGroup(patientDto.bloodGroup());
        return patientRecord;
    }

}
