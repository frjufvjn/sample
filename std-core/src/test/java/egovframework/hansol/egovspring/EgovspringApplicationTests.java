package egovframework.hansol.egovspring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticApplicationContext;

@SpringBootTest
class EgovspringApplicationTests {

	@Test
	public void test_dynamicBeanCreation() {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		beanFactory.registerSingleton("xxxxxsimplebean", new SimpleBean());
		SimpleBean bean = (SimpleBean)applicationContext.getBean("xxxxxsimplebean");
		bean.print();

		SimpleBean bean1 = (SimpleBean) beanFactory.getSingleton("xxxxxsimplebean");

		bean1.print();

		// applicationContext.removeBeanDefinition("xxxxxsimplebean"); // 에러
		applicationContext.close();
	}

	class SimpleBean {
		public void print() {
			System.out.println("Simple Bean!");
		}
	}
}
