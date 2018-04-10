/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.customtype;


import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;
import org.azrul.langkuik.framework.webgui.BeanView;
import org.azrul.langkuik.framework.webgui.VerticalView;
import org.azrul.langkuik.security.role.FieldState;
/**
 *
 * @author azrulm
 */
public interface CustomTypeUICreator<C,W> {
    
     Component createUIForForm(
            final C currentBean, 
            final Class<? extends CustomType> customClass, 
            final String pojoFieldName, 
            final BeanView view, 
            final DataAccessObject<C,W> containerClassDao,
            final DataAccessObject<? extends CustomType, W> customTypeDao,
            final PageParameter<W> pageParameter,
            final FieldState componentState,
            final Window window);

}
