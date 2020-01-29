package com.hansol.std.data.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * {key:queryId, value:DatasourceId} 정보를 해시맵에 담아서 관리한다.
 * @author PJW
 */
@Component
public class RepositoryScan {

	private static Map<String,String> repoList = new HashMap<>();

	private SqlSessionTemplate sqlSession1;
	private SqlSessionTemplate sqlSession2;
	private SqlSessionTemplate sqlSession3;
	private SqlSessionTemplate sqlSession4;

	@Autowired
	@Qualifier(DatasourceConstants.SQL_SESSION1)
	public void setSqlSessionTemplate1(SqlSessionTemplate sqlSession) {
		this.sqlSession1 = sqlSession;
	}

	@Autowired
	@Qualifier(DatasourceConstants.SQL_SESSION2)
	public void setSqlSessionTemplate2(SqlSessionTemplate sqlSession) {
		this.sqlSession2 = sqlSession;
	}

	@Autowired
	@Qualifier(DatasourceConstants.SQL_SESSION3)
	public void setSqlSessionTemplate3(SqlSessionTemplate sqlSession) {
		this.sqlSession3 = sqlSession;
	}

	@Autowired
	@Qualifier(DatasourceConstants.SQL_SESSION4)
	public void setSqlSessionTemplate4(SqlSessionTemplate sqlSession) {
		this.sqlSession4 = sqlSession;
	}

	/**
	 * MyBatis Mapper xml에 등록된 서비스 노드의 정보를 해시맵에 담는다.
	 */
	public void retreiveMapperScanCommand(boolean isClear) {

		if (isClear) {
			repoList.clear();
		}

		Collection<String> repoList1 = sqlSession1.getConfiguration().getMappedStatementNames();
		Collection<String> repoList2 = sqlSession2.getConfiguration().getMappedStatementNames();
		Collection<String> repoList3 = sqlSession3.getConfiguration().getMappedStatementNames();
		Collection<String> repoList4 = sqlSession4.getConfiguration().getMappedStatementNames();

		repoList1.stream().forEach(item -> {
			repoList.put(item, DatasourceConstants.DATASOURCE_1);
		});

		repoList2.stream().forEach(item -> {
			repoList.put(item, DatasourceConstants.DATASOURCE_2);
		});

		repoList3.stream().forEach(item -> {
			repoList.put(item, DatasourceConstants.DATASOURCE_3);
		});

		repoList4.stream().forEach(item -> {
			repoList.put(item, DatasourceConstants.DATASOURCE_4);
		});
	}

	/**
	 * MyBatis Mapper xml에 등록된 서비스 노드의 정보를 queryId를 통해 얻어온다.
	 * @param queryId
	 * @return
	 */
	public String getDatasourceId(String queryId) {
		return repoList.get(queryId);
	}
}
