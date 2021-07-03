/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.view;

import com.azrul.langkuik.loanorigsystem.model.Application;
import com.azrul.langkuik.loanorigsystem.model.Product;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.table.TableView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 *
 * @author azrul
 */
@Route(value = "product", layout = MainView.class)
@PageTitle("Product")
//@CssImport("styles/views/masterdetail/master-detail-view.css")
public class ProductMDV extends TableView<Product>{
    public ProductMDV(){
        super(Product.class, TableView.Mode.MAIN);
    }
}
