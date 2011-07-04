/**
 * Abstract: CliLogHandler.java
 *
 * @author: fn3k4j
 * @date: Apr 29, 2011
 */
package com.github.fn3k4j.minecraft.cliconsole;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 * Implements standard
 * @see java.util.logging.Handler
 */
public class CliLogHandler extends Handler {

    private NotificationBroadcasterSupport fieldBroadcaster;

    private AtomicLong fieldCounter = new AtomicLong(System.nanoTime());

    public CliLogHandler() {
    }

    /**
     * @param notificationEmitter
     */
    public CliLogHandler(final NotificationBroadcasterSupport nbs) {
        fieldBroadcaster = nbs;
    }

    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    @Override
    public void publish(final LogRecord record) {
        if (fieldBroadcaster == null || record == null) {
            return;
        }
        Notification ntf = new Notification("minecraft.cliconsole", fieldBroadcaster, fieldCounter.getAndIncrement(),
                record.getMessage());
        fieldBroadcaster.sendNotification(ntf);
    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    @Override
    public void flush() {
    }

    /**
     * @see java.util.logging.Handler#close()
     */
    @Override
    public void close() throws SecurityException {
        fieldBroadcaster = null;
    }

    /**
     * @return the broadcaster
     */
    public NotificationBroadcasterSupport getBroadcaster() {
        return fieldBroadcaster;
    }

    /**
     * @param broadcaster the broadcaster to set
     */
    public void setBroadcaster(NotificationBroadcasterSupport broadcaster) {
        fieldBroadcaster = broadcaster;
    }
}
