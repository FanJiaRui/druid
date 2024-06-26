package com.alibaba.druid.bvt.pool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.pool.DruidDataSource;

public class DruidDataSourceTest_interrupt extends TestCase {
    private DruidDataSource dataSource;

    protected void setUp() throws Exception {
        dataSource = new DruidDataSource() {
            protected void createAndStartCreatorThread() {
                return;
            }
        };

        dataSource.setUrl("jdbc:mock:xxx");
        dataSource.setTestOnBorrow(false);
        dataSource.setDefaultAutoCommit(true);
        dataSource.setInitialSize(1);
    }

    protected void tearDown() throws Exception {
        dataSource.close();
    }

    public void test_autoCommit() throws Exception {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(1);
        final AtomicInteger errorCount = new AtomicInteger();
        Thread thread = new Thread() {
            public void run() {
                try {
                    startLatch.countDown();
                    // not wait createConnectionThread latch as it is null: createAndStartCreatorThread method is mocked.
                    dataSource.init();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        startLatch.await();

        Thread.sleep(10);

        Assert.assertEquals(0, errorCount.get());

        thread.interrupt();

        endLatch.await();
        Assert.assertEquals(1, errorCount.get());
    }
}
