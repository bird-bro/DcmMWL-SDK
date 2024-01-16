package org.birdbro.dcmmwl;

import cn.hutool.core.util.StrUtil;
import org.birdbro.dcmmwl.dto.DcmMWLDto;
import org.birdbro.dcmmwl.dto.WorkListDto;
import org.birdbro.dcmmwl.uitl.DicomTagUtil;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
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
    public static void main(String[] args) {

        DcmMWLDto dcmMWLDto = new DcmMWLDto();
        /*
         * dcm wl scu query
         * RemoteCalled: SCP的AE   not null
         * RemoteAEHost: SCP的IP   not null
         * RemoteAEPort: SCP的端口  not null
         * LocalCalling: 本机AE     not null
         * LocalAEHostL: 本机IP     not null
         *
         * Date:检查日期 默认Null
         * Mod:检查模态  默认Null

         * */
        dcmMWLDto.setRemoteCalled("DEMO");
        dcmMWLDto.setRemoteAEHost("127.0.0.1");
        dcmMWLDto.setRemoteAEPort(3000);

        dcmMWLDto.setLocalCalling("test");
        dcmMWLDto.setLocalAEHost("127.0.0.1");
        dcmMWLDto.setMod("CT");

        dcmMWLDto.setDate("19960406");
        //dcmMWLDto.setTime("160700");

        try {
            //原始dicom标签数据返回
            List<DicomObject> result = DcmMWLUtil.getWorkListDicomObject(dcmMWLDto);

            System.out.println("Received data:" + result);

            //JSON，不同SCP结构可能不同需根据情况进行序列化
            // List<WorkListDto> result = DcmMWLUtil.getWorkListJson(dcmMWLDto);
        } catch (Exception e) {
            log.error("执行出错", e);
        }
    }

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
        /*if (StrUtil.isNotBlank(dcmMWLDto.getConnectTO()))
            dcmmwl.setConnectTimeout(DcmMWLServiceUtil.parseInt(dcmMWLDto.getConnectTO(),
                    "illegal argument of option -connectTO", 1, Integer.MAX_VALUE));
        if (StrUtil.isNotBlank(dcmMWLDto.getReaper()))
            dcmmwl.setAssociationReaperPeriod(DcmMWLServiceUtil.parseInt(dcmMWLDto.getReaper(),
                    "illegal argument of option -reaper", 1, Integer.MAX_VALUE));
        if (StrUtil.isNotBlank(dcmMWLDto.getRspTO()))
            dcmmwl.setDimseRspTimeout(DcmMWLServiceUtil.parseInt(dcmMWLDto.getRspTO(),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (StrUtil.isNotBlank(dcmMWLDto.getAcceptTO()))
            dcmmwl.setAcceptTimeout(DcmMWLServiceUtil.parseInt(dcmMWLDto.getAcceptTO(),
                    "illegal argument of option -acceptTO", 1, Integer.MAX_VALUE));
        if (StrUtil.isNotBlank(dcmMWLDto.getReleaseTO()))
            dcmmwl.setReleaseTimeout(DcmMWLServiceUtil.parseInt(dcmMWLDto.getReleaseTO(),
                    "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
        if (StrUtil.isNotBlank(dcmMWLDto.getSoclosedelay()))
            dcmmwl.setSocketCloseDelay(DcmMWLServiceUtil.parseInt(dcmMWLDto.getSoclosedelay(),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (StrUtil.isNotBlank(dcmMWLDto.getRcvpdulen()))
            dcmmwl.setMaxPDULengthReceive(DcmMWLServiceUtil.parseInt(dcmMWLDto.getRcvpdulen(),
                    "illegal argument of option -rcvpdulen", 1, 10000) * DcmMWLServiceUtil.KB);
        if (StrUtil.isNotBlank(dcmMWLDto.getSndpdulen()))
            dcmmwl.setMaxPDULengthSend(DcmMWLServiceUtil.parseInt(dcmMWLDto.getSndpdulen(),
                    "illegal argument of option -sndpdulen", 1, 10000) * DcmMWLServiceUtil.KB);
        if (StrUtil.isNotBlank(dcmMWLDto.getSosndbuf()))
            dcmmwl.setSendBufferSize(DcmMWLServiceUtil.parseInt(dcmMWLDto.getSosndbuf(),
                    "illegal argument of option -sosndbuf", 1, 10000) * DcmMWLServiceUtil.KB);
        if (StrUtil.isNotBlank(dcmMWLDto.getSorcvbuf()))
            dcmmwl.setReceiveBufferSize(DcmMWLServiceUtil.parseInt(dcmMWLDto.getSorcvbuf(),
                    "illegal argument of option -sorcvbuf", 1, 10000) * DcmMWLServiceUtil.KB);*/
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
/*        if (dcmMWLDto.getMatchingKeys() != null) {
            String[] matchingKeys = dcmMWLDto.getMatchingKeys();
            for (int i = 1; i < matchingKeys.length; i++, i++)
                dcmmwl.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]), matchingKeys[i]);
        }
        if (dcmMWLDto.getReturnKeys() != null) {
            String[] returnKeys = dcmMWLDto.getReturnKeys();
            for (int i = 0; i < returnKeys.length; i++)
                dcmmwl.addReturnKey(Tag.toTagPath(returnKeys[i]));
        }*/
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
//
//    private static CommandLine parse(String[] args) {
//        Options opts = new Options();
//
//        OptionBuilder.withArgName("name");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "set device name, use DCMMWL by default");
//        opts.addOption(OptionBuilder.create("device"));
//
//        OptionBuilder.withArgName("aet[@host]");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("set AET and local address of local " +
//                "Application Entity, use device name and pick up any valid " +
//                "local address to bind the socket by default");
//        opts.addOption(OptionBuilder.create("L"));
//
//        OptionBuilder.withArgName("username");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "enable User Identity Negotiation with specified username and "
//                        + " optional passcode");
//        opts.addOption(OptionBuilder.create("username"));
//
//        OptionBuilder.withArgName("passcode");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "optional passcode for User Identity Negotiation, "
//                        + "only effective with option -username");
//        opts.addOption(OptionBuilder.create("passcode"));
//
//        opts.addOption("uidnegrsp", false,
//                "request positive User Identity Negotation response, "
//                        + "only effective with option -username");
//
//        OptionBuilder.withArgName("NULL|3DES|AES");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "enable TLS connection without, 3DES or AES encryption");
//        opts.addOption(OptionBuilder.create("tls"));
//
//        OptionGroup tlsProtocol = new OptionGroup();
//        tlsProtocol.addOption(new Option("tls1",
//                "disable the use of SSLv3 and SSLv2 for TLS connections"));
//        tlsProtocol.addOption(new Option("ssl3",
//                "disable the use of TLSv1 and SSLv2 for TLS connections"));
//        tlsProtocol.addOption(new Option("no_tls1",
//                "disable the use of TLSv1 for TLS connections"));
//        tlsProtocol.addOption(new Option("no_ssl3",
//                "disable the use of SSLv3 for TLS connections"));
//        tlsProtocol.addOption(new Option("no_ssl2",
//                "disable the use of SSLv2 for TLS connections"));
//        opts.addOptionGroup(tlsProtocol);
//
//        opts.addOption("noclientauth", false,
//                "disable client authentification for TLS");
//
//        OptionBuilder.withArgName("file|url");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "file path or URL of P12 or JKS keystore, resource:tls/test_sys_1.p12 by default");
//        opts.addOption(OptionBuilder.create("keystore"));
//
//        OptionBuilder.withArgName("password");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "password for keystore file, 'secret' by default");
//        opts.addOption(OptionBuilder.create("keystorepw"));
//
//        OptionBuilder.withArgName("password");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "password for accessing the key in the keystore, keystore password by default");
//        opts.addOption(OptionBuilder.create("keypw"));
//
//        OptionBuilder.withArgName("file|url");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "file path or URL of JKS truststore, resource:tls/mesa_certs.jks by default");
//        opts.addOption(OptionBuilder.create("truststore"));
//
//        OptionBuilder.withArgName("password");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription(
//                "password for truststore file, 'secret' by default");
//        opts.addOption(OptionBuilder.create("truststorepw"));
//
//        opts.addOption("ivrle", false,
//                "offer only Implicit VR Little Endian Transfer Syntax.");
//        opts.addOption("fuzzy", false,
//                "negotiate support of fuzzy semantic person name attribute matching.");
//        opts.addOption("pdv1", false,
//                "send only one PDV in one P-Data-TF PDU, pack command and data " +
//                        "PDV in one P-DATA-TF PDU by default.");
//        opts.addOption("tcpdelay", false,
//                "set TCP_NODELAY socket option to false, true by default");
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("timeout in ms for TCP connect, no timeout by default");
//        opts.addOption(OptionBuilder.create("connectTO"));
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("delay in ms for Socket close after sending A-ABORT, 50ms by default");
//        opts.addOption(OptionBuilder.create("soclosedelay"));
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("period in ms to check for outstanding DIMSE-RSP, 10s by default");
//        opts.addOption(OptionBuilder.create("reaper"));
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("timeout in ms for receiving DIMSE-RSP, 10s by default");
//        opts.addOption(OptionBuilder.create("rspTO"));
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
//        opts.addOption(OptionBuilder.create("acceptTO"));
//
//        OptionBuilder.withArgName("ms");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("timeout in ms for receiving A-RELEASE-RP, 5s by default");
//        opts.addOption(OptionBuilder.create("releaseTO"));
//
//        OptionBuilder.withArgName("KB");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
//        opts.addOption(OptionBuilder.create("rcvpdulen"));
//
//        OptionBuilder.withArgName("KB");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
//        opts.addOption(OptionBuilder.create("sndpdulen"));
//
//        OptionBuilder.withArgName("KB");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("set SO_RCVBUF socket option to specified value in KB");
//        opts.addOption(OptionBuilder.create("sorcvbuf"));
//
//        OptionBuilder.withArgName("KB");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("set SO_SNDBUF socket option to specified value in KB");
//        opts.addOption(OptionBuilder.create("sosndbuf"));
//
//        OptionBuilder.withArgName("[seq/]attr=value");
//        OptionBuilder.hasArgs();
//        OptionBuilder.withValueSeparator('=');
//        OptionBuilder.withDescription("specify matching key. attr can be " +
//                "specified by name or tag value (in hex), e.g. PatientName " +
//                "or 00100010. Attributes in nested Datasets can " +
//                "be specified by preceding the name/tag value of " +
//                "the sequence attribute, e.g. 00400100/00400009 " +
//                "for Scheduled Procedure Step ID in the Scheduled " +
//                "Procedure Step Sequence.");
//        opts.addOption(OptionBuilder.create("q"));
//
//        OptionBuilder.withArgName("date");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("specify matching SPS start date " +
//                "(range). Shortcut for -q00400100/00400002=<date>.");
//        opts.addOption(OptionBuilder.create("date"));
//
//        OptionBuilder.withArgName("time");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("specify matching SPS start time " +
//                "(range). Shortcut for -q00400100/00400003=<time>.");
//        opts.addOption(OptionBuilder.create("time"));
//
//        OptionBuilder.withArgName("modality");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("specify matching Modality. Shortcut " +
//                "for -q00400100/00080060=<modality>.");
//        opts.addOption(OptionBuilder.create("mod"));
//
//        OptionBuilder.withArgName("aet");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("specify matching Scheduled Station AE " +
//                "title. Shortcut for -q00400100/00400001=<aet>.");
//        opts.addOption(OptionBuilder.create("aet"));
//
//        OptionBuilder.withArgName("attr");
//        OptionBuilder.hasArgs();
//        OptionBuilder.withDescription("specify additional return key. attr can " +
//                "be specified by name or tag value (in hex).");
//        opts.addOption(OptionBuilder.create("r"));
//
//        OptionBuilder.withArgName("num");
//        OptionBuilder.hasArg();
//        OptionBuilder.withDescription("cancel query after receive of specified " +
//                "number of responses, no cancel by default");
//        opts.addOption(OptionBuilder.create("C"));
//
//
//        opts.addOption("lowprior", false,
//                "LOW priority of the C-FIND operation, MEDIUM by default");
//        opts.addOption("highprior", false,
//                "HIGH priority of the C-FIND operation, MEDIUM by default");
//        opts.addOption("h", "help", false, "print this message");
//        opts.addOption("V", "version", false,
//                "print the version information and exit");
//        CommandLine cl = null;
//        try {
//            cl = new GnuParser().parse(opts, args);
//        } catch (ParseException e) {
//            exit("dcmmwl: " + e.getMessage());
//            throw new RuntimeException("unreachable");
//        }
//        if (cl.hasOption('V')) {
//            Package p = DcmMWLServiceImpl.class.getPackage();
//            System.out.println("dcmqr v" + p.getImplementationVersion());
//            System.exit(0);
//        }
//        if (cl.hasOption('h') || cl.getArgList().size() != 1) {
//            HelpFormatter formatter = new HelpFormatter();
//            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
//            System.exit(0);
//        }
//
//        return cl;
//    }

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