
package edu.uestc.msstudio.cloud.userservice.dao.impl;

import org.springframework.stereotype.Component;

import edu.uestc.msstudio.cloud.userservice.dao.UserRepository;

@Component
public class UserDaoImpl implements UserRepository
{
	@Override
	public String count()
	{
		String test = "1 IS FOR A START";
		try{
			char result  = test.charAt(100);
			return String.valueOf(result);	
		}catch(Exception e){
			return "Error Happened";
		}
		
	}

	 
	@Override
	public char throwError()
	{
		String test = "1 IS FOR A START";
		return test.charAt(100);
		
	}

}
 
