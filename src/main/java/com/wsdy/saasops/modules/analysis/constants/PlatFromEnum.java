package com.wsdy.saasops.modules.analysis.constants;

/***
 * 游戏平台
 */
public enum PlatFromEnum {

    Enum_PT("PT","PT"),
    Enum_NT("NT","NT"),
    Enum_BBIN("BBIN","BBIN"),
    Enum_AGIN("AGIN","AGIN"),
    Enum_PT2("PT2","PT2"),
    Enum_PNG("PNG","PNG"),
    Enum_IBC("IBC","IBC"),
    Enum_T188("T188","T188"),
    Enum_EG("EG","EG"),
    Enum_OPUSCA("OPUSCA","OPUSCA"),
    Enum_OPUSSB("OPUSSB","OPUSSB"),
    Enum_MG("MG","MG");

    private String key;
    private String value;

    PlatFromEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    PlatFromEnum() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
