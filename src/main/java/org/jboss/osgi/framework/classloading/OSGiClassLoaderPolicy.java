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
import java.util.HashMap;
import java.util.Map;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.vfs.policy.VFSClassLoaderPolicy;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.classloader.VFSDeploymentClassLoaderPolicyModule;
import org.jboss.osgi.framework.bundle.AbstractDeployedBundleState;
import org.jboss.osgi.framework.deployers.OSGiBundleNativeCodeDeployer;
import org.jboss.virtual.VirtualFile;

/**
 * The ClassLoaderPolicy for OSGi bundles.
 * 
 * This implementation supports the notion of OSGi Native Code Libraries.
 * The library map is initialized in {@link OSGiBundleNativeCodeDeployer}.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 11-Sep-2209
 */
public class OSGiClassLoaderPolicy extends VFSClassLoaderPolicy
{
   private Map<String, File> libraryMap = new HashMap<String, File>();
   
   public OSGiClassLoaderPolicy(AbstractDeployedBundleState bundleState, VirtualFile[] roots)
   {
      super(roots);
      
      if (bundleState == null)
         throw new IllegalArgumentException("Null bundleState");

      DeploymentUnit unit = bundleState.getDeploymentUnit();
      Module module = unit.getAttachment(Module.class);
      if (module instanceof VFSDeploymentClassLoaderPolicyModule == false)
         throw new IllegalStateException("Module is not an instance of " + VFSDeploymentClassLoaderPolicyModule.class.getName() + " actual=" + module);

      VFSDeploymentClassLoaderPolicyModule vfsModule = (VFSDeploymentClassLoaderPolicyModule)module;
      String[] packageNames = vfsModule.getPackageNames();
      setExportedPackages(packageNames);
      setIncluded(vfsModule.getIncluded());
      setExcluded(vfsModule.getExcluded());
      setExcludedExport(vfsModule.getExcludedExport());
      setExportAll(vfsModule.getExportAll());
      setImportAll(vfsModule.isImportAll());
      setCacheable(vfsModule.isCacheable());
      setBlackListable(vfsModule.isBlackListable());
      setDelegates(vfsModule.getDelegates());
   }

   public void addLibraryMapping(String libname, File libfile)
   {
      libraryMap.put(libname, libfile);
   }

   public String findLibrary(String libname)
   {
      File libfile = libraryMap.get(libname);
      
      // [TODO] why does the TCK use 'Native' to mean 'libNative' ? 
      if (libfile == null)
         libfile = libraryMap.get("lib" + libname);
         
      return (libfile != null ? libfile.getAbsolutePath() : null);
   }
}
