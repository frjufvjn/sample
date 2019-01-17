class HowJdbc {
  /**
	 * @description DB 목록 반환 
	 * @param searchDt
	 * @param areaCode
	 * @return
	 */
	private static ArrayList<String> getDataFromDb(String searchDt, String areaCode) {

		ArrayList<String> res = new ArrayList<String>();

		String connectionUrl = "jdbc:sqlserver://" + HOST + ";" + "databaseName=" + DNAME + ";";

		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {

			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			con = DriverManager.getConnection(connectionUrl, SECU, SECP);

			stmt = con.createStatement();

			// [NOTICE-1]
			String areaCodeWhere = AreaCode.length > 1 ? " AND AreaCode = '"+areaCode+"'" : "";

			String sqlTxt = "SELECT FileName FROM " + TableName
					+ " WHERE StartTime BETWEEN {fn CONCAT('"+searchDt+"','000000')} AND {fn CONCAT('"+searchDt+"','235959')}"
					+ areaCodeWhere
					+ " ORDER BY FileName ASC"
					;

			rs = stmt.executeQuery(sqlTxt);

			/*
			ResultSetMetaData rsColumnInfo = rs.getMetaData();

			for (int i = 1; i <= rsColumnInfo.getColumnCount(); i++) {
				System.out.println(rsColumnInfo.getColumnName(i));
			}
			 */

			while(rs.next()) {
				res.add(rs.getString("FileName").trim());
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
