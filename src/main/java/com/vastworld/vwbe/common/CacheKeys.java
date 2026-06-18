package com.vastworld.vwbe.common;

public final class CacheKeys {

    private static String build(String prefix, Object... keys) {
        var sb = new StringBuilder(prefix);

        for (var key : keys) {
            sb.append(":").append(key);
        }

        return sb.toString();
    }

    public static String cultivationRealms(Object... keys) {
        return build("cultivation-realms", keys);
    }
    public static String playerSpiritRoots(Object... keys) {
        return build("player-spirit-roots", keys);
    }
    public static String players(Object... keys) {
        return build("players", keys);
    }
    public static String spiritRoots(Object... keys) {
        return build("spiritRoots", keys);
    }
    public static String playerMeditation(Object... keys){
        return build("playerMeditation", keys);
    }
}
