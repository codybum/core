package io.cresco.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

/**
 * Bundle Activator.<br/>
 * Looks up the Configuration Admin service and on activation will configure Pax Logging.
 * On deactivation will unconfigure Pax Logging.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.2.2, November 26, 2008
 */
public final class Activator
        implements BundleActivator
{

    /**
     * {@inheritDoc}
     * Configures Pax Logging via Configuration Admin.
     */
    public void start( final BundleContext bundleContext )
            throws Exception
    {
        updateConfiguration( bundleContext, "%5p [%t] - %m%n" );

        installInternalBundleJars(bundleContext,"org.osgi.service.cm-1.6.0.jar").start();
        Bundle loggerService = installInternalBundleJars(bundleContext,"pax-logging-service-1.10.1.jar");
        Bundle loggerAPI = installInternalBundleJars(bundleContext,"pax-logging-api-1.10.1.jar");
        loggerService.start();
        loggerAPI.start();

    }


    private Bundle installInternalBundleJars(BundleContext context, String bundleName) {

        Bundle installedBundle = null;
        try {
            URL bundleURL = getClass().getClassLoader().getResource(bundleName);
            if(bundleURL != null) {

                String bundlePath = bundleURL.getPath();
                installedBundle = context.installBundle(bundlePath,
                        getClass().getClassLoader().getResourceAsStream(bundleName));


            } else {
                System.out.println("Bundle = null");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            System.out.println("Failed to load bundle exiting!");
            System.exit(0);
        }

        return installedBundle;
    }

    /**
     * {@inheritDoc}
     * UnConfigures Pax Logging via Configuration Admin.
     */
    public void stop( final BundleContext bundleContext )
            throws Exception
    {
        updateConfiguration( bundleContext, "%-4r [%t] %-5p %c %x - %m%n" );
    }

    /**
     * Updates Pax Logging configuration to a specifid conversion pattern.
     *
     * @param bundleContext bundle context
     * @param pattern       layout conversion pattern
     *
     * @throws IOException - Re-thrown
     */
    private void updateConfiguration( BundleContext bundleContext,
                                      final String pattern )
            throws IOException
    {
        final ConfigurationAdmin configAdmin = getConfigurationAdmin( bundleContext );
        final Configuration configuration = configAdmin.getConfiguration( "org.ops4j.pax.logging", null );

        final Hashtable<String, Object> log4jProps = new Hashtable<String, Object>();
        log4jProps.put( "log4j.rootLogger", "INFO, CONSOLE" );
        log4jProps.put( "log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender" );
        log4jProps.put( "log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout" );
        log4jProps.put( "log4j.appender.CONSOLE.layout.ConversionPattern", pattern );

        log4jProps.put( "log4j.category.org.apache.activemq","ERROR");

        /*


        log4jProps.put( "log4j.logger.org.apache.activemq.spring","WARN");
        log4jProps.put( "log4j.logger.org.apache.activemq.web.handler","WARN");
        log4jProps.put( "log4j.logger.org.springframework","WARN");
        log4jProps.put( "log4j.logger.org.apache.xbean","WARN");
        log4jProps.put( "log4j.logger.org.apache.camel","INFO");
        log4jProps.put( "log4j.logger.org.eclipse.jetty","WARN");
        log4jProps.put( "log4j.logger.org.apache.activemq.broker","WARN");
        log4jProps.put( "log4j.logger.org.apache.activemq","ERROR");
*/

        configuration.update( log4jProps );
    }

    /**
     * Gets Configuration Admin service from service registry.
     *
     * @param bundleContext bundle context
     *
     * @return configuration admin service
     *
     * @throws IllegalStateException - If no Configuration Admin service is available
     */
    private ConfigurationAdmin getConfigurationAdmin( final BundleContext bundleContext )
    {
        final ServiceReference ref = bundleContext.getServiceReference( ConfigurationAdmin.class.getName() );
        if( ref == null )
        {
            throw new IllegalStateException( "Cannot find a configuration admin service" );
        }
        return (ConfigurationAdmin) bundleContext.getService( ref );
    }

}
