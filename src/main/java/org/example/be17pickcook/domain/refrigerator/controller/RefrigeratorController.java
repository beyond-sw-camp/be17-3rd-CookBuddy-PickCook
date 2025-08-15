package org.example.be17pickcook.domain.refrigerator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.refrigerator.service.RefrigeratorService;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refrigerators")
public class RefrigeratorController {

    private final RefrigeratorService service;

    @GetMapping("/{refrigeratorId}/items")
    public List<RefrigeratorItemDto.ItemRes> list(@PathVariable Long refrigeratorId) {
        return service.listItems(refrigeratorId);
    }

    @PostMapping("/{refrigeratorId}/items")
    public RefrigeratorItemDto.ItemRes add(@PathVariable Long refrigeratorId,
                                           @Valid @RequestBody RefrigeratorItemDto.Register req) {
        return service.addItem(refrigeratorId, req);
    }

    @PutMapping("/items/{itemId}")
    public RefrigeratorItemDto.ItemRes update(@PathVariable Long itemId,
                                              @RequestBody RefrigeratorItemDto.Update req) {
        return service.updateItem(itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    public void delete(@PathVariable Long itemId) {
        service.deleteItem(itemId);
    }
}
