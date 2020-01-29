package com.hansol.std.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hansol.std.data.repository.MybatisRepository;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service
public class CommonSqlService extends EgovAbstractServiceImpl {

	private MybatisRepository repo;

	@Autowired
	public void setMybatisRepository(MybatisRepository repo) {
		this.repo = repo;
	}



	public List<Object> getSelectFromDatasource1Xml() {
		return repo.selectListTest();
	}

	public Map<String,Object> sqlServiceList(final String queryId,  Object param) throws Exception {
		return repo.commonSqlServiceTest(queryId, param);
	}

	public void insertTxTest() {
		repo.sqlTxService();
	}
	
	public void insertBatchTest() {
		repo.sqlBatchInsert();
	}
	
	public int procedureTest() {
		return repo.callProcedure();
	}
	
	public Map<String, Object> sqlService(final String queryId, final Object param) {
		return repo.sqlService(queryId, param);
	}
	

	public void sqlBatchServiceTest1() {
		
		List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
		for (int i=0; i < 5; i++) {
			Map<String,Object> req = new HashMap<String,Object>();
			int idx = i == 2 || i == 3 ? 1 : i; // i; // i == 2 || i == 3 ? 1 : i;   트랜잭션 롤백 테스트를 위한...
			String value = "dd" +idx;
			req.put("device_id", value);
			
			System.out.println("value : " + value);
			
			l.add(req);
		}
		
		System.out.println("batchmode result : " + repo.sqlService("insert_tx_svc03", l));
	}
	
}
