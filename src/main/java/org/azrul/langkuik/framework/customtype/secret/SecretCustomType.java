/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.customtype.secret;

import org.azrul.langkuik.framework.customtype.attachment.*;
import java.util.Date;
import org.azrul.langkuik.framework.customtype.CustomType;

/**
 *
 * @author azrulm
 */

public interface SecretCustomType extends CustomType{

    public String getHashedPassword();

    public void setHashedPassword(String hashedPassword);

    public String getSalt();

    public void setSalt(String salt);
    
    @Override
    String toString();
}
