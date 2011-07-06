/**
 * Abstract: CliConsole.java
 *
 * @author: fn3k4
 * @date: Mar 11, 2011
 */
package com.github.fn3k4.minecraft.cliconsole;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;

import net.minecraft.server.MinecraftServer;

import org.bukkit.util.config.Configuration;

/**
 * 
 */
@MXBean
public class CliConsole extends NotificationBroadcasterSupport implements CliConsoleMXBean {

    /** RMI_URL_PREFIX */
    public static final String RMI_URL_PREFIX = "service:jmx:rmi:///jndi/rmi://";

    /** DEFAULT_SERVICE_NAME */
    public static final String DEFAULT_SERVICE_NAME = "minecraft";

    /** DEFAULT_INTERFACE */
    public static final String DEFAULT_INTERFACE = "127.0.0.1";

    public static final int DEFAULT_PORT = 1099;

    public static final String OBJECT_TYPE = "CliConsole";

    public static final String DEFAULT_DOMAIN = "cliconsole";

    public static final String OBJECT_NAME = DEFAULT_DOMAIN + ":type=" + OBJECT_TYPE;

    public static final String MINECRAFT_LOGGER_NAME = "Minecraft";

    private static Logger logger = Logger.getLogger(MINECRAFT_LOGGER_NAME);

    private MBeanServer fieldMBeanServer;

    private JMXConnectorServer fieldConnectorServer;

    /** fieldRegistry */
    private Registry fieldRegistry;

    /** Flag indicates RMI interface is already started */
    protected AtomicBoolean started = new AtomicBoolean(false);

    private int fieldPort = DEFAULT_PORT;

    private MinecraftServer fieldMinecraftServer;

    private static CliConsole fieldnstance;

    private Configuration fieldConfiguration;

    /** Singletone constructor. */
    private CliConsole() {
    }

    /**
     * @return
     */
    public synchronized static CliConsole getInstance() {
        if (fieldnstance == null) {
            fieldnstance = new CliConsole();
        }
        return fieldnstance;
    }

    /**
     * @see com.github.fn3k4.minecraft.cliconsole.CliConsoleMXBean#receiveCommand(java.lang.String)
     */
    @Override
    public void receiveCommand(String cmd) {
        logger.info("CliConsole receive command: '" + cmd + "'");

        try {
            if (fieldMinecraftServer != null) {
                // this method put a command in the MC-server queue
                fieldMinecraftServer.issueCommand(cmd, fieldMinecraftServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** 
     * Create an RMI connector and start it. 
     * @throws IOException
     */
    public synchronized void startAgent() throws IOException {
        setConnectorServer(JMXConnectorServerFactory.newJMXConnectorServer(getUrl(), null, fieldMBeanServer));
        getConnectorServer().start();
        started.set(true);
    }

    /**
     * Stops agent.
     * @throws IOException
     */
    public synchronized void stopAgent() throws IOException {
        if (started.get()) {
            getConnectorServer().stop();
        }
    }

    private JMXConnectorServerMBean getConnectorServer() {
        return fieldConnectorServer;
    }

    private void setConnectorServer(JMXConnectorServer newJMXConnectorServer) {
        fieldConnectorServer = newJMXConnectorServer;
    }

    private JMXServiceURL getUrl() {
        JMXServiceURL url = null;
        //fieldConfiguration.getProperty(path);
        try {
            url = new JMXServiceURL(RMI_URL_PREFIX + DEFAULT_INTERFACE + ":" + getPort() + "/" + DEFAULT_SERVICE_NAME);
        } catch (MalformedURLException e) {
            logger.warning(e.getMessage());
        }
        return url;
    }

    /** Create registry. */
    protected synchronized Registry createRegistry() throws IOException {
        if (getPort() < 1024) {
            throw new IllegalArgumentException("Current port value <" + getPort() + "> is under 1024 watermark");
        }
        return LocateRegistry.createRegistry(getPort());
    }

    /**
     * Standalone start.
     */
    public void startStandalone() {
        if (started.get()) {
            logger.info("CliConsole is already started.");
            return;
        }

        setPort(getConfiguration().getInt("port", DEFAULT_PORT));

        // registerMBean in MBeanServer
        fieldMBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            // Construct the ObjectName for the MBean we will register
            ObjectName name = new ObjectName(OBJECT_NAME);
            // Register the CliConsole MBean
            fieldMBeanServer.registerMBean(this, name);
        } catch (Exception e) {
            logger.warning("Cannot register CliConsole MXBean: " + e.getMessage());
        }

        // start JMX agent
        try {
            setRegistry(createRegistry());
            startAgent();
            logger.info("CliConsole started. Listen on " + getUrl());
        } catch (final Exception e) {
            logger.warning(e.getMessage());
        }

        // set own Handler to common Logger to catch its output to notify a listeners
        logger.addHandler(new CliLogHandler(this));
    }

    /**
     * Standalone stop.
     */
    public void stopStandalone() {
        try {
            stopAgent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the registry
     */
    public Registry getRegistry() {
        return fieldRegistry;
    }

    /**
     * @param registry the registry to set
     */
    public void setRegistry(Registry registry) {
        fieldRegistry = registry;
    }

    /**
     * @return the mBeanServer
     */
    public MBeanServer getMBeanServer() {
        return fieldMBeanServer;
    }

    /**
     * @param mBeanServer the mBeanServer to set
     */
    public void setMBeanServer(MBeanServer mBeanServer) {
        fieldMBeanServer = mBeanServer;
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
     * @return the fieldMinecraftServer
     */
    public MinecraftServer getMinecraftServer() {
        return fieldMinecraftServer;
    }

    /**
     * @param fieldMinecraftServer the fieldMinecraftServer to set
     */
    public void setMinecraftServer(MinecraftServer srv) {
        fieldMinecraftServer = srv;
    }

    /**
     * @param configuration
     */
    public void setConfiguration(final Configuration configuration) {
        fieldConfiguration = configuration;
    }

    /**
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return fieldConfiguration;
    }

}
