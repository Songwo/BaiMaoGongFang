package com.bmgf.po;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor//快速创建构造方法
@AllArgsConstructor
public class Result<T> {
    private Integer code;//业务状态吗 200-成功 1-失败
    private String msg;//提示信息
    private T data;//响应数据

    //快速返回操作成功响应结果（带响应数据）
    public static <E> Result<E> success(E data) {
        return new Result<>(200, "操作成功", data);
    }

    //快速返回操作成功的响应结果
    public static Result success() {
        return new Result(200, "操作成功", null);
    }

    public static <E> Result<E> error(String msg) {
        return new Result(1, msg, null);
    }
}
