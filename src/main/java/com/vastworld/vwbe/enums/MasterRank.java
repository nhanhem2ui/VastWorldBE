package com.vastworld.vwbe.enums;

public enum MasterRank {
    OUTER_DISCIPLE("Đệ tử ngoại môn", 1),
    INNER_DISCIPLE("Đệ tử nội môn", 2),
    ELDER("Trưởng lão", 3),
    SECT_MASTER("Tông chủ", 4);

    private final String defaultName;
    private final int baseOrder;

    MasterRank(String defaultName, int baseOrder) {
        this.defaultName = defaultName;
        this.baseOrder = baseOrder;
    }

    public String getCode() {
        return this.name();
    }

    public String getDefaultName() {
        return defaultName;
    }

    public int getBaseOrder() {
        return baseOrder;
    }

    public static MasterRank fromCode(String code) {
        if (code == null) return null;
        return MasterRank.valueOf(code.trim().toUpperCase());
    }
}