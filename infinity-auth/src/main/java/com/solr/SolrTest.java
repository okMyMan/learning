package com.solr;

/**
 * Created by xule on 2017/5/15.
 */
import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
/**
 * API简单操作
 * @author weishuai
 *
 */
public class SolrTest {
    // solr 部署的url
    private static final String url = "http://localhost:8080/solr";
    // home
    private static final String uri = "my_core";

    // 添加一条数据
    public static void addDoc() throws SolrServerException, IOException {
        // 得到请求
        SolrClient sc = getSolrClient();
        // 拼装文本
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "id");
        // 其他参数省略。。。
        doc.addField("title", "我深深地爱着你，solr");
        sc.add(doc);
        sc.commit();
    }

    // 删除一条数据
    public static void deleteDocById() throws SolrServerException, IOException {
        // 得到请求
        SolrClient sc = getSolrClient();
        sc.deleteById("0");//id为0的数据
        sc.commit();
    }

    // 删除全部数据
    private static void deleteAllDoc() throws SolrServerException, IOException {
        // 得到请求
        SolrClient sc = getSolrClient();
        sc.deleteByQuery("*:*");
        sc.commit();
    }

    // 根据id修改一个数据(与添加类似：存在就修改，不存在就添加)
    private static void updateDocById() throws SolrServerException, IOException {
        // 得到请求
        SolrClient sc = getSolrClient();
        // 拼装文本
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "id");
        // 其他省略。。。
        doc.addField("title", "我深深地爱着你，solr !");
        sc.add(doc);
        sc.commit();
    }

    // 根据Id查询一条数据
    private static void getDocById() throws SolrServerException, IOException {
        // 得到请求
        SolrClient sc = getSolrClient();
        SolrDocument sd = sc.getById("id");
        System.out.println(sd); // 打印为：SolrDocument{id=id,
        // content_test=[我深深地爱着你，solr !],
        // _version_=1560544234369449984}
        System.out.println(sd.get("id"));
        System.out.println(sd.get("title"));
        System.out.println(sd.get("_version_"));

    }

    // 全部查询
    private static void getDocByAll() throws SolrServerException, IOException {
        SolrClient sc = getSolrClient();
        SolrQuery query = new SolrQuery();
        //设置查询条件（全部）
        query.setQuery("*:*");
        //查询
        SolrDocumentList solrDocumentList =sc.query(query).getResults();
        //遍历结果集
        for (SolrDocument solrDocument : solrDocumentList) {
            System.out.println(solrDocument); //打印为：SolrDocument{id=id, content_test=[我深深地爱着你，solr !], _version_=1560544234369449984}
            System.out.println(solrDocument.get("id"));
            System.out.println(solrDocument.get("title"));
            System.out.println(solrDocument.get("_version_"));
        }
    }

    public static void main(String[] args) throws SolrServerException,
            IOException {
        // 添加一个索引
        addDoc();
        // 根据Id删一条数据
         deleteDocById();
        // 删除全部数据
         deleteAllDoc();
        // 修改一条数据
         updateDocById();
        // 根据Id查询一条数据
         getDocById();
        // 全部查询
        getDocByAll();
    }

    /**
     * 该对象有两个可以使用，都是线程安全的 1、CommonsHttpSolrServer：启动web服务器使用的，通过http请求的 2、
     * EmbeddedSolrServer：内嵌式的，导入solr的jar包就可以使用了 3、solr
     * 4.0之后好像添加了不少东西，其中CommonsHttpSolrServer这个类改名为HttpSolrClient
     *
     * @return
     */
    public static SolrClient getSolrClient() {
        return new HttpSolrClient(url + "/" + uri);
    }
}
