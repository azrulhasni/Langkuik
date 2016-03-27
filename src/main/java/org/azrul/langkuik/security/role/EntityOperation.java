/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.security.role;
import java.util.Locale;
import java.util.ResourceBundle;
import org.azrul.langkuik.configs.Configuration;

/**
 *
 * @author azrulm
 */
public enum EntityOperation {
    RESTRICTED,
    VIEW,
    CREATE_UPDATE,
    UPDATE,
    DELETE;

    @Override
    public String toString() {
          Locale locale = new Locale("en");
         ResourceBundle resourceBundle = ResourceBundle.getBundle("Text", locale);
         return resourceBundle.getString("security.operation."+this.name());
    }
}
