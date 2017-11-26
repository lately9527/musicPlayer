package com.kevin.action;

import com.kevin.domain.Product;
import com.kevin.service.ProductService;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;


public class ProductAction extends ActionSupport implements ModelDriven<Product> {
	//模型驱动要使用的类
	Product product = new Product();
	
	@Override
	public Product getModel() {
		// TODO Auto-generated method stub
		return product;
	}
	
	
	private ProductService productService;

	//按名称自动注入业务层   spring，strusts plugin
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public String save(){
		System.out.println("ProductAction........");
		System.out.println("ProductAction: "+product.getPname()+" "+product.getPrice());
		productService.save(product);
		return NONE;
	}
	

}
