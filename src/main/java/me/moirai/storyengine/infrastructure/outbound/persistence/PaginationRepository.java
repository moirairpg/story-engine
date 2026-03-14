package me.moirai.storyengine.infrastructure.outbound.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;

@NoRepositoryBean
public interface PaginationRepository<T, U> extends PagingAndSortingRepository<T, U>, JpaSpecificationExecutor<T> {

    Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);
}
