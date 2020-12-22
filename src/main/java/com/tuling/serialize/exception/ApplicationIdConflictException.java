package com.tuling.serialize.exception;

/**
 * @author lujintao
 * @date 2020oo-12-22
 */
public class ApplicationIdConflictException extends RuntimeException{


    public ApplicationIdConflictException(){
        super("This application's id is the same as another application,please restart this application.");
    }
}
