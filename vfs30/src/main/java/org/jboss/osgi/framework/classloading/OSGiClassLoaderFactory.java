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

import java.util.List;

import org.jboss.classloader.spi.ClassLoaderPolicy;
import org.jboss.classloader.spi.ClassLoaderPolicyFactory;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.classloader.VFSDeploymentClassLoaderPolicyModule;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.osgi.framework.bundle.AbstractBundleState;
import org.jboss.vfs.VirtualFile;

/**
 * OSGiClassLoaderFactory
 * 
 * Creates the OSGiClassLoader
 * 
 * @author thomas.diesler@jboss.com
 * @since 11-Sep-2209
 */
public class OSGiClassLoaderFactory implements ClassLoaderFactory
{
   /** The classloader system */
   private ClassLoaderSystem system;

   public void setSystem(ClassLoaderSystem system)
   {
      this.system = system;
   }

   public ClassLoader createClassLoader(final DeploymentUnit unit) throws Exception
   {
      if (unit instanceof VFSDeploymentUnit == false)
         throw new IllegalStateException("Not an instance of VFSDeploymentUnit: " + unit);
      
      Module module = unit.getAttachment(Module.class);
      if (module instanceof VFSDeploymentClassLoaderPolicyModule == false)
         throw new IllegalStateException("Not an instance of VFSDeploymentClassLoaderPolicyModule: " + module);

      VFSDeploymentClassLoaderPolicyModule vfsModule = (VFSDeploymentClassLoaderPolicyModule)module;
      vfsModule.setPolicyFactory(new ClassLoaderPolicyFactory()
      {
         public ClassLoaderPolicy createClassLoaderPolicy()
         {
            VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)unit;
            AbstractBundleState bundleState = unit.getAttachment(AbstractBundleState.class);
            VirtualFile[] roots = getClassLoaderPolicyRoots(bundleState, vfsUnit);
            ClassLoaderPolicy policy = new OSGiClassLoaderPolicy(bundleState, roots);
            unit.addAttachment(ClassLoaderPolicy.class, policy);
            return policy;
         }

         private VirtualFile[] getClassLoaderPolicyRoots(AbstractBundleState bundleState, VFSDeploymentUnit vfsUnit)
         {
            // The classpath is initialised by the bundle structure deployer
            List<VirtualFile> classPaths = vfsUnit.getClassPath();
            VirtualFile[] policyRoots = new VirtualFile[classPaths.size()];
            classPaths.toArray(policyRoots);
            return policyRoots;
         }
      });
      return vfsModule.registerClassLoaderPolicy(system);
   }

   public void removeClassLoader(DeploymentUnit unit) throws Exception
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null)
         return;

      ClassLoader classLoader = unit.getClassLoader();
      try
      {
         // Remove the classloader
         system.unregisterClassLoader(classLoader);
      }
      finally
      {
         module.reset();
      }
   }
}
