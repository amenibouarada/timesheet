package com.aplana.timesheet.exception.controller;

/**
 * User: vsergeev
 * Date: 18.01.13
 */
public class BusinessTripsAndIllnessControllerException extends Exception {

    public BusinessTripsAndIllnessControllerException(String message){
        super(message);
    }

    public BusinessTripsAndIllnessControllerException(String message, Throwable th) {
        super(message, th);
    }
}
