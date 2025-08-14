package org.example.be17pickcook.refrigerator.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.refrigerator.repository.RefrigeratorRepository;
import org.example.be17pickcook.refrigerator.model.Refrigerator;
import org.example.be17pickcook.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.refrigerator.model.RefrigeratorItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefrigeratorService {

    private final RefrigeratorRepository refrigeratorRepository;
    private final RefrigeratorItemRepository itemRepository;

    public List<RefrigeratorItemDto.ItemRes> listItems(Long refrigeratorId) {
        return itemRepository.findByRefrigerator_Id(refrigeratorId)
                .stream()
                .map(RefrigeratorItemDto.ItemRes::from)
                .toList();
    }

    @Transactional
    public RefrigeratorItemDto.ItemRes addItem(Long refrigeratorId, RefrigeratorItemDto.Register req) {
        Refrigerator ref = refrigeratorRepository.findById(refrigeratorId)
                .orElseThrow(() -> new IllegalArgumentException("Refrigerator not found: " + refrigeratorId));

        RefrigeratorItem saved = itemRepository.save(req.toEntity(ref));
        return RefrigeratorItemDto.ItemRes.from(saved);
    }

    @Transactional
    public RefrigeratorItemDto.ItemRes updateItem(Long itemId, RefrigeratorItemDto.Update req) {
        RefrigeratorItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        req.apply(item); // changeQuantity / changeExpirationDate 호출
        return RefrigeratorItemDto.ItemRes.from(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }
}
