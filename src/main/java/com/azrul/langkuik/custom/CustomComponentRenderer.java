/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom;

import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public interface CustomComponentRenderer<T> {
    Optional<T> renderInFormAsDependency(T parent, String relationName, VerticalLayout layout,Map<String, RelationMemento> relationMementos);
   
}
