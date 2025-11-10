package com.ltl.ltl.ltl.web.dto;

import java.util.List;

public class TableauNodeDto {
    public List<String> formulas;
    /** New: detailed entries including whether each formula was checked during expansion. */
    public List<EntryDto> entries;
    public Boolean closed;
    public List<TableauNodeDto> children;

    public static class EntryDto {
        public String formula;
        public boolean checked;

        public EntryDto() {}

        public EntryDto(String formula, boolean checked) {
            this.formula = formula;
            this.checked = checked;
        }
    }
}
