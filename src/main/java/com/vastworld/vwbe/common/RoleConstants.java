package com.vastworld.vwbe.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleConstants {
    PLAYER("PLAYER"),
    ADMIN("ADMIN");

    private final String value;
}