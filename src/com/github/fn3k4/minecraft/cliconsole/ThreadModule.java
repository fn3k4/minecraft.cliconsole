package com.github.fn3k4.minecraft.cliconsole;

/**
 * This class implements Runnable interface
 * and provides some additional support for thread running
 */
public abstract class ThreadModule implements Runnable {

    private boolean fieldShouldStop = false;

    private Thread fieldSleepingThread;

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public abstract void run();

    /**
     * Should NOT be sychronized.
     * @param timeout
     */
    public void sleep(final long timeout) {
        try {
            fieldSleepingThread = Thread.currentThread();
            if (!hasToStop() && timeout > 0) {
                Thread.sleep(timeout);
            }
        } catch (final InterruptedException e) {
        }
    }

    /**
     * @return
     */
    public boolean hasToStop() {
        return fieldShouldStop;
    }

    /** Signal to stop the collector thread */
    public synchronized void stop() {
        fieldShouldStop = true;
        if (fieldSleepingThread != null && !fieldSleepingThread.isInterrupted()) {
            fieldSleepingThread.interrupt();
        }
    }

}
