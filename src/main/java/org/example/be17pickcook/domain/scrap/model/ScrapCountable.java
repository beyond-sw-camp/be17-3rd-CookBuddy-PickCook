package org.example.be17pickcook.domain.scrap.model;

public interface ScrapCountable {
    Long getIdxScrap();
    Long getScrapCount();
    void increaseScrap();
    void decreaseScrap();
}
