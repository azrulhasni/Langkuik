/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.view;

import com.azrul.langkuik.loanorigsystem.model.RelationshipManager;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.table.TableView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 *
 * @author azrul
 */
@Route(value = "relationmanager", layout = MainView.class)
@PageTitle("RelationManager")
//@CssImport("styles/views/masterdetail/master-detail-view.css")
public class RelationshipManagerMDV extends TableView<RelationshipManager>{
    public RelationshipManagerMDV(){
        super(RelationshipManager.class, TableView.Mode.MAIN);
    }
}
