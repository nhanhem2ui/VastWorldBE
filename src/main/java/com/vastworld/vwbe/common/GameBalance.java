package com.vastworld.vwbe.common;

import java.time.Duration;
import java.time.LocalDateTime;

public final class GameBalance {

    private GameBalance() {}

    public static final int INVENTORY_SLOT = 30;

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

    public static final double BASE_BREAKTHROUGH_CHANCE = 0.5D;

    public static final double MIN_BREAKTHROUGH_CHANCE = 0.0001D;
    public static final double MAX_BREAKTHROUGH_CHANCE = 1D;

    private static final double[] REALM_BREAKTHROUGH_CHANCE_MODIFIER = {
            0.7D, // Luyen Khi
            0.6D, // Truc Co
            0.55D, // Kim Dan
            0.52D, // Nguyen Anh
            0.5D, // Hoa Than
            0.45D,
            0.42D,
            0.4D,
            0.35D,
            0.32D,
            0.3D,
            0.25D,
            0.22D,
            0.2D,
            0.15D,
            0.1D,
            0.01D,
            0.001D
    };

    private static final double[] STAGE_BREAKTHROUGH_CHANCE_MODIFIER = {
            1.00D,
            0.98D,
            0.96D,
            0.94D,
            0.92D,
            0.90D,
            0.88D,
            0.86D,
            0.84D,
            0.70D
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

    public static int getNextRealmId(int realmId, int stageId) {
        if (stageId < STAGES_PER_REALM) {
            return realmId;
        }
        return realmId + 1;
    }
    public static int getNextStageId(int stageId) {
        if (stageId < STAGES_PER_REALM) {
            return stageId + 1;
        }
        return 1;
    }

    public static double getBreakthroughChance(int realmId, int stageId,
            double spiritRootMultiplier, double buffMultiplier
    ) {
        double realmModifier = REALM_BREAKTHROUGH_CHANCE_MODIFIER[realmId - 1];

        double stageModifier = STAGE_BREAKTHROUGH_CHANCE_MODIFIER[stageId - 1];

        double chance = BASE_BREAKTHROUGH_CHANCE
                        * spiritRootMultiplier
                        * realmModifier
                        * stageModifier
                        * buffMultiplier;

        return Math.clamp(chance, MIN_BREAKTHROUGH_CHANCE, MAX_BREAKTHROUGH_CHANCE);
    }
}