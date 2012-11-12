/*******************************************************************************
 * Copyright (c) 2010, 2012 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.nexus.internal.plugin.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.tycho.nexus.internal.plugin.cache.PathLock;
import org.eclipse.tycho.nexus.internal.plugin.cache.PathLock.PathLockMonitor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PathLockTest {

    public static String aString = "";
    public static String bString = "";
    @SuppressWarnings("unused")
    private static String cString = "";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public static void buildStrings(final String path) {
        final PathLockMonitor lock = PathLock.getLock(path);
        synchronized (lock) {
            if (path.equals("a")) {
                aString += "a";
                cString += "y";
                Thread.yield();
                cString += "z";
                aString += "b";
            } else {
                bString += "c";
                cString += "y";
                Thread.yield();
                cString += "z";
                bString += "d";
            }
        }
        Assert.assertTrue(PathLock.releaseLock(lock));
    }

    @Test
    public void testPathLock() throws InterruptedException {
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(400);

        final ExecutorService executor = Executors.newCachedThreadPool();
        final List<Future<Void>> results = new LinkedList<Future<Void>>();

        for (int i = 0; i < 200; i++) {

            final Callable<Void> c = new CallableWorker(startSignal, doneSignal, "a");
            results.add(executor.submit(c));
            final Callable<Void> c2 = new CallableWorker(startSignal, doneSignal, "b");
            results.add(executor.submit(c2));

        }

        startSignal.countDown();
        doneSignal.await();
        //In theory this test can fail, as it can not be ruled out that the threads are 
        //executed in sequential order, but it is very probable therefore it might be interesting for manual testing
        //Assert.assertTrue(cString.contains("yy") || cString.contains("zz"));
        Assert.assertFalse(aString.contains("aa"));
        Assert.assertFalse(bString.contains("cc"));
        executor.shutdown();
    }

    class CallableWorker implements Callable<Void> {

        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final String path;

        CallableWorker(final CountDownLatch startSignal, final CountDownLatch doneSignal, final String path) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.path = path;
        }

        @Override
        public Void call() throws Exception {
            try {
                startSignal.await();
                doWork();
            } finally {
                doneSignal.countDown();
            }
            return null;
        }

        void doWork() {
            buildStrings(path);
        }
    }

}
