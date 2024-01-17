package org.birdbro.dcmmwl.dto;

/**
 * @author birdbro
 * @date 15:00 2024-01-17
 **/
public enum DicomTagEnum {

    /**
     *
     **/
    CharSet("00080005"),

    /**
     *
     **/
    SOPClassUID("00080016"),

    /**
     *
     **/
    SOPInstanceUID("00080018"),

    /**
     *
     **/
    StudyDate("00080020"),

    /**
     *
     **/
    SeriesDate("00080021"),

    /**
     *
     **/
    StudyTime("00080030"),

    /**
     *
     **/
    SeriesTime("00080031"),

    /**
     *
     **/
    AccessionNumber("00080050"),

    /**
     *
     **/
    Modality("00080060"),

    /**
     *
     **/
    ConversionType("00080064"),

    /**
     *
     **/
    Manufacturer("00080070"),

    /**
     *
     **/
    InstitutionName("00080080"),

    /**
     *
     **/
    StationName("00081010"),

    /**
     *
     **/
    StudyDescription("00081030"),

    /**
     *
     **/
    PerformingPhysicianName("00081050"),

    /**
     *
     **/
    PatientName("00100010"),

    /**
     *
     **/
    PatientID("00100020"),

    /**
     *
     **/
    PatientBirthDate("00100030"),

    /**
     *
     **/
    PatientSex("00100040"),

    /**
     *
     **/
    PatientAge("00101010"),

    /**
     *
     **/
    PatientSize("00101020"),

    /**
     *
     **/
    PatientWeight("00101030"),

    /**
     *
     **/
    BodyPartExamined("00180015"),

    /**
     *
     **/
    StudyInstanceUID("0020000D"),

    /**
     *
     **/
    SeriesInstanceUID("0020000E"),

    /**
     *
     **/
    StudyID("00200010"),

    /**
     *
     **/
    SeriesNumber("00200011"),

    /**
     *
     **/
    InstanceNumber("00200013"),

    /**
     *
     **/
    NumberofFrames("00280008"),

    /**
     *
     **/
    Rows("00280010"),

    /**
     *
     **/
    Columns("00280011"),

    /**
     *
     **/
    RetrieveAETitle("00080054"),

    /**
     *
     **/
    BitsAllocated("00280100"),

    /**
     *
     **/
    PrivateCreator("00190010"),

    /**
     *
     **/
    PixelData("7FE00010"),

    /**
     *
     **/
    InstanceCreationDate("00080012"),

    /**
     *
     **/
    InstanceCreationTime("00080013"),

    /**
     *
     **/
    SeriesDescription("0008103E"),

    /**
     *
     **/
    ProtocoName("00181030"),

    /**
     *
     **/
    SeriesInstanceCount("00201209"),

    /**
     *
     **/
    InstanceCount("00201208"),

    /**
     *
     **/
    SeriesCount("00201206")
    ;


    private String tag;

    private DicomTagEnum(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
