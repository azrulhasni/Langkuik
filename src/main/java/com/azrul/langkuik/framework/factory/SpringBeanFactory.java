/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.factory;

import com.azrul.langkuik.views.main.*;
import com.azrul.langkuik.views.pojo.PojoView;
import com.vaadin.flow.server.VaadinServlet;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author azrul
 */
public class SpringBeanFactory {
    public static <T> T create(Class<T> tclass){
        ApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(VaadinServlet.getCurrent().getServletContext());
        AutowireCapableBeanFactory factory = springContext.getAutowireCapableBeanFactory();
        return factory.createBean(tclass);
    }
}
