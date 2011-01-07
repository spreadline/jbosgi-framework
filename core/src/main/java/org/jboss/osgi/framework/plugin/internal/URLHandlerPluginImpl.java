/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.osgi.framework.plugin.internal;

import java.lang.reflect.Field;
import java.net.URLConnection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.osgi.framework.bundle.BundleManager;
import org.jboss.osgi.framework.bundle.BundleManager.IntegrationMode;
import org.jboss.osgi.framework.plugin.AbstractPlugin;
import org.jboss.osgi.framework.plugin.ModuleManagerPlugin;
import org.jboss.osgi.framework.plugin.URLHandlerPlugin;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLHandlerPluginImpl extends AbstractPlugin implements URLHandlerPlugin
{
   private final Logger log = Logger.getLogger(URLHandlerPluginImpl.class);

   public URLHandlerPluginImpl(BundleManager bundleManager)
   {
      super(bundleManager);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public void startPlugin()
   {
      ModuleManagerPlugin moduleManager = getPlugin(ModuleManagerPlugin.class);
      ModuleIdentifier frameworkModuleIdentifier = getBundleManager().getSystemBundle().getModuleIdentifier();
      Module frameworkModule = moduleManager.getModule(frameworkModuleIdentifier);

      if (getBundleManager().getIntegrationMode() == IntegrationMode.STANDALONE)
      {
         try
         {
            // TODO the OSGiModuleLoader is aware of our system module but the system module loader isn't
            // this causes issues in standalone mode because the ModularURLStreamHandlerFactory looks for the
            // module in the System Module Loader.

            // Terrible hack to make the module system aware of the OSGi framework module
            // another option could be to set the OSGiModuleLoader to be the system module loader by specifying
            // its class name in the system.module.loader system property.
            Field keyField = Module.class.getDeclaredField("myKey");
            keyField.setAccessible(true);
            Object fm = keyField.get(frameworkModule);

            Field mmapf = ModuleLoader.class.getDeclaredField("moduleMap");
            mmapf.setAccessible(true);
            ModuleLoader moduleLoader = getBundleManager().getSystemModuleLoader();
            Map mmap = (Map)mmapf.get(moduleLoader);
            mmap.put(frameworkModuleIdentifier, fm);
         }
         catch (Exception e)
         {
            // no point in doing anything intelligent here, instead we should get a proper
            // solution to the above...
            e.printStackTrace();
         }
      }
      URLHandlerFactory.setSystemBundleContext(getBundleManager().getSystemContext());

      try
      {
         // TODO I would expect JBoss Modules to set this one too.
         URLConnection.setContentHandlerFactory(new URLContentHandlerFactoryDelegate());
      }
      catch (Error e)
      {
         log.warn("Unable to set the ContentHandlerFactory on the URLConnection.", e);
      }
      URLContentHandlerFactoryDelegate.setDelegate(new URLContentHandlerFactory(getBundleManager().getSystemContext()));

      String val = System.getProperty("jboss.protocol.handler.modules");
      if (val == null)
      {
         val = frameworkModuleIdentifier.getName();
      }
      else
      {
         val += "|" + frameworkModuleIdentifier.getName();
      }
      System.setProperty("jboss.protocol.handler.modules", val);
   }
}
