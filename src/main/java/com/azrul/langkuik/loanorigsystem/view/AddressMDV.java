/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.view;

import com.azrul.langkuik.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.azrul.langkuik.loanorigsystem.model.Address;
import com.azrul.langkuik.loanorigsystem.model.Applicant;
import com.azrul.langkuik.views.table.TableView;
import com.vaadin.flow.router.PreserveOnRefresh;

/**
 *
 * @author azrul
 */

@Route(value = "address", layout = MainView.class)
@PageTitle("Address")
//@CssImport("styles/views/masterdetail/master-detail-view.css")
public class AddressMDV extends TableView<Address> {

    public AddressMDV() {
        super(Address.class, TableView.Mode.MAIN);
        
    }

    
}
