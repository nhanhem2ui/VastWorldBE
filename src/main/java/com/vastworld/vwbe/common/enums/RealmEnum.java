package com.vastworld.vwbe.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RealmEnum {
    LUYEN_KHI(1),
    TRUC_CO(2),
    KIM_DAN(3),
    NGUYEN_ANH(4),
    HOA_THAN(5),
    PHAN_HU(6),
    HOP_THE(7),
    DAI_THUA(8),
    DO_KIEP(9),
    TIEN_NHAN(10),
    CHAN_TIEN(11),
    KIM_TIEN(12),
    THAI_AT(13),
    DAI_LA(14),
    HON_DON(15),
    CHI_TON(16),
    VO_CUC(17),
    THIEN_DAO(18);

    private final Integer value;

    public static RealmEnum fromValue(Integer value) {
        for (var realm : values()) {
            if (realm.value.equals(value)) {
                return realm;
            }
        }
        throw new IllegalArgumentException("Unknown realm value: " + value);
    }

    public static RealmEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Realm name cannot be null or empty");
        }
        String normalized = value
                .trim()
                .replace(' ', '_')
                .toUpperCase();

        for (RealmEnum realm : values()) {
            if (realm.name().equals(normalized)) {
                return realm;
            }
        }
        throw new IllegalArgumentException("Unknown realm: " + value);
    }
}
