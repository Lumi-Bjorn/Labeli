package com.labeli.activities;

public class Product {
	
	private String code, name;
	private int count;
	private double price;
	
	public Product(String code, String name, int count){
		this.setCode(code);
		this.setName(name);
		this.setCount(count);
	}
	
	public Product(String code, String name, int count, double price){
		this.setCode(code);
		this.setName(name);
		this.setCount(count);
		this.setPrice(price);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}	
	
	
	
}