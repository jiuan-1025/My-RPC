package top.guoziyang.rpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.guoziyang.rpc.annotation.Service;
import top.guoziyang.rpc.annotation.ServiceScan;
import top.guoziyang.rpc.enumeration.RpcError;
import top.guoziyang.rpc.exception.RpcException;
import top.guoziyang.rpc.provider.ServiceProvider;
import top.guoziyang.rpc.registry.ServiceRegistry;
import top.guoziyang.rpc.util.ReflectUtil;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author ziyang
 */
public abstract class AbstractRpcServer implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

//    扫描服务
    public void scanServices() {
//        获取程序入口文件
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
//            判断是否包含 @ServiceScan 注解
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)) {
//            获取包路径
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }

//        获取所有的类文件
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
//            判断是否有 @Service 注解
            if(clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
//                    实例化对象
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
//                        发布服务 添加到本地Map中记录 接口路径名称 -> 实例化对象
//                                添加到nacos服务器  接口路径名称 -> 对应的服务器地址
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj , serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        //添加到本地Map中记录
        serviceProvider.addServiceProvider(service, serviceName);
        //添加到nacos服务器中
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}
