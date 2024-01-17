package org.birdbro.dcmmwl.dto;


import lombok.Data;

@Data
public class DcmMWLDto {

  /*  @ApiModelProperty(value = "设备")*/
    private String device;

    /*@ApiModelProperty(value = "远程主机用户")*/
    private String remoteCalled;

    /*@ApiModelProperty(value = "远程主机IP")*/
    private String remoteAEHost;

   /* @ApiModelProperty(value = "远程主机端口")*/
    private int remoteAEPort;

    /*@ApiModelProperty(value = "本地主机AE")*/
    private String localCalling;

    /*@ApiModelProperty(value = "本地主机HOST")*/
    private String localAEHost;

    /*@ApiModelProperty(value = "用户名")*/
    private String username;

    /*@ApiModelProperty(value = "密码")*/
    private String passcode;

    /*@ApiModelProperty(value = "uidnegrsp")*/
    private Boolean uidnegrsp = false;

    /*@ApiModelProperty(value = "连接超时时间")*/
    private String connectTO;

    /*@ApiModelProperty(value = "reaper")*/
    private String reaper;

    /*@ApiModelProperty(value = "rspTO")*/
    private String rspTO;

    /*@ApiModelProperty(value = "acceptTO")*/
    private String acceptTO;

    /*@ApiModelProperty(value = "releaseTO")*/
    private String releaseTO;

    /*@ApiModelProperty(value = "soclosedelay")*/
    private String soclosedelay;

    /*@ApiModelProperty(value = "rcvpdulen")*/
    private String rcvpdulen;

    /*@ApiModelProperty(value = "sndpdulen")*/
    private String sndpdulen;

    /*@ApiModelProperty(value = "sosndbuf")*/
    private String sosndbuf;

    /*@ApiModelProperty(value = "sorcvbuf")*/
    private String sorcvbuf;

    /*@ApiModelProperty(value = "pdv1")*/
    private Boolean pdv1 = false;

    /*@ApiModelProperty(value = "tcpnodelay")*/
    private Boolean tcpnodelay = false;

    /*@ApiModelProperty(value = "CancelAfter")*/
    private String cancelAfter;

    /*@ApiModelProperty(value = "Priority,low=2,high=1")*/
    private Integer priority;

    /*@ApiModelProperty(value = "fuzzy")*/
    private Boolean fuzzy;

    /*@ApiModelProperty(value = "matchingKeys")*/
    private String[] matchingKeys;

    /*@ApiModelProperty(value = "returnKeys")*/
    private String[] returnKeys;

    /*@ApiModelProperty(value = "date")*/
    private String date;

    /*@ApiModelProperty(value = "time")*/
    private String time;

    /*@ApiModelProperty(value = "mod")*/
    private String mod;

    /*@ApiModelProperty(value = "aet")*/
    private String aet;

    /*@ApiModelProperty(value = "ivrle")*/
    private String ivrle;

    /*@ApiModelProperty(value = "cipher,NULL/3DES/AES")*/
    private String cipher;

    /*@ApiModelProperty(value = "tls1，需要填true")*/
    private Boolean tls1;

    /*@ApiModelProperty(value = "ssl3，需要填true")*/
    private Boolean ssl3;

    /*@ApiModelProperty(value = "no_tls1，需要填true")*/
    private Boolean noTls1;

    /*@ApiModelProperty(value = "no_ssl3，需要填true")*/
    private Boolean noSsl3;

    /*@ApiModelProperty(value = "no_ssl2，需要填true")*/
    private Boolean noSsl2;

    /*@ApiModelProperty(value = "clientauth")*/
    private Boolean clientauth;

    /*@ApiModelProperty(value = "keystore")*/
    private String keystore;

    /*@ApiModelProperty(value = "keystorepw")*/
    private String keystorepw;

    /*@ApiModelProperty(value = "keypw")*/
    private String keypw;

    /*@ApiModelProperty(value = "truststore")*/
    private String truststore;

    /*@ApiModelProperty(value = "truststorepw")*/
    private String truststorepw;


}
