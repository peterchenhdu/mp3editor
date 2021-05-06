# mp3editor
mp3 editor

# GitHub Start 
192.30.253.112    Build software better, together 
192.30.253.119    gist.github.com
151.101.184.133    assets-cdn.github.com
151.101.184.133    raw.githubusercontent.com
151.101.184.133    gist.githubusercontent.com
151.101.184.133    cloud.githubusercontent.com
151.101.184.133    camo.githubusercontent.com
151.101.184.133    avatars0.githubusercontent.com
151.101.184.133    avatars1.githubusercontent.com
151.101.184.133    avatars2.githubusercontent.com
151.101.184.133    avatars3.githubusercontent.com
151.101.184.133    avatars4.githubusercontent.com
151.101.184.133    avatars5.githubusercontent.com
151.101.184.133    avatars6.githubusercontent.com
151.101.184.133    avatars7.githubusercontent.com
151.101.184.133    avatars8.githubusercontent.com
 # GitHub End

package com.github.peterchenhdu.mp3editor;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class TestTemplate {

    private static final Integer THREAD_NUM = 1;

    private static final String PRE_TABLE = "7B" + THREAD_NUM;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(LocalDateTime.now() + ":开始3线程聚合查询测试");
//        createTables();
//        writeTest();
        queryTest();
    }

    public static void queryTest() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue(100));

        threadPoolExecutor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor executor) -> {
                    if (!executor.isShutdown()) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            System.out.println(e.toString());
                            Thread.currentThread().interrupt();
                        }
                    }
                }
        );

        for (int i = 0; i < 3; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // 执行线程池
                    List<String> tbNameList = getTableNameList();
                    Random random = new Random();
                    LocalDateTime startTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
                    while (true) {
                        LocalDate date = startTime.plusDays(random.nextInt(6)).toLocalDate();

                        executeQuery("select last(tag_value) from " + tbNameList.get(random.nextInt(tbNameList.size())) + " where data_timestamp>'" + date + " 00:00:00' and data_timestamp<'" + date.plusDays(1) + " 00:00:00' interval(5m) limit 5;");
                    }

                }
            });
        }
    }

    public static void queryTest2() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 3, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue(100));

        threadPoolExecutor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor executor) -> {
                    if (!executor.isShutdown()) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            System.out.println(e.toString());
                            Thread.currentThread().interrupt();
                        }
                    }
                }
        );

        for (int i = 0; i < 3; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // 执行线程池
                    List<String> tbNameList = getTableNameList();
                    Random random = new Random();
                    LocalDateTime startTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
                    while (true) {
                        LocalDate date = startTime.plusDays(random.nextInt(500)).toLocalDate();
                        long startTimestamp = System.currentTimeMillis();
                        executeQuery("select last(tag_value) from " + tbNameList.get(random.nextInt(100)) + " where data_timestamp>'" + date + " 00:00:00' and data_timestamp<'" + date.plusDays(1) + " 00:00:00' interval(5m) limit 5;");
                        System.out.println(Thread.currentThread().getName() + "数据查询耗时：" + (System.currentTimeMillis() - startTimestamp));
                    }

                }
            });
        }
    }

    public static void createTables() {
        for (int i = 0; i < 5; i++) {
            String tagName = PRE_TABLE + "." + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
            String tableName = tagName.replace(".", "_");
            System.out.println("创建表：" + tableName);
            executeUpdate("create table if not exists zhenergy_demo.tb_point_data_" + tableName + " using zhenergy_demo.stb_point_data tags ('" + tagName + "');");
        }
    }


    public static void writeTest() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue(100));

        threadPoolExecutor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor executor) -> {
                    if (!executor.isShutdown()) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            System.out.println(e.toString());
                            Thread.currentThread().interrupt();
                        }
                    }
                }
        );

        long startTime = System.currentTimeMillis();

        System.out.println(LocalDateTime.now() + "开始插入数据：");
        List<String> tbNameList = getTableNameList();
        System.out.println("表总记录数：" + tbNameList.size());
        final CountDownLatch endGate = new CountDownLatch(20000 * tbNameList.size());
//        for (String tableName : tbNameList) {
        String tableName = "tb_point_data_0423_test";
            System.out.println(LocalDateTime.now() + "开始插入数据：" + tableName);
            asyncBatchInsert(tableName, threadPoolExecutor, endGate);
//        }

        endGate.await();
        System.out.println("插入所有数据总耗时：" + (System.currentTimeMillis() - startTime));
        threadPoolExecutor.shutdown();
    }

    public static void asyncBatchInsert(String tableName,
                                        ThreadPoolExecutor threadPoolExecutor,
                                        CountDownLatch endGate) {
        DateTime time = new DateTime();
        time.setStart(LocalDateTime.of(2010, 1, 1, 0, 0, 0));
        time.setEnd(time.getStart().plusYears(10));
        long costSum = 0;
        for (int i = 1; i <= 300000; i++) {
            long startTime = System.currentTimeMillis();
            String sql = getSql(tableName, time);
            long cost = System.currentTimeMillis() - startTime;
            costSum += cost;
//            System.out.println("执行sql:" + tableName);
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // 执行线程池
                    try {

                        executeUpdate(sql);

                    } finally {
                        endGate.countDown();
                    }

                }
            });

//            try {
//                TimeUnit.MILLISECONDS.sleep(8);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

        }
        System.out.println("生产SQL总耗时：" + costSum);
    }

    public static void executeUpdate(String sql) {
        Connection connection = null;
        Statement statement = null;
        try {
            //从连接池中取出一个数据库连接对象
            connection = JdbcUtils.getConnection();
            statement = connection.createStatement();
//            long startTime = System.currentTimeMillis();
            int count = statement.executeUpdate(sql);
//            System.out.println(count + "数据消耗时间：" + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } finally {
            //将数据库连接对象还给连接池
            JdbcUtils.close(statement, connection);
        }
    }

    public static void executeQuery(String sql) {
        System.out.println(Thread.currentThread().getName() + ":执行SQL：" + sql);
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            //从连接池中取出一个数据库连接对象
            connection = JdbcUtils.getConnection();
            long startTimestamp = System.currentTimeMillis();
            ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println(Thread.currentThread().getName() + "数据查询耗时：" + (System.currentTimeMillis() - startTimestamp));
            while (rs.next()) {
//                rs.getObject("data_timestamp");
//                rs.getObject("tag_value");
                                rs.getObject("ts");
                rs.getObject("last(tag_value)");
//                System.out.println(":查询结果：" + rs.getObject("data_timestamp") + " " + rs.getObject("tag_value"));

//                System.out.println(Thread.currentThread().getName() + ":查询结果：" + rs.getObject("ts") + " " + rs.getObject("last(tag_value)"));
            }


        } catch (Exception e) {
            e.printStackTrace();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } finally {
            //将数据库连接对象还给连接池
            JdbcUtils.close(ps, connection);
        }
    }

    public static List<String> getTableNameList() {
        try {
            //从连接池中取出一个数据库连接对象
            Connection connection = JdbcUtils.getConnection();
            Statement statement = connection.createStatement();

//            ResultSet rs = statement.executeQuery("SELECT TBNAME FROM stb_point_data where TBNAME like '%data_0405_%'");
            ResultSet rs = statement.executeQuery("SELECT TBNAME FROM stb_point_data");

            //STEP 5: Extract data from result set
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                //Retrieve by column name
                list.add(rs.getString("tbname"));
            }
            rs.close();
            //将数据库连接对象还给连接池
            JdbcUtils.close(statement, connection);
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    public static String getSql(String tableName, DateTime time) {
        Random r = new Random();

        StringBuilder sb = new StringBuilder("insert into " + tableName + " (data_timestamp, tag_value) values ");
        int i = 0;
        while (time.getStart().isBefore(time.getEnd())) {
            sb.append("('");
            sb.append(Timestamp.valueOf(time.getStart()));
            sb.append("',");
            sb.append(r.nextDouble() * 20000 + ")");
            time.setStart(time.getStart().plusSeconds(1));
            i++;
            if (i == 1000) {
                sb.append(";");
                break;
            } else {
                sb.append(",");
            }
        }

        return sb.toString();
    }

}
