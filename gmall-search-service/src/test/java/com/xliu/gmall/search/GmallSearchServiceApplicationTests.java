package com.xliu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xliu.gmall.bean.PmsSearchSkuInfo;
import com.xliu.gmall.bean.PmsSkuInfo;
import com.xliu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.lucene.queryparser.xml.builders.FilteredQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.attrId", "12");
        boolQueryBuilder.filter(termQueryBuilder);

        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId","49","50");
        boolQueryBuilder.filter(termsQueryBuilder);

        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        Search build = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0105").addType("PmsSkuInfo").build();
        SearchResult searchResult = jestClient.execute(build);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            System.out.println(source);
        }
    }

    @Test
    public void put() throws IOException{
        List<PmsSkuInfo> pmsSkuInfos = new ArrayList<>();
        pmsSkuInfos = skuService.getAllSku();
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);
            pmsSearchSkuInfo.setId(Long.valueOf(pmsSkuInfo.getId()));
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(String.valueOf(pmsSearchSkuInfo.getId())).build();
            jestClient.execute(build);
        }
    }

}
