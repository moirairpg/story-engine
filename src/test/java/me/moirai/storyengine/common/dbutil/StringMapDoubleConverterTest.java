package me.moirai.storyengine.common.dbutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class StringMapDoubleConverterTest {

    private StringMapDoubleConverter converter = new StringMapDoubleConverter();

    @Test
    public void convertStringToMap() {

        // Given
        String commaSeparatedValues = "k1=1.0,k2=2.0";

        // When
        Map<String, Double> result = converter.convertToEntityAttribute(commaSeparatedValues);

        // Then
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .containsEntry("k1", 1.0)
                .containsEntry("k2", 2.0);
    }

    @Test
    public void convertMapToString() {

        // Given
        String commaSeparatedValues = "k1=1.0,k2=2.0";
        Map<String, Double> map = new HashMap<>();
        map.put("k1", 1.0);
        map.put("k2", 2.0);

        // When
        String result = converter.convertToDatabaseColumn(map);

        // Then
        assertThat(commaSeparatedValues).isEqualTo(result);
    }
}
