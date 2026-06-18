package com.vastworld.vwbe.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Realm {
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
}
