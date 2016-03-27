/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.webgui.breadcrumb;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import java.util.Deque;
import java.util.Iterator;

/**
 *
 * @author azrulm
 */
public class BreadCrumbBuilder {
  
    public static void buildBreadCrumb(final Navigator navigator,final Layout breadcrumb,final Deque<History> history) {
        ((HorizontalLayout)breadcrumb).removeAllComponents();
        
        for (Iterator<History> it=history.descendingIterator();it.hasNext();){
            final History h = it.next();
            Button button = new Button(h.getDisplayName()+" >", new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                   while (!h.getViewHandle().equals(history.peek().getViewHandle())){
                       history.pop();
                   }
                   navigator.navigateTo(h.getViewHandle());
                }
            });
            breadcrumb.addComponent(button);
        }
    }
}
