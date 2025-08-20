package org.example.be17pickcook.domain.refrigerator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.example.be17pickcook.domain.refrigerator.service.RefrigeratorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refrigerators")
@Validated // ← PathVariable/RequestParam 검증 활성화
public class RefrigeratorController {

    private final RefrigeratorService service;

    /** 특정 냉장고에 들어있는 아이템 목록 조회 */
    @GetMapping("/{refrigeratorId}/items")
    public List<RefrigeratorItemDto.ItemRes> list(
            @PathVariable @Positive(message = "냉장고 ID는 1 이상의 양수여야 합니다.")
            Long refrigeratorId) {
        return service.listItems(refrigeratorId);
    }

    /** 특정 냉장고에 새 아이템 등록 */
    @PostMapping("/{refrigeratorId}/items")
    public ResponseEntity<String> add(@PathVariable Long refrigeratorId,
                                      @Valid @RequestBody RefrigeratorItemDto.Register req) {
        service.addItem(refrigeratorId, req);  // 반환값 없음
        return ResponseEntity.ok("냉장고 아이템 등록 성공!");
    }

    /** 냉장고 아이템 수정 */
    @PutMapping("/items/{itemId}")
    public RefrigeratorItemDto.ItemRes update(
            @PathVariable @Positive(message = "아이템 ID는 1 이상의 양수여야 합니다.")
            Long itemId,
            @Valid @RequestBody RefrigeratorItemDto.Update req) {
        return service.updateItem(itemId, req);
    }

    /** 냉장고 아이템 삭제 */
    @DeleteMapping("/items/{itemId}")
    public void delete(
            @PathVariable @Positive(message = "아이템 ID는 1 이상의 양수여야 합니다.")
            Long itemId) {
        service.deleteItem(itemId);
    }
}
