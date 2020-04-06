package sysmon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.eventbus.Subscribe;
import com.locus.jedi.log.ErrorLogger;
import com.locus.jedi.service.sql.SQLParam;
import com.locus.jedi.service.sql.SQLServiceException;
import com.locus.jedi.service.sql.SQLServiceManager;

public class StatisticSaver {

	private boolean received;
	private final static SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss", Locale.KOREA );

	@Subscribe
	public void recieveMessage(String message) {
		// ErrorLogger.info("String : {}"+ message);
		received = true;
	}

	@Subscribe
	public void getMessage(String message) {
		blockingJob(message);
		// ErrorLogger.info("xxx : {}"+ message);
		received = true;
	}

	public boolean isReceived() {
		return received;
	}

	private void blockingJob(final String message) {
		try {
			JSONObject obj = new JSONObject(message);

			MultiplyingTask mt = new MultiplyingTask(obj);

			ExecutorService es = Executors.newFixedThreadPool(10); // TODO Pool Size To Environment

			FutureTask<Boolean> future = new FutureTask<Boolean>(mt) {
				@Override
				protected void done() {
					try {
						boolean isSuccess = ((Boolean) get()).booleanValue();
						ErrorLogger.info("StatisticSaver > future task isSuccess : "+ isSuccess);
					} catch (InterruptedException e) {
						ErrorLogger.error("[InterruptedException] " + e.getMessage());
						Thread.currentThread().interrupt();
					} catch (ExecutionException e) {
						ErrorLogger.error("[ExecutionException] " + e.getMessage());
					}
				}
			};

			es.execute(future);
			es.shutdown();

		} catch (JSONException e1) {
			ErrorLogger.error(e1.getMessage());
		}
	}

	public class MultiplyingTask implements Callable {

		private JSONObject obj;

		public MultiplyingTask(JSONObject obj) {
			this.obj = obj;
		}

		@Override
		public Boolean call() throws Exception {
			return statisticInsert(this.obj);
		}
	}

	private boolean statisticInsert(JSONObject obj) {
		try {
			// VALUES (?cpu, ?deviceid, ?rmem, ?smem, str_to_date(?current_time,'%Y%m%d%H%i%s'), ?deviceip)
			String currTime = sdf.format ( new Date() );
			SQLParam sqlParam = new SQLParam();

			sqlParam.setSqlName("sysmon.statsave.insert");
			sqlParam.addValue("deviceid", obj.getString("deviceid"));
			sqlParam.addValue("deviceip", obj.getString("deviceip"));
			sqlParam.addValue("cpu", obj.getDouble("cpu"));
			sqlParam.addValue("rmem", obj.getDouble("rmem"));
			sqlParam.addValue("smem", obj.getDouble("smem"));
			sqlParam.addValue("current_time", currTime);

			SQLServiceManager.getInstance().execute(sqlParam);

		} catch(SQLServiceException e) {
			ErrorLogger.error(e);
			return false;
		} catch(Exception ex) {
			ErrorLogger.error(ex);
			return false;
		}

		return true;
	}
}
