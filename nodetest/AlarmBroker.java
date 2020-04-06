package sysmon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.locus.jedi.log.ErrorLogger;

import sysmon.cache.CacheModule;
import sysmon.cache.CacheService;

public class AlarmBroker {
	private boolean received;

	private static final CacheService cacheService = Guice.createInjector(new CacheModule()).getInstance(CacheService.class);

	@Subscribe
	public void getMessage(String message) {
		// ErrorLogger.info("AlarmBroker getMessage : " + message);
		dispatch(message);
		received = true;
	}

	public boolean isReceived() {
		return received;
	}

	private int getThresholdCpu(String deviceId) {
		return cacheService.getUsageConfigAlarmValue(deviceId, "CPU");
	}

	private int getThresholdRmem(String deviceId) {
		return cacheService.getUsageConfigAlarmValue(deviceId, "MEMORY");
	}

	private int getThresholdSmem(String deviceId) {
		return cacheService.getUsageConfigAlarmValue(deviceId, "SWAP");
	}


	private void dispatch(final String message) {
		try {
			JSONObject obj = new JSONObject(message);

			String deviceId = obj.getString("deviceid");
			Double realCpu = obj.getDouble("cpu");
			Double realMem = obj.getDouble("rmem");
			Double realSwap = obj.getDouble("smem");

			int cpuAlarmVal = getThresholdCpu(deviceId);
			int memAlarmVal = getThresholdRmem(deviceId);
			int swapAlarmVal = getThresholdSmem(deviceId);

			if ( isValidResourceValue(deviceId, AlarmType.RESOURCE_CPU, realCpu, cpuAlarmVal) 
					&& cpuAlarmVal < realCpu )
			{
				String alarmMessage = deviceId + " 서버 CPU 사용율 임계치 초과되었습니다.(" +
						String.format("%.2f", realCpu) + "% > " + cpuAlarmVal + "%)";

				dispatchAlarm(AlarmType.RESOURCE_CPU, deviceId, alarmMessage, "");
			}

			if ( isValidResourceValue(deviceId, AlarmType.RESOURCE_MEMORY, realMem, memAlarmVal) &&
					memAlarmVal < realMem )
			{
				String alarmMessage = deviceId + " 서버 메모리 사용율 임계치 초과되었습니다.(" +
						String.format("%.2f", realMem) + "% > " + memAlarmVal + "%)";

				dispatchAlarm(AlarmType.RESOURCE_MEMORY, deviceId, alarmMessage, "");
			}

			if ( isValidResourceValue(deviceId, AlarmType.RESOURCE_SWAP, realSwap, swapAlarmVal) &&
					swapAlarmVal < realSwap )
			{
				String alarmMessage = deviceId + " 서버 SWAP사용율 임계치 초과되었습니다.(" +
						String.format("%.2f", realSwap) + "% > " + swapAlarmVal + "%)";

				dispatchAlarm(AlarmType.RESOURCE_SWAP, deviceId, alarmMessage, "");
			}



			JSONArray diskinfo = obj.getJSONArray("diskinfo");
			for (int i=0; i < diskinfo.length(); i++) {
				String partiNm = diskinfo.getJSONObject(i).getString("path");
				final int diskAlarmValue = cacheService.getDiskConfigAlarmValue(deviceId, partiNm);

				if ( Constants.NOT_FOUND_RETURN_INTEGER == diskAlarmValue ) {
					// 해당 파티션이 등록되어 있지 않음
					ErrorLogger.warn("disk's usage alarm config not found : "+deviceId+", partiNm : " + partiNm);
				}
				else if ( Constants.NULL_RETURN_INTEGER == diskAlarmValue ) {
					// 파타션이 등록되었으나 설정 값이 비어있음
					ErrorLogger.warn("disk's config value undefined : " + deviceId);
				}
				else {
					Double realUsage = diskinfo.getJSONObject(i).getDouble("usedPercent");
					if (realUsage.isNaN() || realUsage.isInfinite() || realUsage > 100.0) {
						ErrorLogger.warn("Abnormal Received Data id:" + deviceId + ", type:" + AlarmType.RESOURCE_DISK.getTypeName() + ", value:" + realUsage );
					}
					else if (realUsage > diskAlarmValue)
					{
						String alarmMsg = deviceId + " 서버 디스크 임계치 초과 되었습니다. (" + partiNm + " : "
								+ String.format("%.2f", realUsage) + "% > " + diskAlarmValue + "%)";
						dispatchAlarm(AlarmType.RESOURCE_DISK, deviceId, alarmMsg, partiNm);
					}
				}
			}

		} catch (JSONException e) {
			ErrorLogger.error("AlarmBroker dispatch Json Parse error : " + e.getMessage());
		}
	}

	private void dispatchAlarm(final AlarmType type, final String deviceId, final String alarmMessage, final String addition) {
		ErrorLogger.warn("dispatchAlarm > type :" + type.getTypeName() + " deviceId : " + deviceId + 
				" alarmMessage : " + alarmMessage + " addition : " + addition);
	}

	private boolean isValidResourceValue(final String deviceId, final AlarmType type, final Double resValue, final int chkValue) {
		if ( !isValidAlarmValue(chkValue) ) return false;

		if ( resValue.isNaN() || resValue.isInfinite() || resValue > 100.0 ) {
			ErrorLogger.warn("Abnormal Received Data id:"+deviceId+", type:"+type.getTypeName()+", value:" + resValue);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @description Integer value invalid check
	 * @param intValue
	 * @return {Integer}
	 */
	private boolean isValidAlarmValue(final int chkValue) {
		return chkValue != Constants.NOT_FOUND_RETURN_INTEGER && chkValue != Constants.NULL_RETURN_INTEGER;
	}
}
