package com.hansol.std.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hansol.std.data.mappers.Mappers1;
import com.hansol.std.service.CommonSqlService;

@Controller
public class WebController {

	@Autowired
	private Mappers1 mappers1;

	private CommonSqlService commonService;

	@Autowired
	public void setCommService(CommonSqlService commonService) {
		this.commonService = commonService;
	}



	@GetMapping("/hello")
	public @ResponseBody String hello() {
		return "say hello";
	}

	@GetMapping("/insert-tx-test")
	public @ResponseBody String insertTxTest() {
		commonService.insertTxTest();
		return "ok";
	}

	@GetMapping("/insert-batch-test")
	public @ResponseBody String insertBatchTest() {
		commonService.insertBatchTest();
		return "ok";
	}

	@GetMapping("/procedure-test")
	public @ResponseBody int procTest() {
		return commonService.procedureTest();
	}

	// 메퍼 인터페이스로 분리 했을때의 케이스
	@GetMapping("/sql-mapper-1")
	public @ResponseBody Map<String, String> testMappers() {
		Map<String, String> result = new HashMap<>();
		result.put("res", mappers1.selectTest());
		return result;
	}



	// mapper xml로 분리했을때의 케이스
	@GetMapping("/sql-xml-1")
	public @ResponseBody List<Object> sqlOne() {
		return commonService.getSelectFromDatasource1Xml();
	}

	@GetMapping("/sql-wrapper-test/{queryId}")
	public @ResponseBody Map<String, Object> sqlWrapperTest(@PathVariable String queryId) {
		return commonService.sqlService(queryId, null);
	}

	/**
	 * @throws Exception 
	 * @description
	 * 	- curl -i -X POST -H "Content-Type: application/json; charset=utf-8" -d {\"sql\":\"select_list_ds2\"} http://localhost:8080/sql-xml-2
	 * 	- curl -i -X POST -H "Content-Type: application/json; charset=utf-8" -d {\"sql\":\"select_list_test\",\"param\":{\"device_id\":\"SERVER01\"}} http://localhost:8080/sql-xml-2
	 * 	- insert : curl -i -X POST -H "Content-Type: application/json; charset=utf-8" -d {\"sql\":\"insert_test_ds2\",\"param\":{\"device_id\":\"SERVER03\",\"device_ip\":\"127.0.0.1\"}} http://localhost:8080/sql-xml-2
	 * */
	@PostMapping(path="/sql-xml-2", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String,Object> sqlTwo(@RequestBody Map<String,Object> req) throws Exception {
		String reqSqlName = String.valueOf(req.getOrDefault("sql", ""));
		Map<String,Object> param = (Map<String, Object>) req.getOrDefault("param", null);
		return commonService.sqlService(reqSqlName, param);
	}


	@GetMapping("/sql-complex-test")
	public @ResponseBody String sqlComplexTest() {
		commonService.sqlBatchServiceTest1();
		return "ok";
	}

}
