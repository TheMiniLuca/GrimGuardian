package com.gmail.theminiluca.grim.guardian.utils.config.model.formula;

import lombok.Getter;

@Getter
public enum StatementType {
    CONDITIONAL("condition"),
    FUNCTIONAL("function");

    private final String path;

    StatementType(String path) {
        this.path = path;
    }
}