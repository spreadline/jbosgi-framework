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
package org.jboss.osgi.framework.classloading;

// $Id$

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.classloader.spi.NativeLibraryProvider;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.vfs.policy.VFSClassLoaderPolicy;
import org.jboss.osgi.framework.bundle.AbstractBundleState;
import org.jboss.osgi.framework.bundle.AbstractDeployedBundleState;
import org.jboss.osgi.framework.bundle.OSGiBundleManager;
import org.jboss.osgi.framework.bundle.OSGiBundleState;
import org.jboss.osgi.framework.metadata.NativeLibrary;
import org.jboss.osgi.framework.metadata.NativeLibraryMetaData;
import org.jboss.osgi.framework.plugins.BundleStoragePlugin;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;

/**
 * The ClassLoaderPolicy for OSGi bundles.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 11-Sep-2209
 */
public class OSGiClassLoaderPolicy extends VFSClassLoaderPolicy
{
   public OSGiClassLoaderPolicy(AbstractBundleState absBundleState, VirtualFile[] roots)
   {
      super(roots);
      
      if (absBundleState == null)
         throw new IllegalArgumentException("Null bundleState");

      if (absBundleState instanceof AbstractDeployedBundleState)
      {
         AbstractDeployedBundleState depBundleState = (AbstractDeployedBundleState)absBundleState;
         Module module = depBundleState.getDeploymentUnit().getAttachment(Module.class);
         if (module instanceof OSGiModule == false)
            throw new IllegalStateException("Not an instance of OSGiModule: " + module);

         OSGiModule osgiModule = (OSGiModule)module;
         String[] packageNames = osgiModule.getPackageNames();
         setExportedPackages(packageNames);
         setIncluded(osgiModule.getIncluded());
         setExcluded(osgiModule.getExcluded());
         setExcludedExport(osgiModule.getExcludedExport());
         setExportAll(osgiModule.getExportAll());
         setImportAll(osgiModule.isImportAll());
         setCacheable(osgiModule.isCacheable());
         setBlackListable(osgiModule.isBlackListable());
         setDelegates(osgiModule.getDelegates());

         // NativeCode-Library handling
         OSGiClassLoadingMetaData classLoadingMetaData = osgiModule.getClassLoadingMetaData();
         NativeLibraryMetaData libMetaData = classLoadingMetaData.getNativeLibraries();
         if (libMetaData != null && libMetaData.getNativeLibraries() != null)
         {
            OSGiBundleState bundleState = (OSGiBundleState)absBundleState;
            
            // Add the native library mappings to the OSGiClassLoaderPolicy
            for (NativeLibrary library : libMetaData.getNativeLibraries())
            {
               String libpath = library.getLibraryPath();
               String libfile = new File(libpath).getName();
               String libname = libfile.substring(0, libfile.lastIndexOf('.'));
               
               // Add the library provider to the policy
               NativeLibraryProvider libProvider = new OSGiNativeLibraryProvider(bundleState, libname, libpath);
               addNativeLibrary(libProvider);
               
               // [TODO] why does the TCK use 'Native' to mean 'libNative' ? 
               if (libname.startsWith("lib"))
               {
                  libname = libname.substring(3);
                  libProvider = new OSGiNativeLibraryProvider(bundleState, libname, libpath);
                  addNativeLibrary(libProvider);
               }
            }
         }
      }
   }

   static class OSGiNativeLibraryProvider implements NativeLibraryProvider
   {
      private OSGiBundleState bundleState;
      private String libpath;
      private String libname;
      private File libraryFile;
      
      OSGiNativeLibraryProvider(OSGiBundleState bundleState, String libname, String libpath)
      {
         this.bundleState = bundleState;
         this.libpath = libpath;
         this.libname = libname;
         
         // If a native code library in a selected native code clause cannot be found
         // within the bundle then the bundle must fail to resolve
         URL entryURL = bundleState.getEntry(libpath);
         if (entryURL == null)
            throw new IllegalStateException("Cannot find native library: " + libpath);
      }
      
      public String getLibraryName()
      {
         return libname;
      }

      public String getLibraryPath()
      {
         return libpath;
      }
      
      public File getLibraryLocation() throws IOException
      {
         if (libraryFile == null)
         {
            // Get the virtual file for entry for the library
            VirtualFile fileSource = bundleState.getRoot().getChild(libpath);
            
            // Create a unique local file location
            libraryFile = getUniqueLibraryFile(bundleState, libpath);
            libraryFile.deleteOnExit();
            
            // Copy the native library to the bundle storage area
            FileOutputStream fos = new FileOutputStream(libraryFile);
            VFSUtils.copyStream(fileSource.openStream(), fos);
            fos.close();
         }
         return libraryFile;
      }

      private File getUniqueLibraryFile(final OSGiBundleState bundleState, final String libpath)
      {
         OSGiBundleManager bundleManager = bundleState.getBundleManager();
         String timestamp = new SimpleDateFormat("-yyyyMMdd-HHmmssSSS").format(new Date(bundleState.getLastModified()));
         String uniquePath = new StringBuffer(libpath).insert(libpath.lastIndexOf("."), timestamp).toString();
         BundleStoragePlugin plugin = bundleManager.getPlugin(BundleStoragePlugin.class);
         return plugin.getDataFile(bundleState, uniquePath);
      }
   }
}
