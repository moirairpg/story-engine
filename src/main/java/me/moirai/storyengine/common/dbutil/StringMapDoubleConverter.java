package me.moirai.storyengine.common.dbutil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringMapDoubleConverter implements AttributeConverter<Map<String, Double>, String> {

    private static final String SPLIT_CHAR = ",";
    private static final String ASSIGN_CHAR = "=";

    @Override
    public String convertToDatabaseColumn(Map<String, Double> inputMap) {

        return MapUtils.emptyIfNull(inputMap)
                .entrySet()
                .stream()
                .map(e -> e.getKey() + ASSIGN_CHAR + e.getValue())
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public Map<String, Double> convertToEntityAttribute(String inputString) {

        if (StringUtils.isBlank(inputString)) {
            return Collections.emptyMap();
        }

        return Arrays.stream(inputString.split(SPLIT_CHAR))
                .map(s -> s.split(ASSIGN_CHAR))
                .collect(Collectors.toMap(s -> s[0], s -> Double.valueOf(s[1])));
    }
}
