package com.kmovie.controller;

import com.kmovie.dto.request.TitleRequest;
import com.kmovie.dto.response.ApiResponse;
import com.kmovie.dto.response.PageResponse;
import com.kmovie.dto.response.TitleResponse;
import com.kmovie.entity.Title;
import com.kmovie.enums.Category;
import com.kmovie.enums.Genre;
import com.kmovie.service.TitleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movies/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TitleResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) UUID parentTitleId,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Category category) {

        PageResponse<Title> result = titleService.search(search, page, count, parentTitleId, genre, category);
        List<TitleResponse> items = result.getItems().stream().map(TitleResponse::new).collect(Collectors.toList());
        PageResponse<TitleResponse> response =
                new PageResponse<>(items, result.getPage(), result.getCount(), result.getTotalItems());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TitleResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(new TitleResponse(titleService.getById(id))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TitleResponse>> create(@Valid @RequestBody TitleRequest request) {
        Title title = titleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Title created", new TitleResponse(title)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<TitleResponse>> update(@PathVariable UUID id, @RequestBody TitleRequest request) {
        Title title = titleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Title updated", new TitleResponse(title)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        titleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Title and its child titles/videos deleted"));
    }
}
