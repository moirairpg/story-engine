package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static me.moirai.storyengine.common.dbutil.SearchPredicates.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.outbound.adapter.mapper.AdventureLorebookPersistenceMapper;

@Repository
public class AdventureLorebookEntryRepositoryImpl implements AdventureLorebookEntryRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String NAME = "name";
    private static final String DEFAULT_SORT_BY_FIELD = NAME;

    private final AdventureLorebookEntryJpaRepository jpaRepository;
    private final AdventureLorebookPersistenceMapper mapper;

    public AdventureLorebookEntryRepositoryImpl(AdventureLorebookEntryJpaRepository jpaRepository,
            AdventureLorebookPersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AdventureLorebookEntry save(AdventureLorebookEntry entry) {

        return jpaRepository.save(entry);
    }

    @Override
    public Optional<AdventureLorebookEntry> findById(String lorebookEntryId) {

        return jpaRepository.findById(lorebookEntryId);
    }

    @Override
    public List<AdventureLorebookEntry> findAllByRegex(String valueToSearch, String adventureId) {

        return jpaRepository.findAllByNameRegex(valueToSearch, adventureId)
                .stream()
                .toList();
    }

    @Override
    public Optional<AdventureLorebookEntry> findByPlayerId(String playerId, String adventureId) {

        return jpaRepository.findByPlayerId(playerId, adventureId);
    }

    @Override
    public void deleteById(String id) {

        jpaRepository.deleteById(id);
    }

    @Override
    public SearchAdventureLorebookEntriesResult search(SearchAdventureLorebookEntries request) {

        int page = extractPageNumber(request.getPage());
        int size = extractPageSize(request.getSize());
        String sortByField = extractSortByField(request.getSortingField());
        Direction direction = extractDirection(request.getDirection());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<AdventureLorebookEntry> query = buildSearchQuery(request);
        Page<AdventureLorebookEntry> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    private Specification<AdventureLorebookEntry> buildSearchQuery(SearchAdventureLorebookEntries query) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("adventureId"), query.getAdventureId()));

            if (StringUtils.isNotBlank(query.getName())) {
                predicates.add(contains(cb, root, NAME, query.getName()));
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
