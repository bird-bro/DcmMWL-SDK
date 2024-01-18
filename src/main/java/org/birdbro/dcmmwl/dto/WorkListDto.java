package org.birdbro.dcmmwl.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WorkListDto implements Serializable {

    private String charSet;
    /*
    0008,0050
     */
    private String accessionNumber;

    /*
    0008,0090
     */
    private String physicianName;

    /*(0010,0010)*/
    private String patientName;

    /*    (0010,0020)*/
    private String patientID;

    /*    (0010,0030)*/
    private String birthDate;

    /*    (0010,0040)*/
    private String patientSex;

    /*    (0010,1030)*/
    private String patientWeight;

    /*    (0010,2000)*/
    private String medicalAlerts;

    /*(0010,2110)*/
    private String allergies;

    /*    (0010,21C0)*/
    private String pregnancyStatus;

    /*        (0020,000D)*/
    private String instanceUID;

    /*        (0032,1032)*/
    private String requestingPhysician;

    /*      (0032,1033)*/
    private String requestingService;

    /*           (0032,1060)*/
    private String requestedProcedureDescription;
    /*            (0038,0010)*/
    private String admissionID;
    /*(0038,0050)*/
    private String specialNeeds;
    /*(0038,0300)*/
    private String currentPatientLocation;
    /*(0038,0500)*/
    private String patientState;
    /*(0040,0100)*/
    private List<SequenceDto> sequenceDtoList;

    /* (0040,1001)*/
    private String requestedProcedureID;
    /*(0040,1003)*/
    private String requestedProcedurePriority;
    /*(0040,1004)*/
    private String patientTransportArrangements;
    /*(0040,2016)*/
    private String placerOrderNumber;
    /*(0040,2017)*/
    private String fillerOrderNumber;
    /*(0040,3001)*/
    private String confidentialityConstraint;
}
