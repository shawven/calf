package com.example.nativepractice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

@Data
public class Entity implements Serializable {

	private static final long serialVersionUID = -7901272806027260031L;

	public static final String ID = "_id";
	public static final String DELETE = "delete";

	@Id
	private String id;

    protected String creator;

    protected String updater;

	protected Date createTime;

    protected Date updateTime;

    protected boolean delete;
}
