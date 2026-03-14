package me.moirai.storyengine.infrastructure.outbound.persistence.notification;

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
import me.moirai.storyengine.core.application.usecase.notification.request.SearchNotifications;
import me.moirai.storyengine.core.application.usecase.notification.result.SearchNotificationsResult;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationRepository;
import me.moirai.storyengine.infrastructure.outbound.persistence.mapper.NotificationPersistenceMapper;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String RECEIVER_ID = "receiverDiscordId";
    private static final String SENDER_ID = "senderDiscordId";
    private static final String TYPE = "type";
    private static final String IS_GLOBAL = "isGlobal";
    private static final String IS_INTERACTABLE = "isInteractable";
    private static final String DEFAULT_SORT_BY_FIELD = "creationDate";

    private final NotificationJpaRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    public NotificationRepositoryImpl(
            NotificationJpaRepository jpaRepository,
            NotificationPersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Notification> findById(String id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Notification save(Notification notification) {

        return jpaRepository.save(notification);
    }

    @Override
    public void deleteById(String id) {

        jpaRepository.deleteById(id);
    }

    @Override
    public List<Notification> findUnreadByUserId(String userId) {

        return jpaRepository.findUnreadByUserId(userId);
    }

    @Override
    public List<Notification> findReadByUserId(String userId) {

        return jpaRepository.findReadByUserId(userId);
    }

    @Override
    public SearchNotificationsResult search(SearchNotifications request) {

        int page = extractPageNumber(request.getPage());
        int size = extractPageSize(request.getSize());
        String sortByField = extractSortByField(request.getSortingField());
        Direction direction = extractDirection(request.getDirection());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortByField));
        Specification<Notification> query = buildSearchQuery(request);
        Page<Notification> pagedResult = jpaRepository.findAll(query, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    private Specification<Notification> buildSearchQuery(SearchNotifications request) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isNotBlank(request.getSenderDiscordId())) {
                predicates.add(contains(cb, root, SENDER_ID, request.getSenderDiscordId()));
            }

            if (isNotBlank(request.getReceiverDiscordId())) {
                predicates.add(contains(cb, root, RECEIVER_ID, request.getReceiverDiscordId()));
            }

            if (isNotBlank(request.getType())) {
                predicates.add(contains(cb, root, TYPE, request.getType()));
            }

            if (request.isGlobal() != null) {
                predicates.add(cb.equal(root.get(IS_GLOBAL), request.isGlobal().booleanValue()));
            }

            if (request.isInteractable() != null) {
                predicates.add(cb.equal(root.get(IS_INTERACTABLE), request.isInteractable().booleanValue()));
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
