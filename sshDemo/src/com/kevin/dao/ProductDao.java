package com.kevin.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.kevin.domain.Product;

public class ProductDao extends HibernateDaoSupport{

	/**
	 * dao层保存商品
	 * @param product 
	 */
	public void save(Product product) {
		System.out.println("ProductDao : save.....");
		System.out.println("ProductDao:"+product.getPname()+" "+product.getPrice());
		this.getHibernateTemplate().save(product);
	}

}
