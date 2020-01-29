package com.hansol.std.data.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.SqlCommandType;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.hansol.std.data.config.DatasourceConstants;
import com.hansol.std.data.config.RepositoryScan;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class MybatisRepository extends EgovAbstractMapper {

	private static final String sqlResultKey = "result";
	private static final String sqlMessageKey = "message";
	private static final String sqlSuccessMessage = "success";
	private static final String sqlFailureMessage = "fail";

	private SqlSessionTemplate sqlSession1;
	private SqlSessionTemplate sqlSession2;
	private SqlSessionTemplate sqlSession3;
	private SqlSessionTemplate sqlSession4;
	private RepositoryScan repoScan;

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

	@Autowired
	public void setRepoScan(RepositoryScan repoScan) {
		this.repoScan = repoScan;
	}


	private DataSourceTransactionManager txManager;

	@Autowired
	public void setTxManager(DataSourceTransactionManager txManager) {
		this.txManager = txManager;
	}


	public Map<String,Object> sqlService(final String queryId, final Object param) {

		Map<String,Object> result = new HashMap<String,Object>();

		String dsId = repoScan.getDatasourceId(queryId);
		switch (dsId) {
		case DatasourceConstants.DATASOURCE_1:
			result = sqlServiceBroker(sqlSession1, queryId, param);
			break;
		case DatasourceConstants.DATASOURCE_2:
			result = sqlServiceBroker(sqlSession2, queryId, param);
			break;
		case DatasourceConstants.DATASOURCE_3:
			result = sqlServiceBroker(sqlSession3, queryId, param);
			break;
		case DatasourceConstants.DATASOURCE_4:
			result = sqlServiceBroker(sqlSession4, queryId, param);
			break;
		default:
			result.put(sqlResultKey, sqlFailureMessage);
			result.replace(sqlMessageKey, "Unknown Datasource...");
			break;
		}

		return result;
	}

	private Map<String,Object> sqlServiceBroker(final SqlSessionTemplate sqlSession, final String queryId, final Object param) {
		Map<String,Object> result = new HashMap<String,Object>();
		result.put(sqlMessageKey, sqlSuccessMessage);

		try {
			final SqlCommandType cmd = sqlSession.getConfiguration().getMappedStatement(queryId).getSqlCommandType();

			switch (cmd) {
			case SELECT:
				result.put(sqlResultKey, sqlSession.selectList(queryId, param));
				break;

			case INSERT:
				if (param instanceof List) {
					result.put(sqlResultKey, sqlBatchService(sqlSession, queryId, param, SqlCommandType.INSERT));
				} else {
					result.put(sqlResultKey, sqlSession.insert(queryId, param));
				}
				break;

			case UPDATE:
				if (param instanceof List) {
					result.put(sqlResultKey, sqlBatchService(sqlSession, queryId, param, SqlCommandType.UPDATE));
				} else {
					result.put(sqlResultKey, sqlSession.update(queryId, param));
				}
				break;

			case DELETE:
				if (param instanceof List) {
					result.put(sqlResultKey, sqlBatchService(sqlSession, queryId, param, SqlCommandType.DELETE));
				} else {
					result.put(sqlResultKey, sqlSession.delete(queryId, param));
				}
				break;

			default:
				result.put(sqlResultKey, sqlFailureMessage);
				result.replace(sqlMessageKey, "Unknown Sql Command (SELECT,INSERT,UPDATE,DELETE)...");
				break;
			}

		} catch (Exception e) {
			System.out.println("sqlService : " + e.getCause().getMessage()); // TODO

			result.put(sqlResultKey, sqlFailureMessage);
			result.replace(sqlMessageKey, "'" + queryId + "' " + e.getCause().getMessage());
		}

		return result;
	}


	private int sqlBatchService(final SqlSessionTemplate sqlSession, final String queryId, final Object param, SqlCommandType cmd) {

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> paramList = ((List<Map<String,Object>>)param);

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName(queryId + ":" + cmd.name());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = txManager.getTransaction(def);

		int result = 0;
		try {
			for (Map<String, Object> paramMap : paramList) {
				switch (cmd) {
				case INSERT:
					int insRes = sqlSession.insert(queryId, paramMap);
					result = result + insRes;

					break;

				case UPDATE:
					int updRes = sqlSession.update(queryId, paramMap);
					result = result + updRes;

					break;

				case DELETE:
					int delRes = sqlSession.delete(queryId, paramMap);
					result = result + delRes;
					break;

				default:
					throw new IllegalArgumentException();
				}
			}

		} catch (Exception e) {
			System.out.println("sqlBatchService : " + e.getCause().getLocalizedMessage()); // TODO
			txManager.rollback(status);
			// return 0;
			throw e;
		}

		txManager.commit(status);

		return result;
	}


















	@Transactional
	public void sqlTxService() {

		sqlSession1.insert("insert_tx_svc01"); // 성공
		sqlSession1.insert("insert_tx_svc02"); // 실패 --> tx rollback 확인


		// Cross Datasource Tx Management --> 불가능
		/*
		sqlSession2.insert("insert_tx_svc02_ds2"); // 성공
		sqlSession1.insert("insert_tx_svc02"); // 실패 --> tx rollback 확인
		 */

		// TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); TODO ??
	}

	@Transactional
	public void sqlBatchInsert() {
		Map<String,String> m = new HashMap<String,String>();

		for (int i=0; i<5; i++) {
			m.clear();
			m.put("device_id", "key"+i);

			sqlSession1.insert("insert_tx_svc03", m);
		}
	}

	// CALL `debug_msg`('1', 'aa', @result);
	// select @result;
	public int callProcedure() {
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("param1", 0);
		paramMap.put("param2", "aaaa");
		sqlSession1.selectOne("sp_test01", paramMap);

		System.out.println(paramMap);

		return Integer.parseInt(paramMap.get("result").toString());
	}



	public Map<String,Object> commonSqlServiceTest(final String queryId, final Object param) throws Exception {

		Map<String,Object> result = new HashMap<String,Object>();

		// 1. 호출하려는 query id가 어떤 데이터소스에 속해 있는지에 대해 신경쓰지 않고 호출 
		if ( sqlSession1.getConfiguration().getMappedStatementNames().contains(queryId) ) {
			// Primary Datssource Access
			result.put("res", "ok");
			result.put("data", sqlSession1.selectList(queryId, param));
			return result;

		} else {

			// 2. 호출하려는 query id가 어떤 SQL Command인지 신경쓰지 않고 (SELECT, INSERT, UPDATE, DELETE)
			System.out.println(sqlSession2.getConfiguration().getMappedStatement(queryId).getSqlCommandType().name() );

			// 3. Create, Update, Delete의 경우 들어온 파라미터 타입이 List일 경우 Batch Mode로 아닐 경우 그냥

			// 4. 테스트가 다 끝나면 이 클래스를 감싸는 인터페이스로 래핑 필요

			// 5. Primary가 아닌 데이터소스는 RepositoryScan에 담긴 queryId 해시맵에 담긴 정보를 참조하여 2,3,4 중 어떤 데이터소스인지 얻어온다. 
			String dsId = repoScan.getDatasourceId(queryId);
			if ( DatasourceConstants.DATASOURCE_2.equals(dsId) ) {
				SqlCommandType cmd = sqlSession2.getConfiguration().getMappedStatement(queryId).getSqlCommandType();
				if (cmd == SqlCommandType.SELECT) {
					result.put("res", "ok");
					result.put("data", sqlSession2.selectList(queryId, param));
					return result;
				} else if ( cmd == SqlCommandType.INSERT ) {
					sqlSession2.insert(queryId, param);
					result.put("res", "ok");
					return result;
				} else {
					result.put("res", "nok");
					return result;
				}

			} else {
				return null; // TODO 
			}
			/*else if ( Constants.DATASOURCE_3.equals(dsId) ) {
				// TODO Do Something
			}*/ 
		}
	}


	public <E> List<E> selectList(final String queryId, final Object paramMap) {


		return null;
	}


	public List<Object> selectListTest() {
		Map<String,String> parameter = new HashMap<String,String>();
		parameter.put("device_id", "SERVER01");

		return sqlSession1.selectList( "select_list_test", parameter);
	}

}
