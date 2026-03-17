package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

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
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.AdventureLorebookPersistenceMapper;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.AdventurePersistenceMapper;

@Repository
public class AdventureRepositoryImpl implements AdventureRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String WRITE = "WRITE";
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
    private static final String ADVENTURE_ID = "adventureId";
    private static final String DEFAULT_LOREBOOK_SORT_BY_FIELD = NAME;

    private final AdventureJpaRepository jpaRepository;
    private final AdventurePersistenceMapper mapper;
    private final AdventureLorebookEntryJpaRepository lorebookJpaRepository;
    private final AdventureLorebookPersistenceMapper lorebookMapper;

    public AdventureRepositoryImpl(
            AdventureJpaRepository jpaRepository,
            AdventurePersistenceMapper mapper,
            AdventureLorebookEntryJpaRepository lorebookJpaRepository,
            AdventureLorebookPersistenceMapper lorebookMapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.lorebookJpaRepository = lorebookJpaRepository;
        this.lorebookMapper = lorebookMapper;
    }

    @Override
    public Adventure save(Adventure adventure) {

        return jpaRepository.save(adventure);
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
    public Optional<Adventure> findById(Long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Adventure> findByPublicId(UUID publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Optional<Adventure> findByChannelId(String channelId) {

        return jpaRepository.findByChannelId(channelId);
    }

    @Override
    public SearchAdventuresResult search(SearchAdventures request) {

        int page = extractPageNumber(request.page());
        int size = extractPageSize(request.size());
        String sortByField = extractSortByField(request.sortingField());
        Direction direction = extractDirection(request.direction());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<Adventure> query = buildSearchQuery(request);
        Page<Adventure> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    @Override
    public SearchAdventureLorebookEntriesResult searchLorebookEntries(SearchAdventureLorebookEntries request) {

        int page = extractPageNumber(request.page());
        int size = extractPageSize(request.size());
        String sortByField = extractLorebookSortByField(request.sortingField());
        Direction direction = extractDirection(request.direction());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<AdventureLorebookEntry> query = buildLorebookSearchQuery(request);
        Page<AdventureLorebookEntry> pagedResult = lorebookJpaRepository.findAll(query, pageRequest);

        return lorebookMapper.mapToResult(pagedResult);
    }

    @Override
    public String getGameModeByChannelId(String channelId) {

        return jpaRepository.getGameModeByChannelId(channelId);
    }

    private Specification<Adventure> buildSearchQuery(SearchAdventures request) {

        return (root, cq, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (WRITE.equals(request.operation())) {
                predicates.add(canUserWrite(cb, root, request.requesterId()));
            } else {
                predicates.add(canUserRead(cb, root, request.requesterId()));
            }

            if (isNotBlank(request.name())) {
                predicates.add(contains(cb, root, NAME, request.name()));
            }

            if (isNotBlank(request.worldName())) {
                predicates.add(contains(cb, root, WORLD_ID, request.worldName()));
            }

            if (isNotBlank(request.personaName())) {
                predicates.add(contains(cb, root, PERSONA_ID, request.personaName()));
            }

            if (isNotBlank(request.ownerId())) {
                predicates.add(cb.equal(root.get(PERMISSIONS)
                        .get(OWNER_DISCORD_ID), cb.literal(request.ownerId())));
            }

            if (isNotBlank(request.model())) {
                predicates.add(cb.like(cb.upper(cb.toString(root.get(MODEL_CONFIGURATION)
                        .get(AI_MODEL))), cb.literal(request.model().toUpperCase())));
            }

            if (isNotBlank(request.gameMode())) {
                predicates.add(contains(cb, root, GAME_MODE, request.gameMode()));
            }

            if (isNotBlank(request.moderation())) {
                predicates.add(contains(cb, root, MODERATION, request.moderation()));
            }

            if (isNotBlank(request.visibility())) {
                predicates.add(contains(cb, root, VISIBILITY, request.visibility()));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private Specification<AdventureLorebookEntry> buildLorebookSearchQuery(SearchAdventureLorebookEntries query) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get(ADVENTURE_ID), query.adventureId()));

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
