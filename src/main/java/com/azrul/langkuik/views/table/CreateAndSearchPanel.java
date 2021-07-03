/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.table;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;

/**
 *
 * @author azrul
 */
public class CreateAndSearchPanel extends Div {
    private final Button btnCreateNew;
    private final TextField tfSearchString;
    private final Button btnSearch;
    //private ComponentEventListener listener;

    public CreateAndSearchPanel(String caption, Boolean enableCreate) {
        tfSearchString = new TextField();
        tfSearchString.setWidth("80%");
        tfSearchString.setPlaceholder(caption);
        btnSearch = new Button();
        btnSearch.setText(caption);
        btnCreateNew = new Button("Create new");
        if (enableCreate){
            add(tfSearchString, btnSearch, btnCreateNew);
        }else{
            add(tfSearchString, btnSearch);
        }
    }

    public TextField getTextField() {
        return tfSearchString;
    }

    public Button getButton() {
        return btnSearch;
    }
    
    public String getValue(){
        return tfSearchString.getValue();
    }

   

    /**
     * @param listener the listener to set
     */
    public void setSearchListener(ComponentEventListener listener) {
        btnSearch.addClickListener(listener);
        //tfSearchString.addKeyPressListener(listener);
    }
    
    public void setCreateListener(ComponentEventListener listener){
        btnCreateNew.addClickListener(listener);
    }
}
