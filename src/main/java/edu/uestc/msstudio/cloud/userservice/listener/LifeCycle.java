package edu.uestc.msstudio.cloud.userservice.listener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LifeCycle
{
	public String action();//操作描述
	
	public String souce();//源状态
	
	public String target();//目标状态

}
 
