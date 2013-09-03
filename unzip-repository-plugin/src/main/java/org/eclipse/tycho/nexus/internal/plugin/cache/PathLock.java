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

import java.util.HashMap;
import java.util.Map;

/**
 * Lock utility class to work around https://issues.sonatype.org/browse/NEXUS-3622
 */
class PathLock {
    private static final Map<String, PathLock> lockMap = new HashMap<String, PathLock>();

    static PathLockMonitor getLock(final String mdPath) {
        PathLock ret = null;
        synchronized (lockMap) {
            ret = lockMap.get(mdPath);
            if (ret == null) {
                ret = new PathLock(mdPath);
                lockMap.put(mdPath, ret);
            }
            ret.inc();
        }
        return ret.getLock();
    }

    private static boolean releaseLock(final String mdPath) {
        synchronized (lockMap) {
            final PathLock lock = lockMap.get(mdPath);
            if (lock != null) {
                lock.dec();
                if (lock.getCount() <= 0) {
                    lockMap.remove(mdPath);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    static boolean releaseLock(final PathLockMonitor lock) {
        return releaseLock(lock.getPath());
    }

    PathLock(final String path) {
        this.lock = new PathLockMonitor(path);
    }

    PathLockMonitor lock;
    int count = 0;

    private PathLockMonitor getLock() {
        return lock;
    }

    private synchronized int getCount() {
        return count;
    }

    private synchronized void inc() {
        count++;
    }

    private synchronized void dec() {
        count--;
    }

    static class PathLockMonitor {
        private final String path;

        public PathLockMonitor(final String path) {
            super();
            this.path = path;
        }

        public String getPath() {
            return path;
        }

    }

}
