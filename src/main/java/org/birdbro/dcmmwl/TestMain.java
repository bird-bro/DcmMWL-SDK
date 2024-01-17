package org.birdbro.dcmmwl;

import lombok.extern.slf4j.Slf4j;
import org.birdbro.dcmmwl.dto.DcmMWLDto;
import org.birdbro.dcmmwl.dto.DicomTagEnum;
import org.birdbro.dcmmwl.dto.WorkListDto;
import org.birdbro.dcmmwl.uitl.DcmMWLUtil;

import java.util.List;

/**
 * @author birdbro
 * @date 14:58 2024-01-17
 **/
@Slf4j
public class TestMain {

    public static void main(String[] args) {

        DcmMWLDto dcmMWLDto = new DcmMWLDto();
        /*
         * dcm wl scu query
         * RemoteCalled: SCP的AE   not null
         * RemoteAEHost: SCP的IP   not null
         * RemoteAEPort: SCP的端口  not null
         * LocalCalling: 本机AE     not null
         * LocalAEHostL: 本机IP     not null
         * Date:检查日期 默认Null
         * Mod:检查模态  默认Null

         * */
        dcmMWLDto.setRemoteCalled("DEMO");
        dcmMWLDto.setRemoteAEHost("127.0.0.1");
        dcmMWLDto.setRemoteAEPort(3000);

        dcmMWLDto.setLocalCalling("test");
        dcmMWLDto.setLocalAEHost("127.0.0.1");
        dcmMWLDto.setMod("CT");
        //dcmMWLDto.setDate("19951015");


        //自定义查询入参，根据业务需求构造查询入参数组 {dicomTag-1，参数-1，dicomTag-2，参数-2，dicomTag-N，参数-N}
        String[] matching ={DicomTagEnum.AccessionNumber.getTag(),"00009"} ;
        dcmMWLDto.setMatchingKeys(matching);


        try {
            //原始dicom标签数据返回
            // List<DicomObject> result = DcmMWLUtil.getWorkListDicomObject(dcmMWLDto);

            //JSON，不同SCP结构可能不同需根据情况进行序列化，该方法将常见Tag进行序列化
            List<WorkListDto> result = DcmMWLUtil.getWorkListJson(dcmMWLDto);
            System.out.println("Received data:" + result);
        } catch (Exception e) {
            log.error("执行出错", e);
        }
    }
}
