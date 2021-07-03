/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import org.hibernate.search.bridge.StringBridge;

/**
 *
 * @author azrul
 */


public class Integer2StringBridge implements StringBridge {

    public String objectToString(Object object) {
        if (object==null){
            return null;
        }
        return ( (Integer) object ).toString();
    }
}