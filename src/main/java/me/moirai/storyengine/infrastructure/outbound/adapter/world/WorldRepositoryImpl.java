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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.WorldPersistenceMapper;

@Repository
public class WorldRepositoryImpl implements WorldRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String ID = "id";
    private static final String WRITE = "WRITE";
    private static final String NAME = "name";
    private static final String ASSET_ID = "assetId";
    private static final String ASSET_TYPE = "assetType";
    private static final String WORLD = "world";
    private static final String VISIBILITY = "visibility";
    private static final String OWNER_DISCORD_ID = "ownerId";
    private static final String DEFAULT_SORT_BY_FIELD = "name";
    private static final String PERMISSIONS = "permissions";

    private final WorldJpaRepository jpaRepository;
    private final FavoriteRepository favoriteRepository;
    private final WorldPersistenceMapper mapper;

    public WorldRepositoryImpl(
            WorldJpaRepository jpaRepository,
            FavoriteRepository favoriteRepository,
            WorldPersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.favoriteRepository = favoriteRepository;
        this.mapper = mapper;
    }

    @Override
    public World save(World world) {

        return jpaRepository.save(world);
    }

    @Override
    public Optional<World> findById(String id) {

        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(String id) {

        favoriteRepository.deleteAllByAssetId(id);

        jpaRepository.deleteById(id);
    }

    @Override
    public SearchWorldsResult search(SearchWorlds request) {

        int page = extractPageNumber(request.getPage());
        int size = extractPageSize(request.getSize());
        String sortByField = extractSortByField(request.getSortingField());
        Direction direction = extractDirection(request.getDirection());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<World> query = buildSearchQuery(request);
        Page<World> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    private Specification<World> buildSearchQuery(SearchWorlds request) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (WRITE.equals(request.getOperation())) {
                predicates.add(canUserWrite(cb, root, request.getRequesterDiscordId()));
            } else {
                predicates.add(canUserRead(cb, root, request.getRequesterDiscordId()));
            }

            if (request.isFavorites()) {
                Subquery<String> subquery = cq.subquery(String.class);
                Root<FavoriteEntity> favoriteRoot = subquery.from(FavoriteEntity.class);

                subquery.select(favoriteRoot.get(ASSET_ID))
                        .where(cb.equal(favoriteRoot.get(ASSET_TYPE), WORLD));

                predicates.add(root.get(ID).in(subquery));
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
