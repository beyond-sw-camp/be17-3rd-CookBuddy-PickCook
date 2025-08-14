package org.example.be17pickcook.recipe.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.recipe.model.RecipeDto;
import org.example.be17pickcook.recipe.service.RecipeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipe")
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping(value="/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity register(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                   @RequestPart RecipeDto.RecipeRequestDto dto,
                                   @RequestPart(value = "files", required = false)List<MultipartFile> files) throws SQLException, IOException {
        recipeService.register(authUser, dto, files);

        return ResponseEntity.status(200).body("레시피 작성 성공");
    }

    @GetMapping("/list")
    public ResponseEntity list(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        return null;
    }
}
