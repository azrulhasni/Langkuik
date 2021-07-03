/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.table;

import com.azrul.langkuik.framework.dao.DAOQuery;
import com.azrul.langkuik.framework.dao.FindAnyEntityQuery;
import com.vaadin.flow.data.provider.SortDirection;
import java.io.Serializable;

/**
 *
 * @author azrul
 */
public class MasterDetailMomento implements Serializable {
    private final Integer page;
    private final DAOQuery searchQuery;
    private final String sortColumn;
    private final SortDirection sortDirection;

    public MasterDetailMomento(Integer page, DAOQuery searchQuery, String sortColumn,SortDirection sortDirection) {
        this.page = page;
        this.searchQuery = searchQuery;
        this.sortColumn = sortColumn;
        this.sortDirection=sortDirection;
    }

    /**
     * @return the page
     */
    public Integer getPage() {
        return page;
    }

    /**
     * @return the searchQuery
     */
    public DAOQuery getSearchQuery() {
        return searchQuery;
    }

    /**
     * @return the sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * @return the sortDirection
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }
    
    
}
