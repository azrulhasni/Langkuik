/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik;

import com.azrul.langkuik.framework.dao.DataAccessObject;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author azrul
 */
@Component
public class MassIndexer {
     @Autowired
    private DataAccessObject dao;
 
    @PostConstruct
    public void init() {
        dao.massIndex();
    }
}
