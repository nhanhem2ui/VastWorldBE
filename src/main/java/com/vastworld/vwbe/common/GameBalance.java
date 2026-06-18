package com.vastworld.vwbe.common;

import java.time.Duration;
import java.time.LocalDateTime;

public final class GameBalance {

    private GameBalance() {}

    /**
     * TIME
     * 1 Real Hour = 1 Game Year
     */
    public static final int GAME_YEARS_PER_REAL_HOUR = 1;

    public static final long BASE_BREAKTHROUGH_CP = 100L;
    public static final double BREAKTHROUGH_GROWTH_RATE = 1.35D;

    /*
     * SPIRIT ROOT MULTIPLIERS
     */
    public static final double FIVE_SPIRIT_ROOT = 0.5D;
    public static final double FOUR_SPIRIT_ROOT = 0.8D;
    public static final double THREE_SPIRIT_ROOT = 1.0D;
    public static final double TWO_SPIRIT_ROOT = 2.0D;
    public static final double VARIANT_SPIRIT_ROOT = 4.0D;
    public static final double HEAVENLY_SPIRIT_ROOT = 8.0D;


    public static final int STAGES_PER_REALM = 10;

    /**
     * LIFESPAN
     * realmId starts at 1
     */
    private static final int[] REALM_LIFESPAN = {
            100,        // 1 Mortal
            150,        // 2 Luyện Khí
            250,        // 3 Trúc Cơ
            500,        // 4 Kim Đan
            1000,       // 5 Nguyên Anh
            2000,       // 6 Hóa Thần
            4000,       // 7 Luyện Hư
            8000,       // 8 Hợp Thể
            15000,      // 9 Đại Thừa
            30000,      // 10 Độ Kiếp
            60000,      // 11 Nhân Tiên
            120000,     // 12 Địa Tiên
            250000,     // 13 Thiên Tiên
            500000,     // 14 Kim Tiên
            1000000,    // 15 Thái Ất
            5000000,    // 16 Đại La
            50000000,   // 17 Đạo Tổ
            Integer.MAX_VALUE // 18 Thiên Đạo
    };

    /**
     * Returns game years passed since a given time.
     */
    public static long getGameYearsPassed(LocalDateTime beginTime) {
        long realHours = Duration
                .between(beginTime, LocalDateTime.now())
                .toHours();

        return realHours * GAME_YEARS_PER_REAL_HOUR;
    }

    /**
     * Returns required CP for breakthrough.
     * <p>
     * Example:
     * realm 1 stage 1 = 100
     * realm 1 stage 10 ≈ 860
     * realm 2 stage 1 ≈ 1160
     */
    public static long getBreakthroughCpPoints(int realmId, int realmStageId)
    {
        int globalStage = ((realmId - 1) * STAGES_PER_REALM) + realmStageId;
        return Math.round(BASE_BREAKTHROUGH_CP * Math.pow(BREAKTHROUGH_GROWTH_RATE, globalStage - 1));
    }

    public static int getPlayerLifeSpan(int realmId) {
        if (realmId < 1 || realmId > REALM_LIFESPAN.length) {
            throw new IllegalArgumentException(
                    "Invalid realm id: " + realmId
            );
        }
        return REALM_LIFESPAN[realmId - 1];
    }

    /**
     * realm 1 stage 1 = 1
     * realm 18 stage 10 = 180
     * @return global stage number
     */
    public static int getGlobalStage(int realmId, int realmStageId) {
        return ((realmId - 1) * STAGES_PER_REALM) + realmStageId;
    }
}