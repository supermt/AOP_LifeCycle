package edu.uestc.msstudio.cloud.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uestc.msstudio.cloud.userservice.dao.UserRepository;
import edu.uestc.msstudio.cloud.userservice.listener.LifeCycle;

@RestController
@RequestMapping("/")
public class UserController {
  @Autowired
  private UserRepository userRepository;
  
  @GetMapping("test")
  @LifeCycle(action = "", souce = "", target = "")
  public Object count(@RequestParam String test){
	  return userRepository.count();
  }

  @GetMapping("test2")
  @LifeCycle(action = "", souce = "", target = "")
  public Object count(){
	  return userRepository.throwError();
  }
}