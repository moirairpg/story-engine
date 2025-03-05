package me.moirai.discordbot.common.dbutil;

import static java.util.Collections.emptyMap;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringObjectMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final String ERROR_SERIALIZING_MAP = "Error serializing map to save into database";
    private static final String ERROR_DESERIALIZING_MAP = "Error deserializing map from database";

    private static final Logger LOG = LoggerFactory.getLogger(StringObjectMapConverter.class);

    private final ObjectMapper objectMapper;

    public StringObjectMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(Map<String, Object> inputMap) {

        try {
            return objectMapper.writeValueAsString(MapUtils.emptyIfNull(inputMap));
        } catch (Exception e) {
            LOG.error(ERROR_SERIALIZING_MAP, e);
            throw new IllegalStateException(ERROR_SERIALIZING_MAP, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String inputString) {

        try {
            if (StringUtils.isBlank(inputString)) {
                return emptyMap();
            }

            return objectMapper.readValue(inputString, Map.class);
        } catch (Exception e) {
            LOG.error(ERROR_DESERIALIZING_MAP, e);
            throw new IllegalStateException(ERROR_DESERIALIZING_MAP, e);
        }
    }
}
