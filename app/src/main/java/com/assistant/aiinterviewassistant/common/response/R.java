package com.assistant.aiinterviewassistant.common.response;
import lombok.Data;

@Data
public class R<T> {
   private int code;
   private String msg;
   private T data;


   private final static int SUCCESS = 200;
   private final static int ERROR = 500;

    public static <T> R<T> ok(){
       R<T> r = new R<>();
       r.setCode(SUCCESS);
       return r;
   }
    public static <T> R<T> ok(int code ){
        R<T> r = new R<>();
        r.setCode(code);
        return r;
    }
    public static <T> R<T> ok(String meg ){
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setMsg(meg);
        return r;
    }
    public static <T> R<T> ok(int code,String meg ){
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(meg);
        return r;
    }
    public static <T> R<T> ok(T data ){
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail() {
        return fail(ERROR, "操作失败");
    }
    public static <T> R<T> fail(int code) {
        R<T> r = new R<>();
        r.setCode(code);
        return r;
    }
    public static <T> R<T> fail(String msg) {
        return fail(ERROR, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
