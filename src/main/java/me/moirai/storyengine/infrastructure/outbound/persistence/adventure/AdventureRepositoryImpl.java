package me.moirai.storyengine.infrastructure.outbound.persistence.adventure;

import static me.moirai.storyengine.infrastructure.outbound.persistence.SearchPredicates.canUserRead;
import static me.moirai.storyengine.infrastructure.outbound.persistence.SearchPredicates.canUserWrite;
import static me.moirai.storyengine.infrastructure.outbound.persistence.SearchPredicates.contains;
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
import me.moirai.storyengine.core.application.usecase.adventure.request.SearchAdventures;
import me.moirai.storyengine.core.application.usecase.adventure.result.SearchAdventuresResult;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.outbound.persistence.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.persistence.FavoriteRepository;
import me.moirai.storyengine.infrastructure.outbound.persistence.mapper.AdventurePersistenceMapper;

@Repository
public class AdventureRepositoryImpl implements AdventureRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String WRITE = "WRITE";
    private static final String ID = "id";
    private static final String ASSET_ID = "assetId";
    private static final String ASSET_TYPE = "assetType";
    private static final String ADVENTURE = "adventure";
    private static final String NAME = "name";
    private static final String GAME_MODE = "gameMode";
    private static final String VISIBILITY = "visibility";
    private static final String PERSONA_ID = "personaId";
    private static final String WORLD_ID = "worldId";
    private static final String MODERATION = "moderation";
    private static final String OWNER_DISCORD_ID = "ownerId";
    private static final String MODEL_CONFIGURATION = "modelConfiguration";
    private static final String DEFAULT_SORT_BY_FIELD = "creationDate";
    private static final String AI_MODEL = "aiModel";
    private static final String PERMISSIONS = "permissions";

    private final AdventureJpaRepository jpaRepository;
    private final FavoriteRepository favoriteRepository;
    private final AdventurePersistenceMapper mapper;

    public AdventureRepositoryImpl(
            AdventureJpaRepository jpaRepository,
            FavoriteRepository favoriteRepository,
            AdventurePersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.favoriteRepository = favoriteRepository;
        this.mapper = mapper;
    }

    @Override
    public Adventure save(Adventure adventure) {

        return jpaRepository.save(adventure);
    }

    @Override
    public void deleteById(String id) {

        favoriteRepository.deleteAllByAssetId(id);
        jpaRepository.deleteById(id);
    }

    @Override
    public void updateRememberByChannelId(String remember, String channelId) {

        jpaRepository.updateRememberByChannelId(remember, channelId);
    }

    @Override
    public void updateAuthorsNoteByChannelId(String authorsNote, String channelId) {

        jpaRepository.updateAuthorsNoteByChannelId(authorsNote, channelId);
    }

    @Override
    public void updateNudgeByChannelId(String nudge, String channelId) {

        jpaRepository.updateNudgeByChannelId(nudge, channelId);
    }

    @Override
    public void updateBumpByChannelId(String bumpContent, int bumpFrequency, String channelId) {

        jpaRepository.updateBumpByChannelId(bumpContent, bumpFrequency, channelId);
    }

    @Override
    public Optional<Adventure> findById(String id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Adventure> findByChannelId(String channelId) {

        return jpaRepository.findByChannelId(channelId);
    }

    @Override
    public SearchAdventuresResult search(SearchAdventures request) {

        int page = extractPageNumber(request.getPage());
        int size = extractPageSize(request.getSize());
        String sortByField = extractSortByField(request.getSortingField());
        Direction direction = extractDirection(request.getDirection());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<Adventure> query = buildSearchQuery(request);
        Page<Adventure> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    @Override
    public String getGameModeByChannelId(String channelId) {

        return jpaRepository.getGameModeByChannelId(channelId);
    }

    private Specification<Adventure> buildSearchQuery(SearchAdventures request) {

        return (root, cq, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (WRITE.equals(request.getOperation())) {
                predicates.add(canUserWrite(cb, root, request.getRequesterDiscordId()));
            } else {
                predicates.add(canUserRead(cb, root, request.getRequesterDiscordId()));
            }

            if (request.isFavorites()) {
                Subquery<String> subquery = cq.subquery(String.class);
                Root<FavoriteEntity> favoriteRoot = subquery.from(FavoriteEntity.class);

                subquery.select(favoriteRoot.get(ASSET_ID))
                        .where(cb.equal(favoriteRoot.get(ASSET_TYPE), ADVENTURE));

                predicates.add(root.get(ID).in(subquery));
            }

            if (isNotBlank(request.getName())) {
                predicates.add(contains(cb, root, NAME, request.getName()));
            }

            if (isNotBlank(request.getWorld())) {
                predicates.add(contains(cb, root, WORLD_ID, request.getWorld()));
            }

            if (isNotBlank(request.getPersona())) {
                predicates.add(contains(cb, root, PERSONA_ID, request.getPersona()));
            }

            if (isNotBlank(request.getOwnerId())) {
                predicates.add(cb.equal(root.get(PERMISSIONS)
                        .get(OWNER_DISCORD_ID), cb.literal(request.getOwnerId())));
            }

            if (isNotBlank(request.getModel())) {
                predicates.add(cb.like(cb.upper(cb.toString(root.get(MODEL_CONFIGURATION)
                        .get(AI_MODEL))), cb.literal(request.getModel().toUpperCase())));
            }

            if (isNotBlank(request.getGameMode())) {
                predicates.add(contains(cb, root, GAME_MODE, request.getGameMode()));
            }

            if (isNotBlank(request.getModeration())) {
                predicates.add(contains(cb, root, MODERATION, request.getModeration()));
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
