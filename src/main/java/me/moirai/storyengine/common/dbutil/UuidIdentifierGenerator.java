package me.moirai.storyengine.common.dbutil;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.fasterxml.uuid.Generators;

public class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {

        final String id = (String) session.getEntityPersister(null, object)
                .getIdentifier(object, session);

        if (StringUtils.isNotBlank(id)) {
            return id;
        }

        return Generators.timeBasedEpochGenerator().generate().toString();
    }
}
