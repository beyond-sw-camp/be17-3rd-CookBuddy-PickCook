package org.example.be17pickcook.refrigerator.repository;

import org.example.be17pickcook.refrigerator.model.Refrigerator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefrigeratorRepository extends JpaRepository<Refrigerator, Long> {
}
