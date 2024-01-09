package org.birdbro.dcmmwl.dto;

import lombok.Data;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;

import java.util.concurrent.Executor;

/**
 * @author birdbro
 */
@Data
public class ConnDto {
//    public static final int KB = 1024;
    private static final String USAGE = "dcmmwl <aet>[@<host>[:<port>]] [Options]";
    private static final String DESCRIPTION =
            "Query specified remote Application Entity (=Modality Worklist SCP) " +
                    "If <port> is not specified, DICOM default port 104 is assumed. " +
                    "If also no <host> is specified localhost is assumed.\n" +
                    "Options:";
    private static final String EXAMPLE =
            "\nExample: dcmmwl MWLSCP@localhost:11112 -mod CT -date 20060502\n" +
                    "=> Query Application Entity MWLSCP listening on local port 11112 for " +
                    "CT procedure steps scheduled for May 2, 2006.";

    public static String[] TLS1 = {"TLSv1"};

    public static String[] SSL3 = {"SSLv3"};

    public static String[] NO_TLS1 = {"SSLv3", "SSLv2Hello"};

    public static String[] NO_SSL2 = {"TLSv1", "SSLv3"};

    public static String[] NO_SSL3 = {"TLSv1", "SSLv2Hello"};

    private static char[] SECRET = {'s', 'e', 'c', 'r', 'e', 't'};

    public static final int[] RETURN_KEYS = {
            Tag.AccessionNumber,
            Tag.ReferringPhysicianName,
            Tag.PatientName,
            Tag.PatientID,
            Tag.PatientBirthDate,
            Tag.PatientSex,
            Tag.PatientWeight,
            Tag.MedicalAlerts,
            Tag.Allergies,
            Tag.PregnancyStatus,
            Tag.StudyInstanceUID,
            Tag.RequestingPhysician,
            Tag.RequestingService,
            Tag.RequestedProcedureDescription,
            Tag.AdmissionID,
            Tag.SpecialNeeds,
            Tag.CurrentPatientLocation,
            Tag.PatientState,
            Tag.RequestedProcedureID,
            Tag.RequestedProcedurePriority,
            Tag.PatientTransportArrangements,
            Tag.PlacerOrderNumberImagingServiceRequest,
            Tag.FillerOrderNumberImagingServiceRequest,
            Tag.ConfidentialityConstraintOnPatientDataDescription,
    };

    public static final int[] SPS_RETURN_KEYS = {
            Tag.Modality,
            Tag.RequestedContrastAgent,
            Tag.ScheduledStationAETitle,
            Tag.ScheduledProcedureStepStartDate,
            Tag.ScheduledProcedureStepStartTime,
            Tag.ScheduledPerformingPhysicianName,
            Tag.ScheduledProcedureStepDescription,
            Tag.ScheduledProcedureStepID,
            Tag.ScheduledStationName,
            Tag.ScheduledProcedureStepLocation,
            Tag.PreMedication,
            Tag.ScheduledProcedureStepStatus
    };

    public static final String[] IVRLE_TS = {
            UID.ImplicitVRLittleEndian};

    public static final String[] LE_TS = {
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian};

    private static final byte[] EXT_NEG_INFO_FUZZY_MATCHING = {1, 1, 1};

    private Executor executor;
    private NetworkApplicationEntity remoteAE;
    private NetworkConnection remoteConn;
    private Device device;
    private NetworkApplicationEntity ae;
    private NetworkConnection conn;
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
    private DicomObject keys;
    private DicomObject spsKeys;

    private boolean fuzzySemanticPersonNameMatching;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";

    private char[] keyStorePassword = SECRET;

    private char[] keyPassword;

    private String trustStoreURL = "resource:tls/mesa_certs.jks";

    private char[] trustStorePassword = SECRET;
}
