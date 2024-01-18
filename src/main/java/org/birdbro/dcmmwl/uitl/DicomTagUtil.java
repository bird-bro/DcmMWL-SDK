package org.birdbro.dcmmwl.uitl;

import cn.hutool.core.util.ArrayUtil;
import org.birdbro.dcmmwl.dto.SequenceDto;
import org.birdbro.dcmmwl.dto.WorkListDto;
import org.dcm4che2.data.DicomObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author birdbro
 */
public class DicomTagUtil {

    public static List<WorkListDto> getWorkListDto(List<DicomObject> dicomObjectList) {

        List<WorkListDto> workListDtoList = new ArrayList<>();
        if (dicomObjectList != null && !dicomObjectList.isEmpty()) {
            for (DicomObject dicomObject : dicomObjectList) {
                String[] strArray = dicomObject.toString().split("\\n\\(");
                WorkListDto workListDto = new WorkListDto();
                for (String str : strArray) {
                    String regexStr = regexStr(str);
                    if (str.contains("0008,0005)")) {
                        workListDto.setCharSet(regexStr);
                        continue;
                    }

                    if (str.contains("0008,0050)")) {
                        workListDto.setAccessionNumber(regexStr);
                        continue;
                    }
                    if (str.contains("0008,0090)")) {
                        workListDto.setPhysicianName(regexStr);
                        continue;
                    }
                    if (str.contains("0010,0010)")) {
                        workListDto.setPatientName(regexStr);
                        continue;
                    }
                    if (str.contains("0010,0030)")) {
                        workListDto.setBirthDate(regexStr);
                        continue;
                    }
                    if (str.contains("0010,0040)")) {
                        workListDto.setPatientSex(regexStr);
                        continue;
                    }
                    if (str.contains("0010,1030)")) {
                        workListDto.setPatientWeight(regexStr);
                        continue;
                    }
                    if (str.contains("0010,2000)")) {
                        workListDto.setMedicalAlerts(regexStr);
                        continue;
                    }
                    if (str.contains("0010,2110)")) {
                        workListDto.setAllergies(regexStr);
                        continue;
                    }
                    if (str.contains("0010,21C0)")) {
                        workListDto.setPregnancyStatus(regexStr);
                        continue;
                    }
                    if (str.contains("0020,000D)")) {
                        workListDto.setInstanceUID(regexStr);
                        continue;
                    }
                    if (str.contains("0032,1032)")) {
                        workListDto.setRequestingPhysician(regexStr);
                        continue;
                    }
                    if (str.contains("0032,1033)")) {
                        workListDto.setRequestingService(regexStr);
                        continue;
                    }
                    if (str.contains("0032,1060)")) {
                        workListDto.setRequestedProcedureDescription(regexStr);
                        continue;
                    }
                    if (str.contains("0038,0010)")) {
                        workListDto.setAdmissionID(regexStr);
                        continue;
                    }
                    if (str.contains("0038,0050)")) {
                        workListDto.setSpecialNeeds(regexStr);
                        continue;
                    }
                    if (str.contains("0038,0300)")) {
                        workListDto.setCurrentPatientLocation(regexStr);
                        continue;
                    }
                    if (str.contains("0038,0500)")) {
                        workListDto.setPatientState(regexStr);
                        continue;
                    }
                    if (str.contains("0040,0100)")) {
                        List<SequenceDto> sequenceDtoList = new ArrayList<>();
                        String[] seqArray = str.split(":");
                        if (ArrayUtil.isNotEmpty(seqArray)) {
                            for (String seqStr : seqArray) {
                                String[] seqInfoArray = seqStr.split("\\n>\\(");
                                SequenceDto sequenceDto = new SequenceDto();
                                if (ArrayUtil.isNotEmpty(seqInfoArray) && !seqInfoArray[0].contains(">ITEM")) {
                                    for (String seqInfo : seqInfoArray) {
                                        String seqRegexStr = regexStr(seqInfo);
                                        if (seqInfo.contains("0008,0060)")) {
                                            sequenceDto.setModality(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0032,1070)")) {
                                            sequenceDto.setContrastAgent(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0001)")) {
                                            sequenceDto.setAeTitle(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0002)")) {
                                            sequenceDto.setStepStartDate(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0003)")) {
                                            sequenceDto.setStepStartTime(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0006)")) {
                                            sequenceDto.setPhysicianName(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0007)")) {
                                            sequenceDto.setDescription(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0009)")) {
                                            sequenceDto.setStepID(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0010)")) {
                                            sequenceDto.setStationName(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0011)")) {
                                            sequenceDto.setStepLocation(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0012)")) {
                                            sequenceDto.setPreMedication(seqRegexStr);
                                            continue;
                                        }
                                        if (seqInfo.contains("0040,0020)")) {
                                            sequenceDto.setStepStatus(seqRegexStr);
                                        }
                                    }
                                    sequenceDtoList.add(sequenceDto);
                                }
                            }
                        }
                        workListDto.setSequenceDtoList(sequenceDtoList);
                        continue;
                    }
                    if (str.contains("0040,1001)")) {
                        workListDto.setRequestedProcedureID(regexStr);
                        continue;
                    }
                    if (str.contains("0040,1003)")) {
                        workListDto.setRequestedProcedurePriority(regexStr);
                        continue;
                    }
                    if (str.contains("0040,1004)")) {
                        workListDto.setPatientTransportArrangements(regexStr);
                        continue;
                    }
                    if (str.contains("0040,2016)")) {
                        workListDto.setPlacerOrderNumber(regexStr);
                        continue;
                    }
                    if (str.contains("0040,2017)")) {
                        workListDto.setFillerOrderNumber(regexStr);
                        continue;
                    }
                    if (str.contains("0040,3001)")) {
                        workListDto.setConfidentialityConstraint(regexStr);
                    }
                }
                workListDtoList.add(workListDto);
            }
        }

        return workListDtoList;
    }

    public static String regexStr(String str) {
        //正则表达式
        String regex = "\\[(.*?)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        String match = null;
        while (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
}
