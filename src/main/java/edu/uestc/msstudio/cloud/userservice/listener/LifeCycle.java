package edu.uestc.msstudio.cloud.userservice.listener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LifeCycle
{
	public String value();
}
 
