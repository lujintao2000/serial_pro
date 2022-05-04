//package com.tuling.serialize.util;
//
//
//import com.tuling.serialize.Builder;
//import org.apache.log4j.Logger;
//
//import java.io.File;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 定义与Builder相关的常用方法
// * @author lujintao
// * @date   2020-06-16
// */
//public class BuilderUtil {
//    private static final Logger LOGGER = Logger.getLogger(BuilderUtil.class);
//    private static final Map<Class,Builder> map = new HashMap<>();
//    private static final String DEFAULT_BUILDER_PACKAGE_NAME = "builder";
//
//    static{
//        String packageName = System.getProperty("builder.package");
//        if(packageName == null){
//            packageName = getDefaultPackageName();
//        }
//        if(packageName != null){
//            List<Class> list = getAllClass(packageName);
//            list.stream().forEach( x -> {
//                Builder builder = null;
//                try {
//                    builder = (Builder) x.newInstance();
//                }catch (Exception ex){
//                    LOGGER.error("Can't instantiate " + x, ex);
//                }
//                if(builder != null){
//                     map.put(builder.getType(), builder);
//                }
//
//            });
//        }
//    }
//
//    /**
//     * 获得指定Builder类的参数类型
//     * @param target
//     * @return
//     */
//    private static Class getParameterType(Class<Builder> target){
//        Type[] types = target.getGenericInterfaces();
//        ParameterizedType type = (ParameterizedType)types[0];
//        return (Class) type.getActualTypeArguments()[0];
//    }
//
//    /**
//     * 是否给特定的类指定了一个用于创建该类对象的Builder
//     * @param type
//     * @return
//     */
//    public static boolean isSpecifyBuilder(Class type){
//        return map.containsKey(type);
//    }
//
//    /**
//     * 获取指定类型的Builder
//     * @param type
//     * @return
//     */
//    public static Builder get(Class type){
//        return map.get(type);
//    }
//
//    /**
//     * 扫描指定包路径下，实现了Builder接口的所有类
//     * @param packageName
//     * @return
//     */
//    private static List<Class> getAllClass(String packageName){
//            List<Class> list = new ArrayList<>();
//            String basePath = String.class.getResource("/").getPath();
//            String path = (basePath.contains("test") ? basePath.replace("test-","") : basePath) +  getPathOfPackageName(packageName);
//            list = getAllClass(packageName,list,new File(path));
//        return list;
//    }
//
//    /**
//     * 获得指定目录及其子目录下的所有实现了Builder接口的类
//     * @param packageName  当前目录对应的包名
//     * @param list
//     * @param directory
//     * @return
//     */
//    private static List<Class> getAllClass(String packageName,List<Class> list,File directory){
//        if(directory.isDirectory()){
//            File[] files = directory.listFiles();
//            for(File file : files){
//                if(file.isDirectory()){
//                    getAllClass(packageName + "." + file.getName(), list, file);
//                }else{
//                    try {
//                        Class loadClass = BuilderUtil.class.getClassLoader().loadClass(packageName + "." + file.getName().substring(0,file.getName().length() - 6));
//                        if(Builder.class.isAssignableFrom(loadClass)){
//                            list.add(loadClass);
//                        }
//                    } catch (ClassNotFoundException e) {
//                       LOGGER.error(e.getMessage(),e);
//                    }
//
//                }
//            }
//        }
//
//
//        return list;
//    }
//
//    /**
//     * 获取Builder所在的包的包名，默认的包名最后一个包是builder
//     * @return
//     */
//    public static String getDefaultPackageName(){
//        String result = null;
//        String path = String.class.getResource("/").getPath();
//        path = path.contains("test") ? path.replace("test-","") : path;
//        File file = new File(path);
//        File[] files = file.listFiles();
//        for(File item : file.listFiles()){
//            if(item.isDirectory()){
//                String name = getDefaultPackageName(item.getName(), item);
//                if(name != null){
//                    result = name;
//                    break;
//                }
//            }
//        }
//        return result;
//    }
//
//    public static String getDefaultPackageName(String packageName,File directory){
//        String result = null;
//        for(File item : directory.listFiles()){
//            if(item.isDirectory()){
//                if(item.getName().equals(DEFAULT_BUILDER_PACKAGE_NAME)){
//                    result = packageName + "." + item.getName();
//                    break;
//                }else{
//                    result = getDefaultPackageName(packageName + "." + item.getName(), item);
//                    if(result != null){
//                        break;
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//    public static  void  main(String[] args){
//        String packageName = BuilderUtil.getDefaultPackageName();
//        System.out.println(packageName);
//    }
//
//    /**
//     * 获得指定包名对应的路径名
//     * @param packageName
//     * @return
//     */
//    private static String getPathOfPackageName(String packageName){
//        String result = packageName;
//        while(result.contains(".")){
//            result = result.replace(".", File.separator);
//        }
//        return result;
//    }
//}
