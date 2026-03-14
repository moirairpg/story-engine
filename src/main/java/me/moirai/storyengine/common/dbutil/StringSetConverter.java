package me.moirai.storyengine.common.dbutil;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(Set<String> inputList) {

        return SetUtils.emptyIfNull(inputList)
                .stream()
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public Set<String> convertToEntityAttribute(String inputString) {

        if (StringUtils.isBlank(inputString)) {
            return Collections.emptySet();
        }

        return Arrays.asList(inputString.split(SPLIT_CHAR))
                .stream()
                .collect(toSet());
    }
}