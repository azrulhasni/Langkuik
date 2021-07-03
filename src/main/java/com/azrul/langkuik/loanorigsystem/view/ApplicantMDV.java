/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.view;

import com.azrul.langkuik.loanorigsystem.model.Applicant;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.table.TableView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 *
 * @author azrul
 */
@Route(value = "applicant", layout = MainView.class)
@PageTitle("Applicant")
//@CssImport("styles/views/masterdetail/master-detail-view.css")
public class ApplicantMDV extends TableView<Applicant>{
    public ApplicantMDV() {
        super(Applicant.class, TableView.Mode.MAIN);
    }
}
