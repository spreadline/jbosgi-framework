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
import java.util.Map;

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
            Field keyField = Module.class.getDeclaredField("myKey");
            keyField.setAccessible(true);
            Object fm = keyField.get(frameworkModule);

            Field mmapf = ModuleLoader.class.getDeclaredField("moduleMap");
            mmapf.setAccessible(true);
            ModuleLoader moduleLoader = getBundleManager().getSystemModuleLoader();
            Map mmap = (Map)mmapf.get(moduleLoader);
            mmap.put(frameworkModuleIdentifier, fm);

            /*
            ModuleLoader ModuleLoader = getBundleManager().getSystemModuleLoader();
            Module m2 = ModuleLoader.loadModule(frameworkModuleIdentifier);
            System.out.println("### " + frameworkModule);
            System.out.println("### " + m2);

            // final ModuleIdentifier identifier = ModuleIdentifier.fromString("org.jboss.osgi.framework");
            Module osgiModule = Module.getSystemModuleLoader().loadModule(frameworkModuleIdentifier);
            final ServiceLoader<java.net.URLStreamHandlerFactory> loader = osgiModule.loadService(java.net.URLStreamHandlerFactory.class);
            for (java.net.URLStreamHandlerFactory factory : loader)
            {
               System.out.println("### factory: " + factory);
            }
            */
         }
         catch (Exception e)
         {
            // no point in doing anything intelligent here, instead we should get a proper
            // solution to the above...
            e.printStackTrace();
         }
      }
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

      /* 
      // Debug
      try
      {
         Field field = URL.class.getDeclaredField("factory");
         field.setAccessible(true);
         Object obj = field.get(null);
         Method meth = obj.getClass().getDeclaredMethod("createURLStreamHandler", String.class);
         meth.setAccessible(true);
         Object h = meth.invoke(obj, "protocol1");
         System.out.println("*** " + h);
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      */
   }
}
