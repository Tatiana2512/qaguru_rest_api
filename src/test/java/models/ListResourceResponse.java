package models;

import lombok.Data;

import java.util.List;


@Data
public class ListResourceResponse {
    private int per_page;
    private int total;
    private List<DataItem> data;
    private int page;
    private int total_pages;
    private Support support;
}