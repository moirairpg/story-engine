package me.moirai.storyengine.common.dbutil;

public record Filter(String clause, String paramName, Object value) {

    public Filter(String clause) {
        this(clause, null, null);
    }

    public Filter(String clause, String paramName, Object value) {
        this.clause = clause;
        this.paramName = paramName;
        this.value = value;
    }
}
