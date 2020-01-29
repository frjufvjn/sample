package com.hansol.std.data.config;

import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.hansol.std.data.mappers.Mappers1;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @description
 * <ul>
 *  <li>@Configuration
 *      <ul>
 *          <li>@Configuration 애너테이션이 선언 된 java 클래스는 스프링 IoC Container에게 해당 파일이 환경 설정과 관련된 파일(Bean 구성 Class)이라는 것을 인식시킨다.
 *          <li>스프링에서 @Configuration이 선언 된 클래스에서 @Bean으로 빈으로 만들었기 때문에 런타임시 스프링에서 싱글톤으로 관리한다.
 *      </ul>
 *  </li>
 *  <li>@Bean
 *      <ul>
 *          <li>스프링 IoC Container에 의해서 만들어진 자바 객체를 스프링 Bean(빈)이라고 부른다. 즉, 자바 객체이다.
 *          <li>빈은 스프링 IoC Container에 의해서 인스턴스화되서 관리되고 일반적으로 싱글톤이다.
 *      </ul>
 *  </li>
 *  <li>@PropertySource
 *      <ul>
 *          <li>스프링 IoC Container에서 런타임시 properties값을 가져오기 위함
 *      </ul>
 *  </li>
 * </ul>
 * */
@Configuration
public class DatasourceConfiguration {

	private ApplicationContext applicationContext;

	@Autowired
	public void setApplicationContext(ApplicationContext ctx) {
		this.applicationContext = ctx;
	}



	@Bean(name = DatasourceConstants.DATASOURCE_1, destroyMethod = "")
	@ConfigurationProperties(prefix = "spring.datasource." + DatasourceConstants.DATASOURCE_1)
	@Primary
	public DataSource dataSource1() {
		return new HikariDataSource();
	}

	@Bean(name = DatasourceConstants.DATASOURCE_2, destroyMethod = "")
	@ConfigurationProperties(prefix = "spring.datasource." + DatasourceConstants.DATASOURCE_2)
	public DataSource dataSource2() {
		return new HikariDataSource();
	}

	@Bean(name = DatasourceConstants.DATASOURCE_3, destroyMethod = "")
	@ConfigurationProperties(prefix = "spring.datasource." + DatasourceConstants.DATASOURCE_3)
	public DataSource dataSource3() {
		return new HikariDataSource();
	}

	@Bean(name = DatasourceConstants.DATASOURCE_4, destroyMethod = "")
	@ConfigurationProperties(prefix = "spring.datasource." + DatasourceConstants.DATASOURCE_4)
	public DataSource dataSource4() {
		return new HikariDataSource();
	}

	/**
	 * @description Define Datasource 1
	 * */
	@Bean(name = DatasourceConstants.SQL_SESSION_FACTORY1, destroyMethod = "") // TODO destroyMethod = ""은 AutoClosable ??
	@Primary
	public SqlSessionFactoryBean sqlSessionFactory1(@Named(DatasourceConstants.DATASOURCE_1 ) final DataSource dataSource) throws Exception {		
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/" + DatasourceConstants.DATASOURCE_1 + "/**/*.xml"));
		SqlSessionFactory sqlSessionFactory;
		sqlSessionFactory = sqlSessionFactoryBean.getObject();
		sqlSessionFactory.getConfiguration().addMapper(Mappers1.class);

		// Various other SqlSessionFactory settings
		return sqlSessionFactoryBean;
	}

	/**
	 * @description Mapper 인터페이스 설정 지원
	 * @param sqlSessionFactoryBean
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Primary
	public MapperFactoryBean<Mappers1> mapper1(@Named(DatasourceConstants.SQL_SESSION_FACTORY1) final SqlSessionFactoryBean sqlSessionFactoryBean) throws Exception {
		MapperFactoryBean<Mappers1> factoryBean = new MapperFactoryBean<>(Mappers1.class);
		factoryBean.setSqlSessionFactory(sqlSessionFactoryBean.getObject());
		return factoryBean;
	}

	@Bean(name = DatasourceConstants.SQL_SESSION1)
	@Primary
	public SqlSessionTemplate sqlSessionTemplate1(@Named(DatasourceConstants.SQL_SESSION_FACTORY1) SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	// TODO TX Manager [jw] 이 설정을 하지 않아도 @Transactional 걸어놓으면 되네....
//	@Bean(name = DatasourceConstants.SQL_SESSION1)
//	public PlatformTransactionManager txManager1() throws Exception {
//		return new DataSourceTransactionManager(dataSource1());
//	}



	/**
	 * @description Define Datasource 2
	 * */
	@Bean(name = DatasourceConstants.SQL_SESSION_FACTORY2, destroyMethod = "")
	public SqlSessionFactoryBean sqlSessionFactory2(@Named(DatasourceConstants.DATASOURCE_2 ) final DataSource dataSource) throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/" + DatasourceConstants.DATASOURCE_2 + "/**/*.xml"));

		// Various other SqlSessionFactory settings
		return sqlSessionFactoryBean;
	}

	@Bean(name = DatasourceConstants.SQL_SESSION2)
	public SqlSessionTemplate sqlSessionTemplate2(@Named(DatasourceConstants.SQL_SESSION_FACTORY2) SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}



	/**
	 * @description Define Datasource 3
	 * */
	@Bean(name = DatasourceConstants.SQL_SESSION_FACTORY3, destroyMethod = "")
	public SqlSessionFactoryBean sqlSessionFactory3(@Named(DatasourceConstants.DATASOURCE_3 ) final DataSource dataSource) throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/" + DatasourceConstants.DATASOURCE_3 + "/**/*.xml"));

		// Various other SqlSessionFactory settings
		return sqlSessionFactoryBean;
	}

	@Bean(name = DatasourceConstants.SQL_SESSION3)
	public SqlSessionTemplate sqlSessionTemplate3(@Named(DatasourceConstants.SQL_SESSION_FACTORY3) SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}



	/**
	 * @description Define Datasource 4
	 * */
	@Bean(name = DatasourceConstants.SQL_SESSION_FACTORY4, destroyMethod = "")
	public SqlSessionFactoryBean sqlSessionFactory4(@Named(DatasourceConstants.DATASOURCE_4 ) final DataSource dataSource) throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/" + DatasourceConstants.DATASOURCE_4 + "/**/*.xml"));

		// Various other SqlSessionFactory settings
		return sqlSessionFactoryBean;
	}

	@Bean(name = DatasourceConstants.SQL_SESSION4)
	public SqlSessionTemplate sqlSessionTemplate4(@Named(DatasourceConstants.SQL_SESSION_FACTORY4) SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
