package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示客户端发送过来的一个HTTP请求内容
 * 每个请求由三部分构成：
 * 请求行，消息头，消息正文
 */
public class HttpRequest {
    //请求行相关信息
    private String method;//请求方式
    private String uri;//抽象路径
    private String protocol;//协议版本

    private String requestURI;//存抽象路径中的请求部分，即：uri中？左侧的内容
    private String queryString;//存抽象路径中的参数部分，即：uri中？右侧的内容
    private Map<String,String> parameter = new HashMap<>();//存每一组参数


    //消息头相关信息
    private Map<String,String> headers = new HashMap<>();
    //消息正文相关信息


    private Socket socket;

    /**
     * HttpRequest的实例化过程就是解析请求的过程
     *
     */
    public HttpRequest(Socket socket) throws EmptyRequestException {
        this.socket = socket;
        //1解析请求行
        parseRequestLine();
        //2解析消息头
        parseHeaders();
        //3解析消息正文
        parseContenet();


    }



    //解析一个请求的三步骤
    //1：解析请求行
    private void parseRequestLine() throws EmptyRequestException {
        System.out.println("HttpRequest:开始解析请求行...");
        try{
            String line = readLine();
            if (line.isEmpty()){//请求行如果是空字符串则是空请求
                throw new EmptyRequestException();
            }


            System.out.println("请求行："+line);
            //http:localhost:8088/index.html
            //将请求行按照空格拆分为三部分，并分别赋值给上述变量

            String[] data = line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol = data[2];
            parseUri();//解析请求行的三部分之后，对uri抽象路径部分进一步拆分
            System.out.println("method："+method);//method:GET
            System.out.println("uri："+uri);//uri:/index.html
            System.out.println("protocol："+protocol);//protocol:HTTP/1.1
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("HttpRequest:请求行解析完毕！！！");
    }

    //进一步解析消息头
    private void parseUri(){
        System.out.println("HttpEquest：进一步解析消息头uri....");
        /*
            uri会存在两种情况：含有参数和不含有参数
            不含有参数的样子如：/myweb/index.html
            含有参数的样子如：/myweb/regUser?username=xxx&password=xxx...
            如果uri不含有参数，则不需要拆分，直接将uri的值赋值给requestURI即可。

            如果uri含有参数，则需要进行拆分：
            1：将uri按照"?"拆分为两部分，左侧赋值给requestURI，右侧赋值给queryString
            2：在将queryString部分按照"&"拆分出每一组参数，然后每一组参数再按照"="拆分为
                参数名和参数值，并将参数名作为key,参数值作为value保存到parameters这个Map中
                完成解析工作
         */

        if(uri.contains("?")){
            String[] data = uri.split("\\?");
            requestURI = data[0];
            // http://localhost:8088/myweb/regUser?
            if (data.length>1){
                queryString = data[1];
                //username=fancq&password=123456&nickname=chuanqi&age=22
                //拆分每一组参数
                data = queryString.split("&");
                for (String para : data){
                    //username=fancq
                    //按照=拆分参数名与参数值
                    String[] paras = para.split("=");
                    if (paras.length>1){
                        parameter.put(paras[0],paras[1]);
                    }else {
                        parameter.put(paras[0],null);
                    }
                }
            }
        }else{
            requestURI = uri;
        }



        System.out.println("requestRUI:"+requestURI);
        System.out.println("queryString:"+queryString);
        System.out.println("parameter:"+parameter);
        System.out.println("HttpRequest:进一步解析uri完毕！");
    }

    //2：解析消息头
    private void parseHeaders(){
        System.out.println("HttpRequest:开始解析消息头...");
        try{
            //下面读取每一个消息头后，将消息头的名字作为key，消息头的值作为value保存到headers中
            while (true) {
                String line = readLine();
                //读取消息头时如果只读取到了回车加换行就应当停止读取
                if ((line.isEmpty())) { //readLine单独读取CRLF返回值应当是空字符串
                    break;
                }
                System.out.println("消息头：" + line);
                //将消息头按照冒号空格拆分并存入到headers这个Map中保存
                String[] data = line.split(":\\s");
                headers.put(data[0],data[1]);
            }
            System.out.println("headers:"+headers);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("HttpRequest:消息头解析完毕！！！");
    }
    //3：解析消息正文
    private void parseContenet(){
        System.out.println("HttpRequest:开始解析正文...");

        System.out.println("HttpRequest:正文解析完毕！！！");
    }




    private String readLine() throws IOException {
        /*
            当socket对象相同时，无论调用多少次getInputStream方法，获取回来的输入流
            总是同一个流，输出流也是一样的。
         */
        InputStream in = socket.getInputStream();
        int d;
        char cur =' ';//表示本次读取到的字符
        char pre =' ';//表示上次读取到的字符
        StringBuilder builder = new StringBuilder();//表示读取到的所有字符
        while ((d = in.read())!=-1){
            cur = (char)d;//本次读取到的字符
            //如果上次读取到的是回车符，本次读取到的是换行符则停止读取
            if(pre==13 && cur==10){
                break;
            }
            builder.append(cur);
            pre = cur;
        }
        return builder.toString().trim();
    }


    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 根据消息头的名字获取应用消息头的值
     * @param name
     * @return
     */

    public String getHeaders(String name) {
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 根据参数名获取参数值
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return parameter.get(name);
    }
}
