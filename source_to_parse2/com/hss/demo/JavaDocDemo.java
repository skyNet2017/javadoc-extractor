package com.hss.demo;
import java.lang.String;

public class JavaDocDemo {


    /**  @value 如果是基本变量类型或者string,都不要用引号,双引号包裹,如果是json,那么直接裸写json, 不要双引号化
     *  跳转到订单页面
     * @param orderId  订单id   @value 8908
     * @param fromPage 从哪个页面进入的   @value h5activity
     * @return 是否成功跳转
     */
    public boolean toOrder(long orderId,String fromPage){
        return false;
    }
}
