package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.Refrigerator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefrigeratorRepository extends JpaRepository<Refrigerator, Long> {
}
