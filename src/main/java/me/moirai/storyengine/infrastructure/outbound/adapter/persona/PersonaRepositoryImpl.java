package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static me.moirai.storyengine.common.dbutil.SearchPredicates.canUserRead;
import static me.moirai.storyengine.common.dbutil.SearchPredicates.canUserWrite;
import static me.moirai.storyengine.common.dbutil.SearchPredicates.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.PersonaPersistenceMapper;

@Repository
public class PersonaRepositoryImpl implements PersonaRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String NAME = "name";
    private static final String WRITE = "WRITE";
    private static final String OWNER_DISCORD_ID = "ownerId";
    private static final String VISIBILITY = "visibility";
    private static final String DEFAULT_SORT_BY_FIELD = NAME;
    private static final String PERMISSIONS = "permissions";

    private final PersonaJpaRepository jpaRepository;
    private final PersonaPersistenceMapper mapper;

    public PersonaRepositoryImpl(
            PersonaJpaRepository jpaRepository,
            PersonaPersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Persona save(Persona persona) {

        return jpaRepository.save(persona);
    }

    @Override
    public Optional<Persona> findById(Long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Persona> findByPublicId(String publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public void deleteById(Long id) {

        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByPublicId(String publicId) {

        jpaRepository.deleteByPublicId(publicId);
    }

    @Override
    public boolean existsById(Long id) {

        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByPublicId(String publicId) {

        return jpaRepository.existsByPublicId(publicId);
    }

    @Override
    public SearchPersonasResult search(SearchPersonas request) {

        int page = extractPageNumber(request.getPage());
        int size = extractPageSize(request.getSize());
        String sortByField = extractSortByField(request.getSortingField());
        Direction direction = extractDirection(request.getDirection());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<Persona> query = buildSearchQuery(request);
        Page<Persona> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    private Specification<Persona> buildSearchQuery(SearchPersonas request) {

        return (root, cq, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (WRITE.equals(request.getOperation())) {
                predicates.add(canUserWrite(cb, root, request.getRequesterDiscordId()));
            } else {
                predicates.add(canUserRead(cb, root, request.getRequesterDiscordId()));
            }

            if (isNotBlank(request.getName())) {
                predicates.add(contains(cb, root, NAME, request.getName()));
            }

            if (isNotBlank(request.getOwnerId())) {
                predicates.add(cb.equal(root.get(PERMISSIONS)
                        .get(OWNER_DISCORD_ID), cb.literal(request.getOwnerId())));
            }

            if (isNotBlank(request.getVisibility())) {
                predicates.add(contains(cb, root, VISIBILITY, request.getVisibility()));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private Direction extractDirection(String direction) {
        return isBlank(direction) ? ASC : Direction.fromString(direction);
    }

    private String extractSortByField(String sortByField) {
        return isBlank(sortByField) ? DEFAULT_SORT_BY_FIELD : sortByField;
    }

    private int extractPageSize(Integer pageSize) {
        return pageSize == null ? DEFAULT_ITEMS : pageSize;
    }

    private int extractPageNumber(Integer page) {
        return page == null ? DEFAULT_PAGE : page - 1;
    }
}
