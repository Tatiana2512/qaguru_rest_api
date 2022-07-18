package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class ListResourceResponse {
    @JsonProperty("per_page")
    private int perPage;
    private int total;
    private List<DataItem> data;
    private int page;
    @JsonProperty("total_pages")
    private int totalPages;
    private Support support;
}