package com.kevin.service;

import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.kevin.dao.ProductDao;
import com.kevin.domain.Product;

/**
 * 商品管理业务层
 * @author 李曜铮
 */
@Transactional
public class ProductService {
	//业务层注入Dao
	private ProductDao productDao;
	//set方法注入dao
	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}
	
	/**
	 * 业务层保存商品
	 * @param product 
	 */
	public void save(Product product) {
		System.out.println("ProductService.........");
		productDao.save(product);
	}
}
