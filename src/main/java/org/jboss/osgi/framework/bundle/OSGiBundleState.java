/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.osgi.framework.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.osgi.framework.metadata.OSGiMetaData;
import org.jboss.osgi.framework.plugins.PackageAdminPlugin;
import org.jboss.virtual.VirtualFile;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * BundleState.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class OSGiBundleState extends AbstractBundleState
{
   /** Used to generate a unique id */
   private static final AtomicLong bundleIDGenerator = new AtomicLong();

   /** The bundle id */
   private long bundleId;

   /** The bundle location */
   private String location;

   /** The deployment unit */
   private DeploymentUnit unit;

   /**
    * Create a new BundleState.
    * 
    * @param location The string representation of this bundle's location identifier 
    * @param osgiMetaData the osgi metadata
    * @param unit the deployment unit
    * @throws IllegalArgumentException for a null parameter
    */
   public OSGiBundleState(String location, OSGiMetaData osgiMetaData, DeploymentUnit unit)
   {
      super(osgiMetaData);
      
      if (location == null)
         throw new IllegalArgumentException("Null bundle location");
      if (unit == null)
          throw new IllegalArgumentException("Null deployment unit");

      this.unit = unit;
      this.location = location;
      
      this.bundleId = bundleIDGenerator.incrementAndGet();
      unit.getMutableMetaData().addMetaData(unit, DeploymentUnit.class);
   }

   protected Set<ControllerContext> getRegisteredContexts()
   {
      return getBundleManager().getRegisteredContext(this);
   }

   public long getBundleId()
   {
      return bundleId;
   }

   /**
    * Get the unit.
    * 
    * @return the unit.
    */
   public DeploymentUnit getDeploymentUnit()
   {
      return unit;
   }

   public String getLocation()
   {
      return location;
   }

   public URL getEntry(String path)
   {
      checkInstalled();
      if (noAdminPermission(AdminPermission.RESOURCE))
         return null;

      DeploymentUnit unit = getDeploymentUnit();
      if (unit instanceof VFSDeploymentUnit)
      {
         VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit)unit;

         if (path.startsWith("/"))
            path = path.substring(1);
         return vfsDeploymentUnit.getResourceLoader().getResource(path);
      }
      return null;
   }

   @SuppressWarnings("rawtypes")
   public Enumeration getEntryPaths(String path)
   {
      checkInstalled();
      if (noAdminPermission(AdminPermission.RESOURCE))
         return null;

      DeploymentUnit unit = getDeploymentUnit();
      if (unit instanceof VFSDeploymentUnit)
      {
         VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit)unit;
         VirtualFile root = vfsDeploymentUnit.getRoot();
         if (path.startsWith("/"))
            path = path.substring(1);
         try
         {
            VirtualFile child = root.getChild(path);
            if (child != null)
               return new VFSEntryPathsEnumeration(root, child);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Error determining entry paths for " + root + " path=" + path);
         }

      }
      return null;
   }

   @SuppressWarnings("rawtypes")
   public Enumeration findEntries(String path, String filePattern, boolean recurse)
   {
      if (path == null)
         throw new IllegalArgumentException("Null path");

      checkInstalled();
      if (noAdminPermission(AdminPermission.RESOURCE))
         return null;

      // [TODO] fragments
      resolveBundle();

      if (filePattern == null)
         filePattern = "*";

      DeploymentUnit unit = getDeploymentUnit();
      if (unit instanceof VFSDeploymentUnit)
      {
         VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit)unit;
         VirtualFile root = vfsDeploymentUnit.getRoot();
         if (path.startsWith("/"))
            path = path.substring(1);
         try
         {
            VirtualFile child = root.getChild(path);
            if (child != null)
               return new VFSFindEntriesEnumeration(root, child, filePattern, recurse);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Error finding entries for " + root + " path=" + path + " pattern=" + filePattern + " recurse=" + recurse);
         }

      }
      return null;
   }

   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      checkInstalled();
      checkAdminPermission(AdminPermission.CLASS);
      // [TODO] bundle fragment

      if (resolveBundle() == false)
         throw new ClassNotFoundException("Cannot load class: " + name);

      ClassLoader classLoader = getDeploymentUnit().getClassLoader();
      return classLoader.loadClass(name);
   }

   /**
    * Try to resolve the bundle
    * @return true when resolved
    */
   boolean resolveBundle()
   {
      PackageAdminPlugin packageAdmin = getBundleManager().getPlugin(PackageAdminPlugin.class);
      return packageAdmin.resolveBundles(new Bundle[] { this });
   }

   public URL getResource(String name)
   {
      checkInstalled();
      if (noAdminPermission(AdminPermission.RESOURCE))
         return null;
      
      // [TODO] bundle fragment
      if (resolveBundle() == false)
         return getDeploymentUnit().getResourceLoader().getResource(name);
      
      return getDeploymentUnit().getClassLoader().getResource(name);
   }

   @SuppressWarnings("rawtypes")
   public Enumeration getResources(String name) throws IOException
   {
      checkInstalled();
      if (noAdminPermission(AdminPermission.RESOURCE))
         return null;

      // [TODO] bundle fragment 
      if (resolveBundle() == false)
         return getDeploymentUnit().getResourceLoader().getResources(name);
      
      return getDeploymentUnit().getClassLoader().getResources(name);
   }

   // [TODO] options
   public void start(int options) throws BundleException
   {
      checkInstalled();
      checkAdminPermission(AdminPermission.EXECUTE);

      if (getState() == ACTIVE)
         return;

      getBundleManager().startBundle(this);
   }

   // [TODO] options
   public void stop(int options) throws BundleException
   {
      checkInstalled();
      checkAdminPermission(AdminPermission.EXECUTE);

      if (getState() != ACTIVE)
         return;

      getBundleManager().stopBundle(this);
   }

   /**
    * Start internal
    * 
    * [TODO] Start Level Service & START_TRANSIENT? 
    * [TODO] START_ACTIVATION_POLICY 
    * [TODO] LAZY_ACTIVATION 
    * [TODO] locks 
    * [TODO] options
    * 
    * @throws Throwable for any error
    */
   public void startInternal() throws BundleException
   {
      // If this bundle's state is UNINSTALLED then an IllegalStateException is thrown. 
      if (getState() == Bundle.UNINSTALLED)
         throw new IllegalStateException("Bundle already uninstalled: " + this);
      
      // [TODO] If this bundle is in the process of being activated or deactivated then this method must wait for activation or deactivation 
      // to complete before continuing. If this does not occur in a reasonable time, a BundleException is thrown to indicate this bundle was 
      // unable to be started.
      
      // If this bundle's state is ACTIVE then this method returns immediately. 
      if (getState() == Bundle.ACTIVE)
         return;

      // [TODO] If the START_TRANSIENT option is not set then set this bundle's autostart setting to Started with declared activation  
      // if the START_ACTIVATION_POLICY option is set or Started with eager activation if not set. When the Framework is restarted 
      // and this bundle's autostart setting is not Stopped, this bundle must be automatically started.
      
      // If this bundle's state is not RESOLVED, an attempt is made to resolve this bundle. If the Framework cannot resolve this bundle, 
      // a BundleException is thrown.
      if (getState() != Bundle.RESOLVED)
      {
         try
         {
            getBundleManager().resolveBundle(this, true);
         }
         catch (RuntimeException ex)
         {
            throw new BundleException("Cannot resolve bundle: " + this, ex);
         }
      }
      
      // [TODO] If the START_ACTIVATION_POLICY option is set and this bundle's declared activation policy is lazy then:
      //    * If this bundle's state is STARTING then this method returns immediately.
      //    * This bundle's state is set to STARTING.
      //    * A bundle event of type BundleEvent.LAZY_ACTIVATION is fired.
      //    * This method returns immediately and the remaining steps will be followed when this bundle's activation is later triggered.
      
      
      // This bundle's state is set to STARTING
      // A bundle event of type BundleEvent.STARTING is fired
      createBundleContext();
      changeState(STARTING);

      // The BundleActivator.start(org.osgi.framework.BundleContext) method of this bundle's BundleActivator, if one is specified, is called. 
      try
      {
         OSGiMetaData metaData = getOSGiMetaData();
         if (metaData == null)
            throw new IllegalStateException("Cannot obtain OSGi meta data");

         // Do we have a bundle activator
         String bundleActivatorClassName = metaData.getBundleActivator();
         if (bundleActivatorClassName != null)
         {
            Object result = loadClass(bundleActivatorClassName).newInstance();
            if (result instanceof BundleActivator == false)
               throw new BundleException(bundleActivatorClassName + " is not an implementation of " + BundleActivator.class.getName());

            // Attach so we can call BundleActivator.stop() on this instance
            BundleActivator bundleActivator = (BundleActivator)result;
            unit.addAttachment(BundleActivator.class, bundleActivator);

            bundleActivator.start(getBundleContext());
         }

         if (getState() != STARTING)
            throw new BundleException("Bundle has been uninstalled: " + this);

         changeState(ACTIVE);
      }
      
      // If the BundleActivator is invalid or throws an exception then:
      //   * This bundle's state is set to STOPPING.
      //   * A bundle event of type BundleEvent.STOPPING is fired.
      //   * Any services registered by this bundle must be unregistered.
      //   * Any services used by this bundle must be released.
      //   * Any listeners registered by this bundle must be removed.
      //   * This bundle's state is set to RESOLVED.
      //   * A bundle event of type BundleEvent.STOPPED is fired.
      //   * A BundleException is then thrown.
      catch (Throwable t)
      {
         // This bundle's state is set to STOPPING
         // A bundle event of type BundleEvent.STOPPING is fired
         changeState(STOPPING);
         
         // Any services registered by this bundle must be unregistered.
         // Any services used by this bundle must be released.
         // Any listeners registered by this bundle must be removed.
         stopInternal();
         
         destroyBundleContext();
         changeState(RESOLVED);
         
         // A bundle event of type BundleEvent.STOPPED is fired
         
         if (t instanceof BundleException)
            throw (BundleException)t;
         
         throw new BundleException("Cannot start bundle: " + this, t);
      }
   }

   /**
    * Stop Internal
    */
   public void stopInternal() throws BundleException
   {
      // If this bundle's state is UNINSTALLED then an IllegalStateException is thrown. 
      if (getState() == Bundle.UNINSTALLED)
         throw new IllegalStateException("Bundle already uninstalled: " + this);

      // [TODO] If this bundle is in the process of being activated or deactivated then this method must wait for activation or deactivation 
      // to complete before continuing. If this does not occur in a reasonable time, a BundleException is thrown to indicate this bundle 
      // was unable to be stopped.
      
      // [TODO] If the STOP_TRANSIENT option is not set then then set this bundle's persistent autostart setting to to Stopped. 
      // When the Framework is restarted and this bundle's autostart setting is Stopped, this bundle must not be automatically started. 

      // If this bundle's state is not STARTING or ACTIVE then this method returns immediately
      if (getState() != Bundle.STARTING && getState() != Bundle.ACTIVE)
         return;

      // This bundle's state is set to STOPPING
      // A bundle event of type BundleEvent.STOPPING is fired
      int priorState = getState();
      changeState(STOPPING);

      // If this bundle's state was ACTIVE prior to setting the state to STOPPING, 
      // the BundleActivator.stop(org.osgi.framework.BundleContext) method of this bundle's BundleActivator, if one is specified, is called. 
      // If that method throws an exception, this method must continue to stop this bundle and a BundleException must be thrown after completion 
      // of the remaining steps.
      Throwable rethrow = null;
      if (priorState == Bundle.ACTIVE)
      {
            BundleActivator bundleActivator = getDeploymentUnit().getAttachment(BundleActivator.class);
            BundleContext bundleContext = getBundleContext();
            if (bundleActivator != null && bundleContext != null)
            {
               try
               {
                  bundleActivator.stop(bundleContext);
               }
               catch (Throwable t)
               {
                  rethrow = t;
               }
            }
      }
      
      // Any services registered by this bundle must be unregistered
      getBundleManager().unregisterContexts(this);

      // Any services used by this bundle must be released
      for (ControllerContext context : getUsedContexts(this))
      {
         int count = getUsedByCount(context, this);
         while (count > 0)
         {
            try
            {
               getBundleManager().ungetContext(this, context);
            }
            catch (Throwable t)
            {
               log.debug("Error ungetting service: " + context, t);
            }
            count--;
         }
      }

      // [TODO] Any listeners registered by this bundle must be removed
      
      // If this bundle's state is UNINSTALLED, because this bundle was uninstalled while the 
      // BundleActivator.stop method was running, a BundleException must be thrown
      if (getState() == Bundle.UNINSTALLED)
         throw new BundleException("Bundle uninstalled during activator stop: " + this);
      
      // This bundle's state is set to RESOLVED
      destroyBundleContext();
      changeState(RESOLVED);
      
      if (priorState != STOPPING)
         throw new BundleException("Bundle has been uninstalled: " + getCanonicalName());

      // [TODO] A bundle event of type BundleEvent.STOPPED is fired
      
      if (rethrow != null)
         throw new BundleException("Error during stop of bundle: " + this, rethrow);
   }

   public void update(InputStream in) throws BundleException
   {
      checkAdminPermission(AdminPermission.LIFECYCLE); // [TODO] extension bundles
      // [TODO] update
      throw new UnsupportedOperationException("update");
   }

   public void uninstall() throws BundleException
   {
      checkAdminPermission(AdminPermission.LIFECYCLE); // [TODO] extension bundles
      getBundleManager().uninstallBundle(this);
   }

   @Override
   protected void afterServiceRegistration(OSGiServiceState service)
   {
      getBundleManager().putContext(service, unit);
   }

   @Override
   protected void beforeServiceUnregistration(OSGiServiceState service)
   {
      getBundleManager().removeContext(service, unit);
   }

   public static OSGiBundleState assertBundleState(Bundle bundle)
   {
      if (bundle == null)
         throw new IllegalArgumentException("Null bundle");
      
      if (bundle instanceof OSGiBundleWrapper)
         bundle = ((OSGiBundleWrapper)bundle).getBundleState();
   
      if (bundle instanceof OSGiBundleState == false)
         throw new IllegalArgumentException("Not an OSGiBundleState: " + bundle);
   
      return (OSGiBundleState)bundle;
   }
}
