package com.campus.campus_life.utils;

/**
 * 统一返回结果
 */
public class Result {
    private String code;
    private String msg;
    private Object data;

    // 成功（无数据）
    public static Result success() {
        Result r = new Result();
        r.setCode("200");
        r.setMsg("success");
        return r;
    }

    // 成功（带数据）
    public static Result success(Object data) {
        Result r = new Result();
        r.setCode("200");
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    // 失败
    public static Result error(String msg) {
        Result r = new Result();
        r.setCode("500");
        r.setMsg(msg);
        return r;
    }

    // getter / setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}

