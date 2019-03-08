package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Responses in de API typically contain a meta data object that contains data about the amount of pages a response
 * contains, the pagesize etc. The competition drafts we use to show the standing in our league contains it, for
 * example, in the form of the draftsMeta
 */
@DataObject
public class MetaData {
    private final Integer pageIndex;
    private final Integer pageSize;
    private final Integer totalCount;
    private final Integer totalPages;
    private final Boolean hasPreviousPage;
    private final Boolean hasNextPage;

    public MetaData(JsonObject jsonObject) {
        this.pageIndex = jsonObject.getInteger("pageIndex");
        this.pageSize = jsonObject.getInteger("pageSize");
        this.totalCount = jsonObject.getInteger("totalCount");
        this.totalPages = jsonObject.getInteger("totalPages");
        this.hasPreviousPage = jsonObject.getBoolean("hasPreviousPage");
        this.hasNextPage = jsonObject.getBoolean("hasNextPage");
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Boolean getHasPreviousPage() {
        return hasPreviousPage;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", totalCount=" + totalCount +
                ", totalPages=" + totalPages +
                ", hasPreviousPage=" + hasPreviousPage +
                ", hasNextPage=" + hasNextPage +
                '}';
    }
}
