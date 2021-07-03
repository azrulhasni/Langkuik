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
public enum Status {
    
    DRAFT("Draft"),
    IN_PROGRESS("In progress"),
    DONE("Done"),
    DELETED("Deleted"),
    PREDRAFT("Pre-draft"),
    WAITING("Waiting"),
    IN_PROGRESS_ESCALATED("Escalated");

    private String status;

    private Status(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }

}
