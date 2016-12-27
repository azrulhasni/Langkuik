/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework;

import com.vaadin.ui.Layout;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.persistence.EntityManagerFactory;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;

/**
 *
 * @author azrulhasni
 */
public class PageParameter implements Serializable{
    private final List<DataAccessObject<?>> customTypeDaos;
    private final EntityManagerFactory entityManagerFactory;
    private final RelationManagerFactory relationManagerFactory;
    private final Deque<History> history;
    private final Configuration config;
    private Layout breadcrumb;
    private final ResourceBundle resourceBundle;
    private final Locale locale;
    private Class rootClass;
    private WorkType type;

    public PageParameter(List<DataAccessObject<?>> customTypeDaos, 
            EntityManagerFactory emf, 
            RelationManagerFactory rmf, 
            Deque<History> history, 
            Configuration config, 
            Layout breadcrumb,
            ResourceBundle resourceBundle) {
        this.customTypeDaos = customTypeDaos;
        this.entityManagerFactory = emf;
        this.relationManagerFactory = rmf;
        this.history = history;
        this.config = config;
        this.breadcrumb = breadcrumb;
        this.resourceBundle=resourceBundle;
        this.locale =resourceBundle!=null?resourceBundle.getLocale():new Locale("en");
        this.rootClass=null;
    }
    
    
    
    public PageParameter(List<DataAccessObject<?>> customTypeDaos, 
            EntityManagerFactory emf, 
            RelationManagerFactory rmf, 
            Deque<History> history, 
            Configuration config, 
            Layout breadcrumb) {
        this.customTypeDaos = customTypeDaos;
        this.entityManagerFactory = emf;
        this.relationManagerFactory = rmf;
        this.history = history;
        this.config = config;
        this.breadcrumb = breadcrumb;
        
        this.locale = new Locale("en");
        this.resourceBundle = ResourceBundle.getBundle("Text", locale);
        this.rootClass=null;
    }
    
    public PageParameter(List<DataAccessObject<?>> customTypeDaos, 
            EntityManagerFactory emf, 
            RelationManagerFactory rmf, 
            Deque<History> history, 
            Configuration config) {
        this.customTypeDaos = customTypeDaos;
        this.entityManagerFactory = emf;
        this.relationManagerFactory = rmf;
        this.history = history;
        this.config = config;
        
        this.locale = new Locale("en");
        this.resourceBundle = ResourceBundle.getBundle("Text", locale);
        this.rootClass=null;
    }
    
    public void setBreadcrumb(Layout breadcrumb){
        this.breadcrumb = breadcrumb;
    }
    
    public String getLocalisedText(String key){
         try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "";
        }
    }
    
    
    public String getLocalisedText(String key, String... params){
        String text;
    
        try {
            text = resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            text = "";
        }

        if (params != null) {
            MessageFormat mf = new MessageFormat(text, locale);
            text = mf.format(params, new StringBuffer(), null).toString();
        }

        return text;

    }
    
    public String getLocalisedText(String key, int... params){
        String text;
    
        try {
            text = resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            text = "";
        }

        if (params != null) {
            String[] strParams = new String[params.length];
            for (int i=0;i<params.length;i++){
                strParams[i]=Integer.toString(params[i]);
            }
            MessageFormat mf = new MessageFormat(text, locale);
            text = mf.format(strParams, new StringBuffer(), null).toString();
        }

        return text;

    }
    
    
    /**
     * @return the customTypeDaos
     */
    public List<DataAccessObject<?>> getCustomTypeDaos() {
        return customTypeDaos;
    }

    /**
     * @return the emf
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * @return the rmf
     */
    public RelationManagerFactory getRelationManagerFactory() {
        return relationManagerFactory;
    }

    /**
     * @return the history
     */
    public Deque<History> getHistory() {
        return history;
    }

    /**
     * @return the config
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @return the breadcrumb
     */
    public Layout getBreadcrumb() {
        return breadcrumb;
    }

    /**
     * @return the resourceBundle
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * @return the rootClass
     */
    public Class getRootClass() {
        return rootClass;
    }

    /**
     * @param rootClass the rootClass to set
     */
    public void setRootClass(Class rootClass) {
        this.rootClass = rootClass;
    }

    /**
     * @return the type
     */
    public WorkType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(WorkType type) {
        this.type = type;
    }
 
}
