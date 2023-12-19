package com.cmg.mail.controller.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = -2013432965228651654L;
    /**
     * 通信数据
     */
    private T result;
    /**
     * 状态
     */
    private boolean flag = false;
    /**
     * 状态码
     */
    private Integer code;
    /**
     * 描述
     */
    private String msg = "";


    public static <T> JsonResult<T> of(T result) {
        return new JsonResult<>(result);
    }

    public static <T> JsonResult<T> success(T result) {
        return new JsonResult<>(result, ErrorCode.SUCCESS.getFlag(),
                ErrorCode.SUCCESS.getCode()
                ,ErrorCode.SUCCESS.getMessage());
    }

    public static <T> JsonResult<T> success(T result,String msg) {
        return new JsonResult<>(result, ErrorCode.SUCCESS.getFlag(),
                ErrorCode.SUCCESS.getCode()
                ,msg);
    }

    public static <T> JsonResult<T> error(T result) {
        return new JsonResult<>(result, ErrorCode.ERROR.getFlag(),
                ErrorCode.ERROR.getCode()
                ,ErrorCode.ERROR.getMessage());
    }

    public static <T> JsonResult<T> error(T result, Integer code, String msg) {
        return new JsonResult<>(result, ErrorCode.ERROR.getFlag(), code, msg);
    }

    public static <T> JsonResult<T> of(T result, boolean flag) {
        return new JsonResult<>(result, flag);
    }

    public static <T> JsonResult<T> of(T result, boolean flag, String msg) {
        return new JsonResult<>(result, flag, msg);
    }

    public static <T> JsonResult<T> of(T result, boolean flag, Integer code, String msg) {
        return new JsonResult<>(result, flag, code, msg);
    }


    @Deprecated
    public JsonResult() {

    }

    private JsonResult(T result) {
        this.result = result;
    }

    private JsonResult(T result, boolean flag) {
        this.result = result;
        this.flag = flag;
    }

    private JsonResult(T result, boolean flag, String msg) {
        this.result = result;
        this.flag = flag;
        this.msg = msg;
    }

    private JsonResult(T result, boolean flag, Integer code, String msg) {
        this.result = result;
        this.flag = flag;
        this.code = code;
        this.msg = msg;
    }

}
