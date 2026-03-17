package me.moirai.storyengine.common.dbutil;

import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.fasterxml.uuid.Generators;

public class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {

        var id = (UUID) session.getEntityPersister(null, object)
                .getIdentifier(object, session);

        if (id != null) {
            return id;
        }

        return Generators.timeBasedEpochGenerator().generate();
    }
}
