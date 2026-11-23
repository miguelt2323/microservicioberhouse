package com.microserviciobeerhouse.api.repository;

import com.microserviciobeerhouse.api.model.categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<categoria, Integer> {
    Page<categoria> findAll(Pageable pageable);
}
