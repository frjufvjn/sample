package guava.filter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GuavaFilterTest {

	public static void main(String[] args) {
		new GuavaFilterTest().filterAction();
	}

	/**
	 * @description JDK1.7에서 stream API를 쓰지 못하는 관계로, Guava 라이브러리를 통하여 List Collection의 필터링을 구현한다. 
	 */
	private void filterAction() {
		List<Map<String, Object>> allItems = Lists.newArrayList();
		List<Map<String, Object>> compareItems = Lists.newArrayList();

		// 1. 전체 테스트 리스트 만들기 
		for (int i=0; i<50; i++) {
			Map<String, Object> paramsMap = Maps.newHashMap();

			paramsMap.put("id", i);
			if ( i < 20 ) {
				paramsMap.put("condition_1", true);
				paramsMap.put("condition_2", false);
				paramsMap.put("USE_YN", "Y");
			} else {
				paramsMap.put("condition_1", false);
				paramsMap.put("condition_2", true);
				paramsMap.put("USE_YN", "N");
			}

			paramsMap.put("condition_3", false);

			allItems.add(paramsMap);
		}

		// 2. 교집합 연산을 위한 테스트 리스트 만들기 
		for (int i=0; i<50; i++) {
			Map<String, Object> paramsMap = Maps.newHashMap();

			paramsMap.put("id", i);
			if ( i < 20 ) {
				paramsMap.put("condition_1", true);
				paramsMap.put("condition_2", false);
				paramsMap.put("USE_YN", "Y");
			} else {
				paramsMap.put("condition_1", false);
				paramsMap.put("condition_2", true);
				paramsMap.put("USE_YN", "N");
			}

			paramsMap.put("condition_3", false);

			compareItems.add(paramsMap);
		}

		System.out.println("Before size : " + allItems.size());
		System.out.println("Before : " + allItems.toString());

		long start = System.currentTimeMillis();

		// 3. 필터링 테스트 
		Iterable<Map<String, Object>> filtered = Iterables.filter(allItems, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(Map<String, Object> map) {
				return Boolean.TRUE.equals(map.get("condition_1"))
						&& Boolean.FALSE.equals(map.get("condition_2"));
			}
		});

		for (Map<String, Object> map : filtered) {
			System.out.println(map.toString());
		}

		Map<String, Object> oneMap = filtered.iterator().next();
		System.out.println("oneMap : " + oneMap.toString());

		// 3번의 연산을 위해서 final로 선언 
		final List<Map<String, Object>> filteredList = Lists.newArrayList(filtered);
		System.out.println("After size : " + filteredList.size());






		// 3. 두 리스트간의 교집합 리턴 테스트
		Iterable<Map<String, Object>> compared = Iterables.filter(compareItems, new Predicate<Map<String, Object>>() {
			@Override 
			public boolean apply(final Map<String, Object> map) {
				return Iterables.any(filteredList, new Predicate<Map<String, Object>>() {
					@Override 
					public boolean apply(Map<String, Object> anyMap) {
						return anyMap.get("id").equals(map.get("id"));
					}
				});
			}
		});

		List<Map<String, Object>> finalCompareList = Lists.newArrayList(compared);
		System.out.println(finalCompareList.toString());
		System.out.println("교집합 연산 전 사이즈 : " + compareItems.size());
		System.out.println("연산후 결과 : " + finalCompareList.size());

		// 4. 리스트 안에 구성되어 있는 맵을 원하는 형태로 변환 
		List<Map<String, Object>> transformedList = Lists.transform(finalCompareList, new Function<Map<String, Object>, Map<String, Object>>() {
			public Map<String, Object> apply(Map<String, Object> map) {
				Map<String,Object> res = new HashMap<String,Object>();
				res.put("id", map.get("id"));
				res.put("condition_2", map.get("condition_2"));
				res.put("USE_YN", map.get("USE_YN"));
				return res;
			}
		});
		System.out.println("Transformed List : " + transformedList.toString());
		System.out.println("Transformed List size : " + transformedList.size());




		System.out.println("Elapsed : " + (System.currentTimeMillis() - start) + "ms");
	}

}
