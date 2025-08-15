package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response for walks list")
public class WalksPageResponse {

    @Schema(description = "List of walks for the current page")
    private List<WalkListItem> content;

    @Schema(description = "Current page number (zero-based)", example = "0")
    private Integer page;

    @Schema(description = "Number of items per page", example = "10")
    private Integer size;

    @Schema(description = "Total number of pages", example = "3")
    private Integer totalPages;

    @Schema(description = "Total number of walks", example = "21")
    private Long totalElements;

    // Constructors
    public WalksPageResponse() {}

    public WalksPageResponse(List<WalkListItem> content, Integer page, Integer size, 
                           Integer totalPages, Long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    // Getters and Setters
    public List<WalkListItem> getContent() {
        return content;
    }

    public void setContent(List<WalkListItem> content) {
        this.content = content;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
}
