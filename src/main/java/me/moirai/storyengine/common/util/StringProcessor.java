package me.moirai.storyengine.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class StringProcessor {

    private final List<UnaryOperator<String>> rules = new ArrayList<>();

    public void addRule(UnaryOperator<String> rule) {

        rules.add(rule);
    }

    public String process(final String input) {

        String output = input;
        for (UnaryOperator<String> rule : rules) {
            output = rule.apply(output);
        }

        return output;
    }
}