/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.configs;

import com.vaadin.server.VaadinService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azrulm
 */
public class Configuration implements Serializable {

    String propertiesFileName = "config.properties";
    Map<String, String> readProperties;
    private static Configuration configuration = null;

    public Configuration() {
        FileInputStream fis = null;
        try {
            String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
            Properties props = new Properties();
            fis = new FileInputStream(new File(basepath + "/WEB-INF/" + propertiesFileName));
            props.load(fis);
            readProperties = new HashMap<>();
            for (Map.Entry entry : props.entrySet()) {
                readProperties.put((String) entry.getKey(), (String) entry.getValue());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static synchronized Configuration getInstance() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }

    public String get(String key) {
        if ("dateFormat".equals(key) && readProperties.get(key) == null) {
            //dirty way to get locale date format
            final DateFormat dateInstance
                    = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            if (dateInstance instanceof SimpleDateFormat) {
                final String pattern = ((SimpleDateFormat) dateInstance).toPattern();
                return pattern;
            }else{
                return "yyyy-MM-dd";
            }
        }else  if ("dateTimeFormat".equals(key) && readProperties.get(key) == null) {
            //dirty way to get locale date format
            final DateFormat dateInstance
                    = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            if (dateInstance instanceof SimpleDateFormat) {
                final String pattern = ((SimpleDateFormat) dateInstance).toPattern();
                return pattern;
            }else{
                return "yyyy-MM-dd hh:mm:ss";
            }
        } 
        else {
            return readProperties.get(key);
        }
    }

}
