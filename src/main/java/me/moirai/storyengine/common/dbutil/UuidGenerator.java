package me.moirai.storyengine.common.dbutil;

import java.util.EnumSet;
import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import com.fasterxml.uuid.Generators;

public class UuidGenerator implements BeforeExecutionGenerator {

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public Object generate(
            SharedSessionContractImplementor session,
            Object owner,
            Object currentValue,
            EventType eventType) {

        if (currentValue != null) {
            return (UUID) currentValue;
        }

        return Generators.timeBasedEpochGenerator().generate();
    }
}
