package com.yineng.ynmessager.bean.app;

import java.util.List;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：
 */

public class NewAppPageContent {


    /**
     * size : 5
     * number : 0
     * sort : null
     * first : true
     * firstPage : true
     * lastPage : true
     * totalElements : 4
     * totalPages : 1
     * last : true
     * numberOfElements : 4
     * content :[]
     */

    private int size;
    private int number;
    private int sort;
    private boolean first;
    private boolean firstPage;
    private boolean lastPage;
    private int totalElements;
    private int totalPages;
    private boolean last;
    private int numberOfElements;
    private List<NewAppContentItem> content;

    public List<NewAppContentItem> getContent() {
        return content;
    }

    public void setContent(List<NewAppContentItem> content) {
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
}
