package com.kevin.domain;

/**
 * 产品实体
 * @author 李曜铮
 *
 */
public class Product {

	private  Integer pid;
	private String pname;
	private Double price;
	
	public Product() {
		super();
	}
	
	public Product(Integer pid, String pname, Double price) {
		super();
		this.pid = pid;
		this.pname = pname;
		this.price = price;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public String getPname() {
		return pname;
	}
	public void setPname(String pname) {
		this.pname = pname;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
}
