https://xie.infoq.cn/article/fa2d88404380385c4814d62bf
1、SCP服务
   启动仪器设备或RIS/PACS系统的SCP服务，若开发环境中没有可使用SDK提供的SCP模拟器。
模拟器启动方法：
2、在文件夹\SDK\MWL-SCP_模拟器中双击【start.bat】出现如下界面则启动成功。默认IP为当前主机IP，端口：3000 AE：DEMO
3、使用SDK调用SCP query 获取检查信息
调用方法如下：
4、接口成功返回
方法一：返回原worklist object
List<DicomObject> result = DcmMWLUtil.getWorkListDicomObject(dcmMWLDto);
注意：不同的SCP返回的机构可能不一致，在进行JSON序列化是可能会有异常，需配合实际项目情况序列化。
方法二：返回Json
//JSON，不同SCP结构可能不同需根据情况进行序列化，该方法将常见Tag进行序列化List<WorkListDto> result = DcmMWLUtil.getWorkListJson(dcmMWLDto);


