/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.relationship;

import com.azrul.langkuik.views.table.PageNav;
import com.vaadin.flow.component.grid.Grid;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azrul
 */
public class RelationMemento {
    private Grid grid;
    private PageNav pageNav;
    private int page;
    private int maxPageCount;
    private Map<String, Object> otherStates;

    public RelationMemento(Grid grid, PageNav pageNav, int page, int maxPageCount) {
        this.grid = grid;
        this.pageNav = pageNav;
        this.page = page;
        this.maxPageCount = maxPageCount;
        this.otherStates = new HashMap<>(); 
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * @return the pageNav
     */
    public PageNav getPageNav() {
        return pageNav;
    }

    /**
     * @param pageNav the pageNav to set
     */
    public void setPageNav(PageNav pageNav) {
        this.pageNav = pageNav;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the maxPageCount
     */
    public int getMaxPageCount() {
        return maxPageCount;
    }

    /**
     * @param maxPageCount the maxPageCount to set
     */
    public void setMaxPageCount(int maxPageCount) {
        this.maxPageCount = maxPageCount;
    }
    
    public Object getOtherState(String key){
        return otherStates.get(key);
    }
    
    public void setOtherState(String key, Object state){
        this.otherStates.put(key,state);
    }
}
