package com.webserver.http;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  当前类保存所有与HTTP协议相关的规定内容以便重用
 */
public class HttpContext {
    /**
     * 资源后缀名与响应头Content-Type值的对应关系
     * Key：资源后缀名
     * value：Content-Type对应的值
     */
    private static Map<String, String> mimeMapping = new HashMap<>();

    static {
        initMimeMapping();

    }


    private static void initMimeMapping() {
//        mimeMapping.put("html","text/html");
//        mimeMapping.put("css","text/css");
//        mimeMapping.put("js","application/javascript");
//        mimeMapping.put("png","image/png");
//        mimeMapping.put("gif","image/gif");
//        mimeMapping.put("jpg","image/jpeg");
        try{
            SAXReader reader =new SAXReader();
            Document doc = reader.read("./config/web.xml");
            Element root = doc.getRootElement();
            List<Element> list = root.elements("mime-mapping");

            for (Element mimeEle : list){
                String key = mimeEle.elementText("extension");
                String value =mimeEle.elementText("mime-type");
                mimeMapping.put(key,value);

            }
            System.out.println(mimeMapping.size());



        }catch (Exception e){
            e.printStackTrace();
        }
    }

        public static String getMimeType (String ext){
            return mimeMapping.get(ext);
        }

    public static void main(String[] args) {
        String type = getMimeType("png");
        System.out.println(type);
    }


}