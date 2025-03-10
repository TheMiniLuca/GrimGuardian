package com.gmail.theminiluca.grim.guardian.utils.config.model.formula;

import lombok.Getter;

@Getter
public enum Formula {
    CORRECT,
    INCORRECT,
    EFFICIENCY,
    HASTE,
    MINING_FATIGUE,
    INSTANT(StatementType.CONDITIONAL);

    private final StatementType statementType;

    Formula(StatementType statementType) {
        this.statementType = statementType;
    }

    Formula() {
        this.statementType = StatementType.FUNCTIONAL;
    }
}
