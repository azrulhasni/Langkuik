/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.standard;

/**
 *
 * @author azrul
 */
public class Rules {
    public static boolean canBeDeleted(LangkuikExt bean, String username){
        if (bean==null || username==null){
            return false;
        }
        if (Status.DRAFT.equals(bean.getStatus()) &&  username.equals(bean.getOwnerId())){
            return true;
        }else{
            return false;
        }
        
    }
}
