package org.example.be17pickcook.domain.refrigerator.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorRepository;
import org.example.be17pickcook.domain.refrigerator.model.Refrigerator;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    /** Product 스타일: 컨트롤러가 메시지로만 응답 → void 반환 */
    @Transactional
    public void addItem(Long refrigeratorId, RefrigeratorItemDto.Register req) {
        Refrigerator ref = refrigeratorRepository.findById(refrigeratorId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Refrigerator not found: " + refrigeratorId));

        try {
            itemRepository.save(req.toEntity(ref));
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약(같은 냉장고+재료명 중복) 등
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists in this refrigerator");
        }
    }

    @Transactional
    public RefrigeratorItemDto.ItemRes updateItem(Long itemId, RefrigeratorItemDto.Update req) {
        RefrigeratorItem item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));

        req.apply(item); // changeIngredientName / changeQuantity / changeExpirationDate
        return RefrigeratorItemDto.ItemRes.from(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId);
        }
        itemRepository.deleteById(itemId);
    }
}
