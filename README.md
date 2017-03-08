# 设计思路

1. LifeCycle的具体实现
    - 注解：
    在注解方面，目前实现了@LifeCycle注解，该注解用于修饰方法，当一个项目被启动后，Spring的扫描功能可以将所有具有该注解的方法执行过程剥离，交由自定义的AOP容器管理
    
    - 状态机：
    利用注解中的属性值，可以设定不同的状态字段，以此形成状态机之间的转换功能。调用方法的目的就是将待操作实体的状态进行变换。因此，不论需求如何变化，通过实现状态机的拓展，就能够针对不同操作，通过修改语法前缀的方式，形成操作记录。

    - 错误捕捉与回滚：
    Spring 提供的 AOP 操作会一层一层的递归。如果一个 Exception 在被监控操作中捕捉，该错误无法被 AOP 容器捕捉（当然，也不需要被捕捉）。但是，重新抛出、在方法处定义抛出而不直接捕获，都能够让AOP 容器针对该错误进行定位。也就是说，如果出现无法在方法中解决的错误，Spring 容器可以定位该错误。目前尚不清楚能否直接递归驳回错误，但是可以肯定的是，能够终止请求链，并返回错误处理结果

1. AOP的具体实现
    - LifeCycle的定义：
    
    ```java
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LifeCycle
    {
        public String value();
    }
    ```

    该注解需要在运行时中被识别，因此需要声明运行状态。由于尚不确定生命周期的最终形态，目前不限定可以标注的位置（虽然默认为只能标注在 method 级别）。
    由于 Spring AOP 容器是递归拦截，所以在遇到一些非重点方法时，仅捕捉错误信息，而不需要针对执行状态进行捕获，因此该注解的另一个作用在于定义真正需要影响生命周期的操作。对此可能在后期增加属性值 `target` 来进行功能增强

    - AOP 容器的捕获能力：

        - 运行实例的定位：
            ```java 
            @Value("${eureka.instance.hostname}")
            private String hostName;
            
            @Value("${server.port}")
            private String serverPort;
            ```
        在运行spring cloud之前，Spring boot 需要配置一些启动属性，而定位一个 Spring 运行实例的方式可以 Hostname + Port 的形式，定位至进程在运行环境中的唯一地址，通过`@Value`注解，可以获取配置文件中的值。由此在记录时可以保证记录到运行实例的唯一地址。该记录可以通过配置文件进一步拓展

        - 切点定义

            - 在 Spring 3.x 之后，切点的定义已经不会对源文件产生破坏。除了 XML 配置方式外，利用注解，可以让 Spring 容器在开始时对固定文件进行扫描。
            
            ```java
            @Pointcut("execution("
                + "* edu.uestc.msstudio.cloud..*(..))"
                + "and "
                + "@annotation(edu.uestc.msstudio.cloud.userservice.listener.LifeCycle)")
            public void cutPointDefine(){
                /* this method need no implement*/	
            }       
            ```
            通过定义该空方法，可以集中维护需要扫描的包，具体语法仍在研究中，不过 demo 中的语句已经能够扫描固定包下所有具有@LifeCycle注解的类 （下一步的计划是能够通过ProjectRoot的形式扫描指定包下所有类，即自动配置）

        - 捕捉类型：
            1. 执行类实例
         
            JoinPoint 提供了 getThis 和 getTarget 方法，可以捕获当前运行实例，包括其中的运行信息等
            
            2. 方法
         
            JoinPoint 可以捕获方法签名，利用方法签名可以获取到我们需要的运行实例中方法的固定信息，包括方法的定义、注解内容以及传入参数
            
            ```java

            String methodName = pjp.getSignature().getName();
			Class<?>[] paramTypes = ((MethodSignature)pjp.getSignature()).getMethod().getParameterTypes();
			
			Method targetMethod = pjp.getTarget().getClass().getMethod(methodName,paramTypes);
			
            ```
            3. 传入参数
           
            JoinPoint 提供了 getArgs 方法，可以捕获运行时的参数并返回一个 Object[] ，demo 中利用 Arrays 本身提供的 toString(); 方法可以获取到传入参数的文本信息并打印在logger中。
            
            ```java
            result = pjp.proceed();
            logger.info("Action : " + targetMethod.getAnnotation(LifeCycle.class).value());
            logger.info("done by : "+pjp.getThis().getClass().getTypeName());//获取拦截对象
            logger.info("request using args:"+Arrays.toString(pjp.getArgs()));
            logger.info("Happend In Instance: ("+hostName+":"+serverPort+")");
            return result;
            ```

            4. 错误捕获
           
            JoinPoint 的 proceed 方法会继续原方法的请求，在此过程中，一切未被解决的错误都会被捕捉，但是无法捕获已经被 catch 的错误。如果又需要，可以通过 rethrow的方式通过容器，也可以直接使用 throws 方法，将错误处理集中交给 AOP 容器进行处理，可以避免编程人员在编码时产生的疏忽。

            5. 返回值修改
           
            被捕获值得返回值将交由 AOP 容器重新生成，proceed 方法会返回一个 Object 对象，该对象代表正常执行方法时的期望返回。如果出现错误处理等情况需要由 AOP 容器重新生成值，那么拦截方法里的返回值将作为目标返回值重新传递，需要注意的是，在该过程中无法保证返回结果是否是预期结果，推荐在捕获 method 之后，利用反射方式获取期望返回值类型并返回该类型的信息。

            ```java
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
                        logger.info("Action : " + targetMethod.getAnnotation(LifeCycle.class).value());
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
            ```

3. Demo 的使用方法

在 Spring IDE (Eclipse) 中启动项目，

- 尝试 get /test?testArg=目标参数 可以看到正常运行状态下的输出信息
- 尝试 get /test2 可以看到未处理异常的输出信息
