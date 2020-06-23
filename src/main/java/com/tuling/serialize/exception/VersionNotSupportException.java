package com.tuling.serialize.exception;

import com.tuling.serialize.util.Constant;

/***
 * @author lujintao
 * @date 2020-06-23
 */
public class VersionNotSupportException extends RuntimeException{


    public VersionNotSupportException(String msg){
        super(msg);
    }

    public VersionNotSupportException(Exception ex){
        super(ex);
    }

    public VersionNotSupportException(String msg, Exception ex){
        super(msg,ex);
    }
}
