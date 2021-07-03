/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.history;

import com.vaadin.flow.data.provider.SortDirection;

/**
 *
 * @author azrul
 */
public class TableMomento {
    private Integer page;
    private String sortColumn;
    private SortDirection sortDirection;
    
   

    /**
     * @return the page
     */
    public Integer getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * @return the sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * @param sortColumn the sortColumn to set
     */
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * @return the sortDirection
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * @param sortDirection the sortDirection to set
     */
    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    
}
