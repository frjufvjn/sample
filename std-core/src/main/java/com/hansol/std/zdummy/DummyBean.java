package com.hansol.std.zdummy;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticApplicationContext;

public class DummyBean {

	private void beanActionTest() {
		/**
		 * @description dynamic하게 Bean을 생성하는 테스트
		 * */
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		beanFactory.registerSingleton("xxxxxsimplebean", new SimpleBean());

		SimpleBean bean = (SimpleBean)applicationContext.getBean("xxxxxsimplebean");
		SimpleBean bean_ = (SimpleBean)applicationContext.getBean("xxxxxsimplebean");
		bean.print();

		SimpleBean bean1 = (SimpleBean) beanFactory.getSingleton("xxxxxsimplebean");
		bean1.print();


		// getBean 이든 getSingleton 둘다 static하게 가져오넴
		System.out.println("1:" + bean); 
		System.out.println("1-1:" + bean_);
		System.out.println("2:" + bean1);


		// applicationContext.removeBeanDefinition("xxxxxsimplebean"); // 에러
		applicationContext.close();


		// Context가 close된 이후에도 참조가 가능함.
		bean1.print();

		bean1.setType("a");

		beanFactory.registerSingleton("aaaasimplebean", new SimpleBean());
		SimpleBean beanA = (SimpleBean) beanFactory.getSingleton("aaaasimplebean");

		beanA.setType("b");

		System.out.println(bean1.getType());
		System.out.println(beanA.getType());
		bean1.print();
		beanA.print();

		// TODO Builder 패턴으로 각각 빈팩토리를 등록할 수 있게...
	}

	@Configuration
	public class SimpleBean {

		public SimpleBean() {
			System.out.println("SimpleBean Constructor....");
		}

		private String type;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Bean
		public void print() {
			System.out.println("Simple Bean! : " + this.type);
		}
	}
}
