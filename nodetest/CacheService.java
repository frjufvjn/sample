package sysmon.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.locus.jedi.log.ErrorLogger;
import com.locus.jedi.service.sql.SQLParam;
import com.locus.jedi.service.sql.SQLServiceManager;
import com.locus.jedi.transfer.ListParam;

import sysmon.Constants;

/**
 * @description 서버목록, 임계치 정보를 서버캐시에 저장해 놓고 비동기적으로 SQL서비스를 호출하여 데이터를 갱신한다.
 * @author PJW
 *
 */
public class CacheService {

	private static List<Map<String,Object>> serverList; 		// t_device
	private static List<Map<String,Object>> usageConfigList; 	// t_hw_gijun
	private static List<Map<String,Object>> diskConfigList; 	// t_disk_gijun
	private static List<Map<String,Object>> processConfigList; 	// t_process_gijun
	
	private static Map<String,Long> processRefreshInfo = new ConcurrentHashMap<String,Long>();

	private final int threadPoolSize = 5;

	public CacheService() {
		// TODO Thread Pool 갯수 config로 관리 세팅 
	}

	/**
	 * @description Guava EventBus를 통해 메세지를 subscribe하여 메세지를 받는다.
	 * @param message
	 */
	@Subscribe
	public void getMessage(String message) {
		ErrorLogger.info("message : " + message);
		refreshCommand(message);
	}

	/**
	 * @description 비동기 캐시리프레시 호출
	 * @param cmd
	 */
	private void refreshCommand(String cmd) {
		SqlTask task = new SqlTask(cmd);

		ExecutorService es = Executors.newFixedThreadPool(threadPoolSize);

		@SuppressWarnings("unchecked")
		FutureTask<Boolean> future = new FutureTask<Boolean>(task) {
			@Override
			protected void done() {
				try {
					boolean isSuccess = ((Boolean) get()).booleanValue();
					if (isSuccess) ErrorLogger.info("CacheService Task isSuccess ? "+ isSuccess);
					else ErrorLogger.error("CacheService Task isSuccess ? "+ isSuccess);

				} catch (InterruptedException e) {
					ErrorLogger.error("[CacheService > InterruptedException] " + e.getMessage());
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					ErrorLogger.error("[CacheService > ExecutionException] " + e.getMessage());
				}
			}
		};

		es.execute(future);
		es.shutdown();
	}

	/**
	 * @description SQL서비스를 호출하는 ExecutorService Callable 객체 
	 * @author PJW
	 */
	@SuppressWarnings("rawtypes")
	public class SqlTask implements Callable {

		private String arg;

		public SqlTask(String obj) {
			this.arg = obj;
		}

		@Override
		public Boolean call() throws Exception {
			return getCacheRefresh(this.arg);
		}
	}

	/**
	 * @description 캐시 리프레시를 위해 서비스 타입별로 SQL서비스를 호출하고 static 객체에 저장한다.
	 * @param argCmd
	 * @return
	 */
	private boolean getCacheRefresh(String argCmd) {
		try {
			String mainCmd = "";
			String subCmd = "";
			if ( argCmd.indexOf("|") != -1 ) {
				mainCmd = argCmd.split("\\|")[0];
				subCmd = argCmd.split("\\|")[1];
			} else {
				mainCmd = argCmd;
			}

			Map<String,Object> param = new HashMap<String,Object>();
			param.put("sqlName", getSqlName(mainCmd));
			List<Map<String,Object>> resList = getSqlSelectList(param);

			switch (mainCmd) {
			case "refresh-servers":
				if (serverList != null) serverList.clear();
				serverList = resList;
				ErrorLogger.info("Type : " + mainCmd + " Cache Refresh size : " + serverList.size());
				break;
			case "refresh-usage":
				if (usageConfigList != null) usageConfigList.clear();
				usageConfigList = resList;
				ErrorLogger.info("Type : " + mainCmd + " Cache Refresh size : " + usageConfigList.size());
				break;
			case "refresh-disk":
				if (diskConfigList != null) diskConfigList.clear();
				diskConfigList = resList;
				ErrorLogger.info("Type : " + mainCmd + " Cache Refresh size : " + diskConfigList.size());
				break;
			case "refresh-process":
				if (processConfigList != null) processConfigList.clear();
				processConfigList = resList;
				ErrorLogger.info("Type : " + mainCmd + " Cache Refresh size : " + processConfigList.size() + " subCmd : " + subCmd);
				
				// 장비별 프로세스 최종수정시간 기록
				setProcessRefreshInfo(subCmd, System.currentTimeMillis());
				break;
			}


			// TODO subCmd == deviceId 로 받아서 화면에 Response Hook을 줄 수 있도록 구현
			



		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public long getProcessRefreshInfo(String deviceId) {
		if (processRefreshInfo.containsKey(deviceId)) {
			return processRefreshInfo.get(deviceId);
		} else {
			return 0L;
		}
	}

	private void setProcessRefreshInfo(String deviceId, long lastRefreshTimeMills) {
		processRefreshInfo.put(deviceId, lastRefreshTimeMills);
	}

	/**
	 * @description Jedi Select SQL을 호출하고 결과를 Primitive Collection에 담는다.
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private List<Map<String,Object>> getSqlSelectList(Map<String,Object> param) throws Exception {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();

		try {
			SQLParam sqlParam = new SQLParam();

			Set<String> paramKeys = param.keySet();
			for ( String key : paramKeys ) {
				if ("sqlName".equals(key)) {
					sqlParam.setSqlName(param.get(key).toString());
				}
				sqlParam.addValue(key, param.get(key));
			}

			SQLParam sqlResult = SQLServiceManager.getInstance().execute(sqlParam);

			ListParam l = sqlResult.getListParam(sqlResult.getResultName());
			String[] cols = l.getColumns();

			for (int i=0 ; i<l.rowSize(); i++) {
				Map<String,Object> m = new HashMap<String,Object>();
				Object[] rows = l.getRow(i);
				for (int j = 0; j < rows.length; j++) {
					m.put(cols[j], rows[j]);
				}
				result.add(m);
			}

		} catch (Exception e) {
			ErrorLogger.error("CacheService > getSqlSelectList's exception : " + e.getMessage());
			throw new Exception();
		}

		return result;
	}

	private String getSqlName(String cmd) {
		switch (cmd) {
		case "refresh-servers":
			return "sysmon.getserverlist.select";
		case "refresh-usage":
			return "sysmon.getUsagelist.select";
		case "refresh-disk":
			return "sysmon.getDisklist.select";
		case "refresh-process":
			return "sysmon.getProcesslist.select";
		default:
			return "";
		}
	}






	/**
	 * 	/////////////////////////////////////////////////////////////////////////////////////////////////////
	 * 	@description Get Cache Data public methods
	 *  /////////////////////////////////////////////////////////////////////////////////////////////////////
	 * */

	public List<Map<String,Object>> getServersAlarmAvailable() {
		Iterable<Map<String, Object>> filtered = Iterables.filter(serverList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return "Y".equals(map.get("ALARM_YN"));
			}
		});

		return Lists.newArrayList(filtered);
	}

	public Map<String,Object> getServerByDeviceId(final String deviceId) {
		Iterable<Map<String, Object>> filtered = Iterables.filter(serverList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return deviceId.equals(map.get("DEVICE_ID"));
			}
		});

		if ( filtered.iterator().hasNext() ) {
			return filtered.iterator().next();
		} else {
			return null;
		}
	}

	private List<Map<String,Object>> filterByServers(final Iterable<Map<String, Object>> filtered) {
		Iterable<Map<String, Object>> compared = Iterables.filter(filtered, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(final Map<String, Object> map) {
				return Iterables.any(getServersAlarmAvailable(), new Predicate<Map<String, Object>>() {
					@Override 
					public boolean apply(Map<String, Object> anyMap) {
						return anyMap.get("DEVICE_ID").equals(map.get("DEVICE_ID"));
					}
				});
			}
		});

		return Lists.newArrayList(compared);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<Map<String,Object>> getUsageConfigList(final String deviceId, final String itemType) {
		final Iterable<Map<String, Object>> filtered = Iterables.filter(usageConfigList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return deviceId.equals(map.get("DEVICE_ID"))
						&& itemType.equals(map.get("ITEM_NM"));
			}
		});

		return filterByServers(filtered);
	}

	public int getUsageConfigAlarmValue(String deviceId, String itemType) {
		List<Map<String,Object>> l = getUsageConfigList(deviceId, itemType);
		if ( l.size() > 0 ) {
			String getValue = l.get(0).get("ALARM_THRESHOLD_VALUE").toString();
			return getValue == null ? Constants.NULL_RETURN_INTEGER : Integer.parseInt(getValue);
		} else {
			return Constants.NOT_FOUND_RETURN_INTEGER;
		}
	}

	public String[] getUsageConfigAlarmInfo(String deviceId, String itemType) {
		List<Map<String,Object>> l = getUsageConfigList(deviceId, itemType);
		if ( l.size() > 0 ) {
			String grade = l.get(0).get("ALARM_GIJUN").toString();
			String skipCount = String.valueOf(l.get(0).get("ALARM_SKIP_CNT"));
			String skipInterval = l.get(0).get("INTERVL").toString();
			String banFrom = l.get(0).get("FROM_TIME").toString();
			String banTo = l.get(0).get("TO_TIME").toString();
			String smsYn = l.get(0).get("SMS_YN").toString();

			return new String[] {grade, skipCount, skipInterval, banFrom, banTo, smsYn};
		} else {
			return Constants.NOT_FOUND_RETURN_ARRAY;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<Map<String,Object>> getDiskConfigList(final String deviceId, final String partiType) {
		final Iterable<Map<String, Object>> filtered = Iterables.filter(diskConfigList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return deviceId.equals(map.get("DEVICE_ID"))
						&& partiType.equals(map.get("PARTI_NM"));
			}
		});

		return filterByServers(filtered);
	}

	public int getDiskConfigAlarmValue(String deviceId, String partiType) {
		List<Map<String,Object>> l = getDiskConfigList(deviceId, partiType);
		if ( l.size() > 0 ) {
			String getValue = l.get(0).get("ALARM_THRESHOLD_VALUE").toString();
			return getValue == null ? Constants.NULL_RETURN_INTEGER : Integer.parseInt(getValue);
		} else {
			return Constants.NOT_FOUND_RETURN_INTEGER;
		}
	}

	public String[] getDiskConfigAlarmInfo(String deviceId, String partiType) {
		List<Map<String,Object>> l = getDiskConfigList(deviceId, partiType);
		if ( l.size() > 0 ) {
			String grade = l.get(0).get("ALARM_GIJUN").toString();
			String skipCount = String.valueOf(l.get(0).get("ALARM_SKIP_CNT"));
			String skipInterval = l.get(0).get("INTERVL").toString();
			String banFrom = l.get(0).get("FROM_TIME").toString();
			String banTo = l.get(0).get("TO_TIME").toString();
			String smsYn = l.get(0).get("SMS_YN").toString();

			return new String[] {grade, skipCount, skipInterval, banFrom, banTo, smsYn};
		} else {
			return Constants.NOT_FOUND_RETURN_ARRAY;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<Map<String,Object>> getProcessConfigList(final String deviceId) {
		final Iterable<Map<String, Object>> filtered = Iterables.filter(processConfigList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return deviceId.equals(map.get("DEVICE_ID"));
			}
		});

		List<Map<String, Object>> transformedList = Lists.transform(filterByServers(filtered), 
				new Function<Map<String, Object>, Map<String, Object>>() {
			@Override
			public Map<String, Object> apply(Map<String, Object> map) {
				Map<String,Object> res = new HashMap<String,Object>();
				res.put("id", map.get("DEVICE_ID"));
				res.put("procname", map.get("PROCESS_NM"));
				res.put("cnt", map.get("THRESHOLD_CNT"));
				res.put("memory", map.get("THRESHOLD_MEMORY"));
				return res;
			}
		});

		return transformedList;
	}

	public List<Map<String,Object>> getProcessConfigList(final String deviceId, final String processNm) {
		final Iterable<Map<String, Object>> filtered = Iterables.filter(processConfigList, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return deviceId.equals(map.get("DEVICE_ID"))
						&& processNm.equals(map.get("PROCESS_NM"));
			}
		});

		return filterByServers(filtered);
	}

	public String[] getProcessConfigAlarmInfo(String deviceId, String processNm) {
		List<Map<String,Object>> l = getProcessConfigList(deviceId, processNm);
		if ( l.size() > 0 ) {
			String grade = l.get(0).get("ALARM_GIJUN").toString();
			String skipCount = String.valueOf(l.get(0).get("ALARM_SKIP_CNT"));
			String skipInterval = l.get(0).get("INTERVL").toString();
			String banFrom = l.get(0).get("FROM_TIME").toString();
			String banTo = l.get(0).get("TO_TIME").toString();
			String smsYn = l.get(0).get("SMS_YN").toString();

			
			return new String[] {grade, skipCount, skipInterval, banFrom, banTo, smsYn};
		} else {
			return Constants.NOT_FOUND_RETURN_ARRAY;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
}
