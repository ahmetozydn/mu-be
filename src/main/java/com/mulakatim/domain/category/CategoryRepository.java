package com.mulakatim.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByActiveTrueOrderBySortOrderAsc();

    Optional<Category> findByIdAndActiveTrue(String id);
}
