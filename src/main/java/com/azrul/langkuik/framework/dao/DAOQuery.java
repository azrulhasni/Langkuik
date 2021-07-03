/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.framework.dao;

import java.util.Collection;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author azrulm
 * @param <P>
 * @param <C>
 * @param <W>
 */
public interface DAOQuery<P,C> {
    Collection<C> doQuery(EntityManager em, 
            Optional<String> orderBy,
            Optional<Boolean> asc, 
            Optional<Integer> startIndex, 
            Optional<Integer> pageSize, 
            Optional<String> tenantId, 
            Optional<String> worklist);
    Long count(EntityManager em,  
            Optional<String> tenantId, 
            Optional<String> worklist);
}
