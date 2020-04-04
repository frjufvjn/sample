package com.hansol.pom.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.hansol.pom.db.sql.SqlInnerModule;
import com.hansol.pom.db.sql.SqlInnerServices;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import junit.framework.TestCase;

@RunWith(VertxUnitRunner.class)
public class PivotTest extends TestCase {

	Vertx vertx;

	private SqlInnerServices sqlService = null;
	
	@Before
	public void setup() throws IOException {
		vertx = Vertx.vertx();
		sqlService = Guice.createInjector(new SqlInnerModule())
				.getInstance(SqlInnerServices.class);
	}

	@After
	public void teardown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}


	
	@Test
	public void test_sqlPivot(TestContext ctx) {
		Async async = ctx.async();

		long start = System.currentTimeMillis();
		

		sqlService.sqlRead(
				new JsonObject()
				.put("sqlName", "sql_pivot_test_01")
				, ar -> {
					if (ar.succeeded()) {
						System.out.println("elapsed after sql result : " +(System.currentTimeMillis()-start) + "ms");
						
						List<JsonObject> sqlResult = ar.result();
						Map<String, List<Object>> result = sqlResult
								.stream()
								.collect(Collectors.groupingBy(e -> ((JsonObject)e).getString("reg_hour")));

						// -- System.out.println(result.toString());

						// 피벗 그리드에 표현될 Y축에 해당되는 필드기준으로 그룹핑 된다. 
						Map<JsonObject, List<Object>> grouped = sqlResult
								.stream()
								.collect(Collectors.groupingBy(e -> {
									return new JsonObject()
											.put("reg_hour", ((JsonObject)e).getString("reg_hour"))
											.put("device_id", ((JsonObject)e).getString("device_id"))
											;
								}));

						// -- System.out.println(grouped.toString());

						Set<String> devices = grouped
								.keySet()
								.stream()
								.map(m -> ((JsonObject)m).getString("device_id"))
								.collect(Collectors.toCollection(TreeSet::new))
								;

						Set<String> hours = grouped
								.keySet()
								.stream()
								.map(m -> ((JsonObject)m).getString("reg_hour"))
								.collect(Collectors.toCollection(TreeSet::new))
								;
						//						System.out.print(",");
						//						hours.forEach(s -> System.out.print(s + ","));
						//						System.out.println();

						JsonArray cols = new JsonArray().add("group").add("type");
						hours.forEach(s -> {
							cols.add(s+"_avg").add(s+"_max");
						});

						JsonArray rows = new JsonArray();

						String[] types = {"cpu", "memory","swap"};
						for(int i=0; i<types.length; i++)
						{
							String type = types[i];
							devices.stream()
							.forEach(d -> {
								//*System.out.print(d);
								JsonArray row = new JsonArray().add(d).add(type);
								hours.stream()
								.forEach(h -> {
									JsonObject key = new JsonObject()
											.put("reg_hour", h)
											.put("device_id", d); 
									List<Object> l = grouped.get(key);
									if (l != null) {

										// Get Avg
										Double cpuAvg = l.stream()
												.collect(Collectors.averagingDouble(m -> ((JsonObject)m).getDouble(type)));
										//*System.out.print("," + cpuTot);
										row.add(cpuAvg);

										// Get Max
										Object o = l.stream()
												.max(Comparator.comparing(m -> ((JsonObject)m).getDouble(type)))
												.orElseThrow(NoSuchElementException::new);

										row.add(((JsonObject)o).getDouble(type));
									} else {
										//*System.out.print(",");
										row.add(0);
										row.add(0);
									}

								});
								//*System.out.println();
								rows.add(row);
							});
						}


						JsonObject finalResult = new JsonObject().put("columns", cols).put("records", rows);
						System.out.println(finalResult.encode());

						System.out.println("elapsed final : " +(System.currentTimeMillis()-start) + "ms");
						async.complete();
					}
				});
	}

	@Test
	public void test_sqlPivot2(TestContext ctx) {
		Async async = ctx.async();

		long start = System.currentTimeMillis();
		

		sqlService.sqlRead(
				new JsonObject()
				.put("sqlName", "sql_pivot_test_01")
				, ar -> {
					if (ar.succeeded()) {
						System.out.println("elapsed after sql result : " +(System.currentTimeMillis()-start) + "ms");
						
						List<JsonObject> sqlResult = ar.result();
						Map<String, List<Object>> result = sqlResult
								.stream()
								.collect(Collectors.groupingBy(e -> ((JsonObject)e).getString("reg_hour")));

						// -- System.out.println(result.toString());

						// 피벗 그리드에 표현될 Y축에 해당되는 필드기준으로 그룹핑 된다. 
						Map<JsonObject, List<Object>> grouped = sqlResult
								.stream()
								.collect(Collectors.groupingBy(e -> {
									return new JsonObject()
											.put("reg_hour", ((JsonObject)e).getString("reg_hour"))
											.put("device_id", ((JsonObject)e).getString("device_id"))
											;
								}));

						// -- System.out.println(grouped.toString());

						Set<String> devices = grouped
								.keySet()
								.stream()
								.map(m -> ((JsonObject)m).getString("device_id"))
								.collect(Collectors.toCollection(TreeSet::new))
								;

						Set<String> hours = grouped
								.keySet()
								.stream()
								.map(m -> ((JsonObject)m).getString("reg_hour"))
								.collect(Collectors.toCollection(TreeSet::new))
								;
						//						System.out.print(",");
						//						hours.forEach(s -> System.out.print(s + ","));
						//						System.out.println();

						JsonArray cols = new JsonArray().add("group").add("type");
						hours.forEach(s -> {
							cols.add(s+"_avg").add(s+"_max");
						});

						JsonArray rows = new JsonArray();

						String[] types = {"cpu", "memory","swap"};
						for(int i=0; i<types.length; i++)
						{
							String type = types[i];
							devices.stream()
							.forEach(d -> {
								//*System.out.print(d);
								JsonArray row = new JsonArray().add(d).add(type);
								hours.stream()
								.forEach(h -> {
									JsonObject key = new JsonObject()
											.put("reg_hour", h)
											.put("device_id", d); 
									List<Object> l = grouped.get(key);
									if (l != null) {

										// Get Avg
										Double cpuAvg = l.stream()
												.collect(Collectors.averagingDouble(m -> ((JsonObject)m).getDouble(type)));
										//*System.out.print("," + cpuTot);
										row.add(cpuAvg);

										// Get Max
										Object o = l.stream()
												.max(Comparator.comparing(m -> ((JsonObject)m).getDouble(type)))
												.orElseThrow(NoSuchElementException::new);

										row.add(((JsonObject)o).getDouble(type));
									} else {
										//*System.out.print(",");
										row.add(0);
										row.add(0);
									}

								});
								//*System.out.println();
								rows.add(row);
							});
						}


						JsonObject finalResult = new JsonObject().put("columns", cols).put("records", rows);
						System.out.println(finalResult.encode());

						System.out.println("elapsed final : " +(System.currentTimeMillis()-start) + "ms");
						async.complete();
					}
				});
	}

	@Test
	public void test_pivotExcel(TestContext context) throws FileNotFoundException, IOException {

		Async async = context.async();

		/**
		 * Ref. https://dzone.com/articles/java-pivot-table-using-streams
		 * */
		long start = System.currentTimeMillis();

		String filename = "C:/dev/pivot-test.csv";
		Pattern pattern = Pattern.compile(",");

		try (BufferedReader in = new BufferedReader(new FileReader(filename));) {
			Map<YearTeam,List<Player>> grouped = in
					.lines()
					.skip(1)
					.map(line -> {
						String[] arr = pattern.split(line);
						return new Player(Integer.parseInt(arr[0]),
								arr[1],
								arr[2],
								arr[3],
								Integer.parseInt(arr[4]));
					})
					.collect(Collectors.groupingBy(x-> new YearTeam(x.getYear(), x.getTeamID())));

			Set<String> teams = grouped
					.keySet()
					.stream()
					.map(x -> x.teamID)
					.collect(Collectors.toCollection(TreeSet::new));
			System.out.print(',');
			teams.stream().forEach(t -> System.out.print(t + ","));
			System.out.println();

			Set<Integer> years = grouped
					.keySet()
					.stream()
					.map(x -> x.year)
					.collect(Collectors.toSet());

			years
			.stream()
			.forEach(y -> {
				System.out.print(y + ",");
				teams.stream().forEach(t -> {
					YearTeam yt = new YearTeam(y, t);
					List<Player> players = grouped.get(yt);
					if ( players != null ) {
						long total = players
								.stream()
								.collect(Collectors.summingLong(Player::getSalary))
								;
						System.out.print("[");
						System.out.print(total);

						Player maxPlayer = players
								.stream()
								.max(Comparator.comparing(Player::getSalary))
								.orElseThrow(NoSuchElementException::new);
						System.out.print("," + maxPlayer.getSalary());
						System.out.print("]");
					}
					System.out.print(',');
				});
				System.out.println();
			});
		}

		System.out.println("elapsed:" + (System.currentTimeMillis() - start) + "ms");

		async.complete();
	}

	public class Player {
		private int year;
		private String teamID;
		private String lgID;
		private String playerID;
		public int getYear() {
			return year;
		}

		public void setYear(int year) {
			this.year = year;
		}

		public String getTeamID() {
			return teamID;
		}

		public void setTeamID(String teamID) {
			this.teamID = teamID;
		}

		public String getLgID() {
			return lgID;
		}

		public void setLgID(String lgID) {
			this.lgID = lgID;
		}

		public String getPlayerID() {
			return playerID;
		}

		public void setPlayerID(String playerID) {
			this.playerID = playerID;
		}

		public int getSalary() {
			return salary;
		}

		public void setSalary(int salary) {
			this.salary = salary;
		}

		private int salary;
		// defined getters and setters here

		public Player(int arg1, String arg2, String arg3, String arg4, int arg5) {
			this.year = arg1;
			this.teamID = arg2;
			this.lgID = arg3;
			this.playerID = arg4;
			this.salary = arg5;
		}
	}

	public class YearTeam
	{
		public int year;
		public String teamID;
		public YearTeam(int year,String teamID) {
			this.year = year;
			this.teamID = teamID;
		}
		@Override
		public boolean equals(Object other)
		{
			if ( other == null ) return false;
			if ( this == other ) return true;
			if ( other instanceof YearTeam ) {
				YearTeam yt = (YearTeam)other;
				if ( year == yt.year && teamID.equals(yt.teamID) )
					return true;
			}
			return false;
		}
		@Override
		public int hashCode()
		{
			int hash = 1;
			hash = hash * 17 + year;
			hash = hash * 31 + teamID.hashCode();
			return hash;
		}
		@Override
		public String toString()
		{
			StringBuilder sbuf = new StringBuilder();
			sbuf.append('[').append(year).append(", ").append(teamID)
			.append(']');
			return sbuf.toString();
		}
	}

}

