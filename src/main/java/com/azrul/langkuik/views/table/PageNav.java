/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.table;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 *
 * @author azrul
 */
public class PageNav extends HorizontalLayout {

    private Button firstPage;
    private Button finalPage;
    private Button nextPage;
    private Button lastPage;
    private Label currentPage;

    public PageNav() {
        firstPage = new Button("<<");
        finalPage = new Button(">>");
        nextPage = new Button(">");
        lastPage = new Button("<");
        currentPage = new Label();
        currentPage.getStyle().set("font-size","12px");
        currentPage.getStyle().set("line-height", "4");
        add(firstPage);
        add(lastPage);
        add(currentPage);
        currentPage.setText("0");
        add(nextPage);
        add(finalPage);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    }

    public void setPage(Integer page, Integer maxPageCount) {
        if (page == 1) {
            this.getFinalPage().setEnabled(true);
            this.getFirstPage().setEnabled(false);
            this.getNextPage().setEnabled(true);
            this.getLastPage().setEnabled(false);
        } else if (page == maxPageCount) {
            this.getFinalPage().setEnabled(false);
            this.getFirstPage().setEnabled(true);
            this.getNextPage().setEnabled(false);
            this.getLastPage().setEnabled(true);
        } else {
            this.getFinalPage().setEnabled(true);
            this.getFirstPage().setEnabled(true);
            this.getNextPage().setEnabled(true);
            this.getLastPage().setEnabled(true);

        }
        this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(maxPageCount));
    }

    /**
     * @return the firstPage
     */
    public Button getFirstPage() {
        return firstPage;
    }

    /**
     * @return the finalPage
     */
    public Button getFinalPage() {
        return finalPage;
    }

    /**
     * @return the nextPage
     */
    public Button getNextPage() {
        return nextPage;
    }

    /**
     * @return the lastPage
     */
    public Button getLastPage() {
        return lastPage;
    }

 

}
