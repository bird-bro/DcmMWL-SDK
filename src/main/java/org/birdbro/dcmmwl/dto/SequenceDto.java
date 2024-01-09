package org.birdbro.dcmmwl.dto;

import lombok.Data;

@Data
public class SequenceDto {

    /*(0008,0060)*/
    private String modality;
    /*(0032,1070)*/
    private String contrastAgent;
    /*(0040,0001)*/
    private String aeTitle;
    /*(0040,0002)*/
    private String stepStartDate;
    /*(0040,0003)*/
    private String stepStartTime;
    /*(0040,0006)*/
    private String physicianName;
    /*(0040,0007)*/
    private String description;
    /*(0040,0009)*/
    private String stepID;
    /*(0040,0010)*/
    private String stationName;
    /*(0040,0011)*/
    private String stepLocation;
    /*(0040,0012)*/
    private String preMedication;
    /*(0040,0020)*/
    private String stepStatus;

}
