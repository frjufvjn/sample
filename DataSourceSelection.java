package cs.com.sqlservice;

public class DataSourceSelection {
	private static DataSourceSelection instance;
	/* 
	 * DataSorce 선택값 ( 0 ~ n )
	 * */
	private int DataSourceSelectedId;
	
	private DataSourceSelection () {
		DataSourceSelectedId = 0; // Default Setting
	}
	
	public static synchronized DataSourceSelection getInstance () {
		if (instance == null)
			instance = new DataSourceSelection();
		return instance;
	}
	
	public int getDataSourceSelectedId() {
		return DataSourceSelectedId;
	}

	public void setDataSourceSelectedId(int dataSourceSelectedId) {
		DataSourceSelectedId = dataSourceSelectedId;
	}
}
