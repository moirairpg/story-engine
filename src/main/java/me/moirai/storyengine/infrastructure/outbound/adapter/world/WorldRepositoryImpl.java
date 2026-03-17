package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static me.moirai.storyengine.common.dbutil.SearchPredicates.canUserRead;
import static me.moirai.storyengine.common.dbutil.SearchPredicates.canUserWrite;
import static me.moirai.storyengine.common.dbutil.SearchPredicates.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.WorldLorebookPersistenceMapper;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.WorldPersistenceMapper;

@Repository
public class WorldRepositoryImpl implements WorldRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String WRITE = "WRITE";
    private static final String NAME = "name";
    private static final String VISIBILITY = "visibility";
    private static final String OWNER_DISCORD_ID = "ownerId";
    private static final String DEFAULT_SORT_BY_FIELD = "name";
    private static final String PERMISSIONS = "permissions";
    private static final String WORLD_ID = "worldId";
    private static final String DEFAULT_LOREBOOK_SORT_BY_FIELD = NAME;

    private final WorldJpaRepository jpaRepository;
    private final WorldPersistenceMapper mapper;
    private final WorldLorebookEntryJpaRepository lorebookJpaRepository;
    private final WorldLorebookPersistenceMapper lorebookMapper;

    public WorldRepositoryImpl(
            WorldJpaRepository jpaRepository,
            WorldPersistenceMapper mapper,
            WorldLorebookEntryJpaRepository lorebookJpaRepository,
            WorldLorebookPersistenceMapper lorebookMapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.lorebookJpaRepository = lorebookJpaRepository;
        this.lorebookMapper = lorebookMapper;
    }

    @Override
    public World save(World world) {

        return jpaRepository.save(world);
    }

    @Override
    public Optional<World> findById(Long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<World> findByPublicId(UUID publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public void deleteById(Long id) {

        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {

        jpaRepository.deleteByPublicId(publicId);
    }

    @Override
    public SearchWorldsResult search(SearchWorlds request) {

        int page = extractPageNumber(request.page());
        int size = extractPageSize(request.size());
        String sortByField = extractSortByField(request.sortingField());
        Direction direction = extractDirection(request.direction());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<World> query = buildSearchQuery(request);
        Page<World> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    @Override
    public SearchWorldLorebookEntriesResult searchLorebookEntries(SearchWorldLorebookEntries request) {

        int page = extractPageNumber(request.page());
        int size = extractPageSize(request.size());
        String sortByField = extractLorebookSortByField(request.sortingField());
        Direction direction = extractDirection(request.direction());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<WorldLorebookEntry> query = buildLorebookSearchQuery(request);
        Page<WorldLorebookEntry> pagedResult = lorebookJpaRepository.findAll(query, pageRequest);

        return lorebookMapper.mapToResult(pagedResult);
    }

    private Specification<World> buildSearchQuery(SearchWorlds request) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (WRITE.equals(request.operation())) {
                predicates.add(canUserWrite(cb, root, request.requesterId()));
            } else {
                predicates.add(canUserRead(cb, root, request.requesterId()));
            }

            if (isNotBlank(request.name())) {
                predicates.add(contains(cb, root, NAME, request.name()));
            }

            if (isNotBlank(request.ownerId())) {
                predicates.add(cb.equal(root.get(PERMISSIONS)
                        .get(OWNER_DISCORD_ID), cb.literal(request.ownerId())));
            }

            if (isNotBlank(request.visibility())) {
                predicates.add(contains(cb, root, VISIBILITY, request.visibility()));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private Specification<WorldLorebookEntry> buildLorebookSearchQuery(SearchWorldLorebookEntries query) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get(WORLD_ID), query.worldId()));

            if (isNotBlank(query.name())) {
                predicates.add(contains(cb, root, NAME, query.name()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Direction extractDirection(String direction) {
        return isBlank(direction) ? ASC : Direction.fromString(direction);
    }

    private String extractSortByField(String sortByField) {
        return isBlank(sortByField) ? DEFAULT_SORT_BY_FIELD : sortByField;
    }

    private String extractLorebookSortByField(String sortByField) {
        return isBlank(sortByField) ? DEFAULT_LOREBOOK_SORT_BY_FIELD : sortByField;
    }

    private int extractPageSize(Integer pageSize) {
        return pageSize == null ? DEFAULT_ITEMS : pageSize;
    }

    private int extractPageNumber(Integer page) {
        return page == null ? DEFAULT_PAGE : page - 1;
    }
}
