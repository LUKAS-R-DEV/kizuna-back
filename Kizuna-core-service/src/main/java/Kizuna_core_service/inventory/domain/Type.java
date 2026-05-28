package Kizuna_core_service.inventory.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Type {
    RAW,
    FINISHED,
    WASTE;

    @JsonCreator
    public static Type fromString(String value){
        return value == null ? null : Type.valueOf(value.toUpperCase());
    }
}
