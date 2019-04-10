import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatchUpdateApp {

	private static final String HOST = "127.0.0.1";
	private static final String PORT = "3306";
	private static final String DNAME = "test";
	private static final String USER = "root";
	private static final String PASSWD = "!QAZ2wsx";

	public BatchUpdateApp() {

	}

	public static void main(String[] args) {
		//		<100건 테스트 결과>
		//		Batch Update ---------------------------
		//		100
		//		elapsed : 750 ms
		//		Simple Update ---------------------------
		//		elapsed : 1306 ms


		//		<10000건 테스트 결과>		
		//		Batch Update ---------------------------
		//		10000
		//		elapsed : 7871 ms
		//		Simple Update ---------------------------
		//		elapsed : 72450 ms



		System.out.println("Batch Update ---------------------------");
		long start1 = System.currentTimeMillis();
		batchUpdate(selectList());
		System.out.println("elapsed : " + (System.currentTimeMillis() - start1) + " ms" );



		System.out.println("Simple Update ---------------------------");
		long start2 = System.currentTimeMillis();
		simpleUpdateWrapper(selectList());
		System.out.println("elapsed : " + (System.currentTimeMillis() - start2) + " ms" );
	}

	private static void batchUpdate(ArrayList<Map<String,Object>> list) {

		String connectionUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DNAME + "?characterEncoding=utf8";

		// Declare the JDBC objects.
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "UPDATE test4 SET c1 = ? WHERE data_key = ?";

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(connectionUrl, USER, PASSWD);
			ps = connection.prepareStatement(sql);

			for (Map<String, Object> map : list) {
				int updateValue = (Integer)map.get("c1") + 100;
				ps.setInt(1, updateValue);
				ps.setInt(2, (Integer)map.get("data_key"));

				ps.addBatch();
			}

			int[] ret = ps.executeBatch();

			System.out.println(ret.length);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void simpleUpdateWrapper(ArrayList<Map<String,Object>> list) {
		for (Map<String, Object> map : list) {
			simpleUpdate(map);
		}
	}

	private static void simpleUpdate(Map<String,Object> map) {
		String connectionUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DNAME + "?characterEncoding=utf8";

		// Declare the JDBC objects.
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(connectionUrl, USER, PASSWD);
			stmt = connection.createStatement();

			int c1 = (Integer)map.get("c1") + 100;
			int data_key = (Integer)map.get("data_key");

			String sql = "UPDATE test4 SET c1 = "+ c1 +" WHERE data_key = " + data_key;

			stmt.execute(sql);


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * data select
	 * @return
	 */
	private static ArrayList<Map<String,Object>> selectList() {
		ArrayList<Map<String,Object>> res = new ArrayList<Map<String,Object>>();

		// For mysql : jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf8
		String connectionUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DNAME + "?characterEncoding=utf8";

		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {

			Class.forName("com.mysql.jdbc.Driver");

			con = DriverManager.getConnection(connectionUrl, USER, PASSWD);

			stmt = con.createStatement();

			String sqlTxt = "SELECT * FROM test4 WHERE data_key BETWEEN 1 AND 10000";

			rs = stmt.executeQuery(sqlTxt);

			/*
			ResultSetMetaData rsColumnInfo = rs.getMetaData();

			for (int i = 1; i <= rsColumnInfo.getColumnCount(); i++) {
				System.out.println(rsColumnInfo.getColumnName(i));
			}
			 */

			while(rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("data_key", rs.getInt("data_key"));
				map.put("c1", rs.getInt("c1"));

				res.add(map);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return res;
	}

}
