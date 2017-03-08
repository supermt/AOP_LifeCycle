
package edu.uestc.msstudio.cloud.userservice.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_detail")
public class UserDetail
{
	@Id
	private Long id;
	
	private int age;
	
	public Long getId()
	{
	
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public int getAge()
	{
	
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}
}
 
