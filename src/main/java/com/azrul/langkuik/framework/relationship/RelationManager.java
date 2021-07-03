/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.framework.relationship;

import java.util.List;
import javax.persistence.EntityManager;



/**
 *
 * @author azrulm
 */
public interface RelationManager<P,C> {
    void link(P parentObject, C currentObjects, String parentToCurrentField);
    void unlink(P parentObject, C currentObject, String parentToCurrentField); //return old object which was linked to parent before
}
