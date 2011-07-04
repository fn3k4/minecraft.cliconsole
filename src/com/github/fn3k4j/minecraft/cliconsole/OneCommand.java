/**
 * Abstract: CliConsoleMain.java
 *
 * @author: fn3k4j
 * @date: Mar 11, 2011
 */
package com.github.fn3k4j.minecraft.cliconsole;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * 
 */
public class OneCommand {

    private MBeanServerConnection fieldMbsc;

    private ObjectName fieldCcMBeanName;

    private String fieldHost = "localhost";

    private int fieldPort = 9999;

    /**
     * 
     */
    public void execute(final String[] cmdz) {
        String command = cmdz[0];
        for (int i = 1; i < cmdz.length; i++) {
            command += " " + cmdz[i];
        }

        try {
            init();

            fieldMbsc.invoke(fieldCcMBeanName, "receiveCommand", //
                    new Object[] { command }, //
                    new String[] { "java.lang.String" });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fieldCcMBeanName = null;
        }
    }

    /**
     * 
     */
    private void init() {

        try {
            JMXServiceURL url = new JMXServiceURL( //
                    CliConsole.RMI_URL_PREFIX + getHost() + ":" + getPort() + "/" + CliConsole.DEFAULT_SERVICE_NAME);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            fieldMbsc = jmxc.getMBeanServerConnection();

            fieldCcMBeanName = new ObjectName(CliConsole.DEFAULT_DOMAIN + ":type=" + CliConsole.OBJECT_TYPE);
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
        final OneCommand main = new OneCommand();
        if (args.length < 1) {
            System.out.println("Usage: java " + OneCommand.class.getCanonicalName() + " <command>");
        }
        main.execute(args);
    }

    /**
     * @param s
     */
    public static void logClient(String s) {
        System.out.println("[Client] " + s);
    }
}
