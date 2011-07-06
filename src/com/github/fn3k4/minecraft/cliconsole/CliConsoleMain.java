/**
 * Abstract: CliConsoleMain.java
 *
 * @author: fn3k4
 * @date: Mar 11, 2011
 */
package com.github.fn3k4.minecraft.cliconsole;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * 
 */
public class CliConsoleMain extends ThreadModule {

    private MBeanServerConnection fieldMbsc;

    private ObjectName fieldCcMBeanName;

    private String fieldHost = "localhost";

    private int fieldPort = 9999;

    /**
     * @see com.github.fn3k4.minecraft.cliconsole.ThreadModule#run()
     */
    @Override
    public void run() {
        ClientListener listener = new ClientListener();
        try {
            init();

            fieldMbsc.addNotificationListener(fieldCcMBeanName, listener, null, null);

            BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while (!hasToStop() && (line = rdr.readLine()) != null) {
                // self commands
                if (line.startsWith(":")) {
                    if (line.equalsIgnoreCase(":quit")) {
                        stop();
                    } else {
                        logClient("Unknown self command");
                    }
                } else {
                    // server commands
                    fieldMbsc.invoke(fieldCcMBeanName, "receiveCommand", //
                            new Object[] { line }, //
                            new String[] { "java.lang.String" });
                }
                Thread.yield();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fieldMbsc.removeNotificationListener(fieldCcMBeanName, listener);
            } catch (Exception e) {
            }
            fieldCcMBeanName = null;
        }
    }

    /**
     * 
     */
    private void init() {

        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));

        try {
            JMXServiceURL url = new JMXServiceURL( //
                    CliConsole.RMI_URL_PREFIX + getHost() + ":" + getPort() + "/"
                            + CliConsole.DEFAULT_SERVICE_NAME);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            final NotificationListener listener = new NotificationListener() {

                public void handleNotification(Notification notif, Object handback) {
                    if ("jmx.remote.connection.closed".equals(notif.getType())) {
                        logClient("[JMXConnector] " + notif.getMessage());
                        System.exit(0);
                    }
                }
            };

            jmxc.addConnectionNotificationListener(listener, null, null);

            fieldMbsc = jmxc.getMBeanServerConnection();

            fieldCcMBeanName = new ObjectName(CliConsole.DEFAULT_DOMAIN + ":type="
                    + CliConsole.OBJECT_TYPE);
            if (!fieldMbsc.isRegistered(fieldCcMBeanName)) {
                logClient(fieldCcMBeanName + " is NOT registered. Exiting.");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return fieldHost;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        fieldHost = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return fieldPort;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        fieldPort = port;
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) {
        logClient("CliConsole started.");
        final CliConsoleMain main = new CliConsoleMain();
        new Thread(main).start();

    }

    /**
     * @param s
     */
    public static void logClient(String s) {
        System.out.println("[Client] " + s);
    }

    public static class ClientListener implements NotificationListener {

        /**
         * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
         */
        public void handleNotification(Notification notification, Object handback) {
            System.out.println("[Server] " + notification.getMessage());
        }
    }

    /**
     * 
     */
    public class ShutdownHook extends Thread {

        private CliConsoleMain fieldInstance;

        public ShutdownHook(CliConsoleMain instance) {
            fieldInstance = instance;
        }

        @Override
        public void run() {
            fieldInstance.stop();
            logClient("stopped.");
        }
    }
}
