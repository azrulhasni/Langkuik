/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom;

import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public class VoidCustomComponentRenderer implements CustomComponentRenderer {

    @Override
    public Optional renderInFormAsDependency(Object parent, String relationName, VerticalLayout layout, Map relationMementos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
