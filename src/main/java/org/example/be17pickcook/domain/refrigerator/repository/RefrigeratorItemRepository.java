package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefrigeratorItemRepository extends JpaRepository<RefrigeratorItem, Long> {
    List<RefrigeratorItem> findByRefrigerator_Id(Long refrigeratorId);
}
