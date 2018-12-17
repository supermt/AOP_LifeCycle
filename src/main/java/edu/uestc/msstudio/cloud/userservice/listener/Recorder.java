package edu.uestc.msstudio.cloud.userservice.listener;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class Recorder
{
	private static final Logger logger = LoggerFactory.getLogger(Recorder.class);
	
	@Value("${eureka.instance.hostname}")
	private String hostName;
	
	@Value("${server.port}")
	private String serverPort;
	
	@Pointcut("execution("
		+ "* edu.uestc.msstudio.cloud..*(..))"
		+ "and "
		+ "@annotation(edu.uestc.msstudio.cloud.userservice.listener.LifeCycle)")
	public void cutPointDefine(){
		/* this method need no implement*/	
	}
	
	
	//计时器
	@Around("cutPointDefine()")
	public Object inteceptor(ProceedingJoinPoint pjp){
		Object result = new Object();
		try
		{
			String methodName = pjp.getSignature().getName();
			Class<?>[] paramTypes = ((MethodSignature)pjp.getSignature()).getMethod().getParameterTypes();
			
			Method targetMethod = pjp.getTarget().getClass().getMethod(methodName,paramTypes);
			
			if (!targetMethod.isAnnotationPresent(LifeCycle.class)){
				//如果该对象未被LifeCycle注解，跳过行为记录
				result = pjp.proceed();
				return result;
			}else{
				result = pjp.proceed();
				logger.info("Action : " + targetMethod.getAnnotation(LifeCycle.class).action());
				logger.info("done by : "+pjp.getThis().getClass().getTypeName());//获取拦截对象
				logger.info("request using args:"+Arrays.toString(pjp.getArgs()));
				logger.info("Happend In Instance: ("+hostName+":"+serverPort+")");
				return result;
			}
		}
		catch (Throwable e)
		{
			logger.error("Exception",e);
			logger.info("Exception that function has not been soluted");
			return null;
		}
		
	}
	
}