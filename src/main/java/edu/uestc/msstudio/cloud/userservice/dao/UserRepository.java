package edu.uestc.msstudio.cloud.userservice.dao;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {

	String count();
    
	char throwError();
}