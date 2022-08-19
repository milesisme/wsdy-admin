package com.wsdy.saasops.api.utils;

/***
 * 游戏平台
 * key 为平台的code 应用于ElasticSearch的index
 */
public enum PlatFromEnum {

    ENUM_PT("PT", "PT"),
    ENUM_NT("NT", "NT"),
    ENUM_BBIN("BBIN", "BBIN"),
    ENUM_AGIN("AGIN", "AGIN"),
    ENUM_PT2("PT2", "PT2"),
    ENUM_PNG("PNG", "PNG"),
    ENUM_IBC("IBC", "IBC"),
    ENUM_T188("T188", "T188"),
    ENUM_EG("EG", "EG"),
    ENUM_TS("TS", "TS"),
    ENUM_OPUSCA("OPUSCA", "OPUSCA"),
    ENUM_OPUSSB("OPUSSB", "OPUSSB"),
    ENUM_PB("PB", "PB"),
    ENUM_MG("MG", "MG"),
    ENUM_CQ9("CQ9", "CQ9"),
    ENUM_GNS("GNS", "GNS"),
    ENUM_PG("PG", "PG"),
    ENUM_FUN("FUN", "FUN"),
    ENUM_GG("GG", "GG"),
    ENUM_MG2("MG2", "MG2"),
    ENUM_VR("VR", "VR"),
    ENUM_BG("BG", "BG"),
    ENUM_ELG("ELG", "ELG"),
    ENUM_AVIA("AVIA", "AVIA"),
    ENUM_N2("N2", "N2"),
    ENUM_TTG("TTG", "TTG"),
    ENUM_GD("GD", "GD"),
    ENUM_KG("KG", "KG");

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
