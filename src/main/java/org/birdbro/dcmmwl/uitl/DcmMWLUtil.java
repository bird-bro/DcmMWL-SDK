package org.birdbro.dcmmwl.uitl;

import cn.hutool.core.util.StrUtil;
import org.birdbro.dcmmwl.dto.DcmMWLDto;
import org.birdbro.dcmmwl.dto.WorkListDto;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.*;
import org.dcm4che2.net.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class DcmMWLUtil {




    private static final int KB = 1024;
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

    private static String[] TLS1 = {"TLSv1"};

    private static String[] SSL3 = {"SSLv3"};

    private static String[] NO_TLS1 = {"SSLv3", "SSLv2Hello"};

    private static String[] NO_SSL2 = {"TLSv1", "SSLv3"};

    private static String[] NO_SSL3 = {"TLSv1", "SSLv2Hello"};

    private static char[] SECRET = {'s', 'e', 'c', 'r', 'e', 't'};

    private static final int[] RETURN_KEYS = {
            Tag.SpecificCharacterSet,
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

    private static final int[] SPS_RETURN_KEYS = {
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

    private static final String[] IVRLE_TS = {
            UID.ImplicitVRLittleEndian};

    private static final String[] LE_TS = {
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian};

    private static final byte[] EXT_NEG_INFO_FUZZY_MATCHING = {1, 1, 1};

    private final Executor executor;
    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private final NetworkConnection remoteConn = new NetworkConnection();
    private final Device device;
    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private final NetworkConnection conn = new NetworkConnection();
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
    private final DicomObject keys = new BasicDicomObject();
    private final DicomObject spsKeys = new BasicDicomObject();

    private boolean fuzzySemanticPersonNameMatching;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";

    private char[] keyStorePassword = SECRET;

    private char[] keyPassword;

    private String trustStoreURL = "resource:tls/mesa_certs.jks";

    private char[] trustStorePassword = SECRET;


    public DcmMWLUtil(String name) {
        device = new Device(name);
        executor = new NewThreadExecutor(name);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[]{remoteConn});

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle(name);

        for (int i = 0; i < RETURN_KEYS.length; i++) {
            keys.putNull(RETURN_KEYS[i], null);
        }


        keys.putNestedDicomObject(Tag.RequestedProcedureCodeSequence,
                new BasicDicomObject());
        keys.putNestedDicomObject(Tag.ScheduledProcedureStepSequence, spsKeys);
        for (int i = 0; i < SPS_RETURN_KEYS.length; i++) {
            spsKeys.putNull(SPS_RETURN_KEYS[i], null);
        }
        spsKeys.putNestedDicomObject(Tag.ScheduledProtocolCodeSequence,
                new BasicDicomObject());
    }

    public final void setSpecificCharacterSet(String charSet) {
        keys.putString(Tag.SpecificCharacterSet, VR.CS, charSet);
    }
    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setRemoteHost(String hostname) {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port) {
        remoteConn.setPort(port);
    }

    public final void setTlsProtocol(String[] tlsProtocol) {
        conn.setTlsProtocol(tlsProtocol);
    }

    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
    }

    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        conn.setTlsNeedClientAuth(needClientAuth);
    }

    public final void setKeyStoreURL(String url) {
        keyStoreURL = url;
    }

    public final void setKeyStorePassword(String pw) {
        keyStorePassword = pw.toCharArray();
    }

    public final void setKeyPassword(String pw) {
        keyPassword = pw.toCharArray();
    }

    public final void setTrustStorePassword(String pw) {
        trustStorePassword = pw.toCharArray();
    }

    public final void setTrustStoreURL(String url) {
        trustStoreURL = url;
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout) {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout) {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength) {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        conn.setSendBufferSize(bufferSize);
    }

    public final void setCancelAfter(int limit) {
        this.cancelAfter = limit;
    }


    public void addMatchingKey(int[] tagPath, String value) {
        keys.putString(tagPath, null, value);
    }

    public void addReturnKey(int[] tagPath) {
        keys.putNull(tagPath, null);
    }

    public void addSpsMatchingKey(int tag, String value) {
        spsKeys.putString(tag, null, value);
    }

    public void setFuzzySemanticPersonNameMatching(boolean b) {
        this.fuzzySemanticPersonNameMatching = b;
    }

    public void setTransferSyntax(String[] ts) {
        TransferCapability tc = new TransferCapability(
                UID.ModalityWorklistInformationModelFIND, ts,
                TransferCapability.SCU);
        if (fuzzySemanticPersonNameMatching) {
            tc.setExtInfo(EXT_NEG_INFO_FUZZY_MATCHING);
        }
        ae.setTransferCapability(new TransferCapability[]{tc});
    }

    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    public List<DicomObject> query() throws IOException, InterruptedException {
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(
                UID.ModalityWorklistInformationModelFIND);
        if (tc == null) {
            throw new NoPresentationContextException(
                    "Modality Worklist not supported by "
                            + remoteAE.getAETitle());
        }
        log.info("Send Query Request:");
        log.info(keys.toString());
        DimseRSP rsp = assoc.cfind(UID.ModalityWorklistInformationModelFIND,
                priority, keys, tc.getTransferSyntax()[0], cancelAfter);
        List<DicomObject> result = new ArrayList<>();
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                result.add(data);
                log.info("\nReceived Query Response #" + result.size() + ":");
                log.info(data.toString());
            }
        }
        return result;
    }

    public static List<WorkListDto> getWorkListJson(DcmMWLDto dcmMWLDto) throws Exception {

        // dicomObjectList------->  result
        return DicomTagUtil.getWorkListDto(getWorkListDicomObject(dcmMWLDto));
    }


    @SuppressWarnings("unchecked")
    public static List<DicomObject> getWorkListDicomObject(DcmMWLDto dcmMWLDto) throws Exception {
        DcmMWLUtil dcmmwl = new DcmMWLUtil(StrUtil.isBlank(dcmMWLDto.getDevice()) ? "DCMMWL" : dcmMWLDto.getDevice());
        if (StrUtil.isBlank(dcmMWLDto.getRemoteCalled())) {
            throw new Exception("RemoteCalled不能为空");
        }
        dcmmwl.setCalledAET(dcmMWLDto.getRemoteCalled());
        if (StrUtil.isBlank(dcmMWLDto.getRemoteAEHost())) {
            dcmmwl.setRemoteHost("127.0.0.1");
            dcmmwl.setRemotePort(104);
        } else {
            dcmmwl.setRemoteHost(dcmMWLDto.getRemoteAEHost());
            dcmmwl.setRemotePort(dcmMWLDto.getRemoteAEPort());
        }
        dcmmwl.setCalling(dcmMWLDto.getLocalCalling());
        if (StrUtil.isNotBlank(dcmMWLDto.getLocalAEHost())) {
            dcmmwl.setLocalHost(dcmMWLDto.getLocalAEHost());
        }
        if (StrUtil.isNotBlank(dcmMWLDto.getUsername())) {
            String username = dcmMWLDto.getUsername();
            UserIdentity userId;
            if (StrUtil.isNotBlank(dcmMWLDto.getPasscode())) {
                String passcode = dcmMWLDto.getPasscode();
                userId = new UserIdentity.UsernamePasscode(username,
                        passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }
            userId.setPositiveResponseRequested(dcmMWLDto.getUidnegrsp());
            dcmmwl.setUserIdentity(userId);
        }

        dcmmwl.setPackPDV(dcmMWLDto.getPdv1());
        dcmmwl.setTcpNoDelay(dcmMWLDto.getTcpnodelay());
//        if (StrUtil.isNotBlank(dcmMWLDto.getCancelAfter()))
//            dcmmwl.setCancelAfter(DcmMWLServiceUtil.parseInt(dcmMWLDto.getCancelAfter(),
//                    "illegal argument of option -C", 1, Integer.MAX_VALUE));
        if (dcmMWLDto.getPriority() != null) {
            dcmmwl.setPriority(dcmMWLDto.getPriority());
        }
        if (dcmMWLDto.getFuzzy() != null) {
            dcmmwl.setFuzzySemanticPersonNameMatching(dcmMWLDto.getFuzzy());
        }
      if (dcmMWLDto.getMatchingKeys() != null) {
            String[] matchingKeys = dcmMWLDto.getMatchingKeys();
            for (int i = 1; i < matchingKeys.length; i++, i++) {
                dcmmwl.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]), matchingKeys[i]);
            }
        }
//        if (dcmMWLDto.getReturnKeys() != null) {
//            String[] returnKeys = dcmMWLDto.getReturnKeys();
//            for (int i = 0; i < returnKeys.length; i++) {
//                dcmmwl.addReturnKey(Tag.toTagPath(returnKeys[i]));
//            }
//        }
        if (StrUtil.isNotBlank(dcmMWLDto.getDate())) {
            dcmmwl.addSpsMatchingKey(Tag.ScheduledProcedureStepStartDate,
                    dcmMWLDto.getDate());
        }
        if (StrUtil.isNotBlank(dcmMWLDto.getTime())) {
            dcmmwl.addSpsMatchingKey(Tag.ScheduledProcedureStepStartTime,
                    dcmMWLDto.getTime());
        }
        if (StrUtil.isNotBlank(dcmMWLDto.getMod())) {
            dcmmwl.addSpsMatchingKey(Tag.Modality, dcmMWLDto.getMod());
        }
        if (StrUtil.isNotBlank(dcmMWLDto.getAet())) {
            dcmmwl.addSpsMatchingKey(Tag.ScheduledStationAETitle,
                    dcmMWLDto.getAet());
        }
        if (StrUtil.isNotBlank(dcmMWLDto.getCharSet())) {
            dcmmwl.setSpecificCharacterSet(dcmMWLDto.getCharSet());
        }






        dcmmwl.setTransferSyntax(StrUtil.isNotBlank(dcmMWLDto.getIvrle()) ? IVRLE_TS : LE_TS);

        if (StrUtil.isNotBlank(dcmMWLDto.getCipher())) {
            String cipher = dcmMWLDto.getCipher();
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmmwl.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmmwl.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmmwl.setTlsAES_128_CBC();
            } else {
                log.error("参数错误");
                return null;
            }
            if (dcmMWLDto.getTls1() != null && dcmMWLDto.getTls1()) {
                dcmmwl.setTlsProtocol(TLS1);
            } else if (dcmMWLDto.getSsl3() != null && dcmMWLDto.getSsl3()) {
                dcmmwl.setTlsProtocol(SSL3);
            } else if (dcmMWLDto.getNoTls1() != null && dcmMWLDto.getNoTls1()) {
                dcmmwl.setTlsProtocol(NO_TLS1);
            } else if (dcmMWLDto.getNoSsl3() != null && dcmMWLDto.getNoSsl3()) {
                dcmmwl.setTlsProtocol(NO_SSL3);
            } else if (dcmMWLDto.getNoSsl2() != null && dcmMWLDto.getNoSsl2()) {
                dcmmwl.setTlsProtocol(NO_SSL2);
            }
            dcmmwl.setTlsNeedClientAuth(dcmMWLDto.getClientauth() != null && dcmMWLDto.getClientauth());
            if (StrUtil.isNotBlank(dcmMWLDto.getKeystore())) {
                dcmmwl.setKeyStoreURL(dcmMWLDto.getKeystore());
            }
            if (StrUtil.isNotBlank(dcmMWLDto.getKeystorepw())) {
                dcmmwl.setKeyStorePassword(dcmMWLDto.getKeystorepw());
            }
            if (StrUtil.isNotBlank(dcmMWLDto.getKeypw())) {
                dcmmwl.setKeyPassword(dcmMWLDto.getKeypw());

            }
            if (StrUtil.isNotBlank(dcmMWLDto.getTruststore())) {
                dcmmwl.setTrustStoreURL(dcmMWLDto.getTruststore());
            }
            if (StrUtil.isNotBlank(dcmMWLDto.getTruststorepw())) {
                dcmmwl.setTrustStorePassword(dcmMWLDto.getTruststorepw());
            }
            long t1 = System.currentTimeMillis();
            try {
                dcmmwl.initTLS();
            } catch (Exception e) {
                log.error("ERROR: Failed to initialize TLS context:", e);
                return null;
            }
            long t2 = System.currentTimeMillis();
            log.info("Initialize TLS context in " + ((t2 - t1) / 1000F) + "s");
        }

        long t1 = System.currentTimeMillis();
        try {
            dcmmwl.open();
        } catch (Exception e) {
            log.error("ERROR: Failed to establish association:", e);
            throw new Exception("连接服务器异常");
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + dcmMWLDto.getRemoteAEHost() + " in "
                + ((t2 - t1) / 1000F) + "s");
        List<DicomObject> dicomObjectList;
        try {
            dicomObjectList = dcmmwl.query();
            long t3 = System.currentTimeMillis();
            log.info("========Received ========,{}", dicomObjectList);
            log.info("Received " + dicomObjectList.size() + " matching entries in " + ((t3 - t2) / 1000F) + "s");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("query error:", e);
            throw new Exception("连接服务器异常");
            //e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            log.error("close error:", e);
            throw new Exception("关闭服务器异常");
        }
        try {
            dcmmwl.close();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("Released connection to " + dcmMWLDto.getRemoteCalled());
        return dicomObjectList;
    }


    private static int toPort(String port) {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max) {
                return i;
            }
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    private static String[] split(String s, char delim) {
        String[] s2 = {s, null};
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmmwl -h' for more information.");
        System.exit(1);
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }

    private static KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmMWLUtil.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                ? "PKCS12" : "JKS";
    }
}