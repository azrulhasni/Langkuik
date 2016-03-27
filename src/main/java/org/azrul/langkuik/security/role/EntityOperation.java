/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.security.role;
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
         Configuration config = new Configuration();
         return config.get("security.operation."+this.name());
    }
}
