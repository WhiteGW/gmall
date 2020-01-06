package com.atguigu.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
class GmallListServiceApplicationTests {

	@Autowired
	private JestClient jestClient;

	@Test
	void contextLoads() {
	}

	@Test
	public void testES() throws IOException {
		String query = "";
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		SearchResult searchResult = jestClient.execute(search);
		List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);
		for (SearchResult.Hit<Map, Void> hit : hits) {
			Map map = hit.source;
			System.out.println(map);
		}
	}

}
