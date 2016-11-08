package com.infinity.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Creates a keyspace and tables, and loads some data into them.<br>
 * 创建Keyspace与table，并插入一些数据
 * <p/>
 * Preconditions:
 * - a Cassandra cluster is running and accessible through the contacts points identified by CONTACT_POINTS and PORT.
 * <p/>
 * Side effects:
 * - creates a new keyspace "simplex" in the cluster. If a keyspace with this name already exists, it will be reused;
 * - creates two tables "simplex.songs" and "simplex.playlists". If they exist already, they will be reused;
 * - inserts a row in each table.
 *
 * @see <a href="http://datastax.github.io/java-driver/manual/">Java driver online manual</a>
 */
public class CassandraUsageDemo {

    static String[] CONTACT_POINTS = {"127.0.0.1"};
    static int PORT = 9042;


    private Cluster cluster;
    private Session session;

    public static void main(String[] args) {

        CassandraUsageDemo client = new CassandraUsageDemo();

        try {

            client.connect(CONTACT_POINTS, PORT);
            client.createSchema();
            client.loadData();
            client.querySchema();
            client.deleteData();
        } finally {
            client.close();
        }
    }


    /**
     * Initiates a connection to the cluster
     * specified by the given contact point. <br>
     * 连接到指定的Cassandra节点。 该节点最好是Seeds server
     *
     * @param contactPoints the contact points to use. 连接点
     * @param port          the port to use. 端口，默认9042
     */
    public void connect(String[] contactPoints, int port) {

        cluster = Cluster.builder()
                .withClusterName("myCluster")
                .addContactPoints(contactPoints).withPort(port)
                .build();

        System.out.printf("Connected to cluster: %s%n", cluster.getMetadata().getClusterName());

        session = cluster.connect();
    }

    /**
     * Creates the schema (keyspace) and tables
     * for this example.
     */
    public void createSchema() {
        // 创建Keyspace simplex, 如果之前已经创建了就直接复用
        // 使用SimpleStrategy， 复制因子=1 （数据没有备份，只存放1份）
        session.execute("CREATE KEYSPACE IF NOT EXISTS simplex WITH replication " +
                "= {'class':'SimpleStrategy', 'replication_factor':1};");

//        'class':'NetworkTopologyStrategy','dc1':3, 'dc2':2
        // 创建Table simplex.songs
        session.execute(
                "CREATE TABLE IF NOT EXISTS simplex.songs (" +
                        "id uuid PRIMARY KEY," +
                        "title text," +
                        "album text," +
                        "artist text," +    // Cassandra 之中varchar == text
                        "tags set<text>," +        // 这里用了set的数据类型
                        "data blob" +            // 二进制类型
                        ");");

        // 创建表simplex.playlists
        // 注意：playlists 跟 songs的数据是重复的， 
        // 只是primary key 不太一样。 
        // 这就是NoSQL跟RDBMS的不一样： 反范式
        session.execute(
                "CREATE TABLE IF NOT EXISTS simplex.playlists (" +
                        "id uuid," +
                        "title text," +
                        "album text, " +
                        "artist text," +
                        "song_id uuid," +
                        "PRIMARY KEY (id, title, album, artist)" +
                        ");");
    }

    /**
     * Inserts data into the tables.<br>
     * 插入数据
     */
    public void loadData() {

        Session s1 = cluster.connect("simplex");
        Session s2 = cluster.connect("simplex");
        Session s3 = cluster.connect("simplex");

        session.execute(
                "INSERT INTO simplex.songs (id, title, album, artist, tags) " +
                        "VALUES (" +
                        "756716f7-2e54-4715-9f00-91dcbea6cf50," +
                        "'La Petite Tonkinoise'," +
                        "'Bye Bye Blackbird'," +
                        "'Joséphine Baker'," +
                        "{'jazz', '2013'})" +
                        ";");

        session.execute(
                "INSERT INTO simplex.playlists (id, song_id, title, album, artist) " +
                        "VALUES (" +
                        "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d," +
                        "756716f7-2e54-4715-9f00-91dcbea6cf50," +
                        "'La Petite Tonkinoise'," +
                        "'Bye Bye Blackbird'," +
                        "'Joséphine Baker'" +
                        ");");

        QueryBuilder.gt("a", 2);
    }

    /**
     * Queries and displays data.<br>
     * 查询数据
     */
    public void querySchema() {
        ResultSet results = session.execute(
                "SELECT * FROM simplex.playlists " +
                        "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;");
//        session.execute("use keyspace");
        System.out.printf("%-30s\t%-20s\t%-20s%n", "title", "album", "artist");
        System.out.println("-------------------------------+-----------------------+--------------------");

//        List<Row> rows = results.all();
        for (Row row : results) {

            System.out.printf("%-30s\t%-20s\t%-20s%n",
                    row.getString("title"),
                    row.getString("album"),
                    row.getString("artist"));

        }


        // 把上一条语句修改成 异步IO
        // 创建一个PreparedStatement， 用于预编译的查询语句
        PreparedStatement statement = session.prepare(
                "SELECT * FROM simplex.playlists WHERE id = ?");

        List<ResultSetFuture> futures = new ArrayList<>();
        String[] ids = {"2cc9ccb7-6221-4ccb-8387-f22b6a1b354d"};

        System.out.printf("%-30s\t%-20s\t%-20s%n", "title", "album", "artist");
        System.out.println("-------------------------------+-----------------------+--------------------");

        for (String id : ids) {
            // 【重要】 这里使用的是executeAsync， 而whereInDemo 之中使用的是execute() 方法
            // 另外，因为Cassandra之中schema的定义id为uuid类型，
            // 所以我们不能直接塞入一个String，可以通过UUID.fromString()方法构造一个UUID对象
            ResultSetFuture resultSetFuture = session.executeAsync(statement.bind(UUID.fromString(id)));

            // 将future 放到一个list 之中， 方便后期使用
            futures.add(resultSetFuture);
        }

        for (ResultSetFuture future : futures) {
            // 【重要】逐个便利Futre List， 并且使用的是getUninterruptibly() 来获取结果集ResultSet
            ResultSet rows = future.getUninterruptibly();
            for (Row row : rows) {
                System.out.printf("%-30s\t%-20s\t%-20s%n",
                        row.getString("title"),
                        row.getString("album"),
                        row.getString("artist"));
            }
        }


    }

    /**
     * Delete data <br>
     * 删除数据
     */
    private void deleteData() {
        ResultSet results = session.execute(
                "delete from simplex.playlists " +
                        "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;");
        // 删除之后再次进行查询
        querySchema();
    }

    /**
     * Closes the session and the cluster.<br>
     * 最后一定要记得关闭！
     */
    public void close() {
        session.close();
        cluster.close();
    }

}