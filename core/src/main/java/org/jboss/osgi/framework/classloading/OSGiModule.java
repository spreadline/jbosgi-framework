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

import java.util.List;
import java.util.Set;

import org.jboss.classloader.spi.ClassLoaderPolicy;
import org.jboss.classloader.spi.DelegateLoader;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.classloader.VFSDeploymentClassLoaderPolicyModule;
import org.jboss.logging.Logger;

/**
 * The {@link Module} that represents and OSGi bundle deployment.
 * 
 * @author thomas.diesler@jboss.com
 * @version $Revision$
 */
public class OSGiModule extends VFSDeploymentClassLoaderPolicyModule
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiModule.class);

   private static final long serialVersionUID = 1L;

   public OSGiModule(DeploymentUnit unit, ClassLoadingMetaData metaData)
   {
      super(unit);
      if (log.isTraceEnabled())
         log.trace("new OSGiModule\n  " + unit + "\n  " + metaData);
   }

   @Override
   public ClassLoaderPolicy createClassLoaderPolicy()
   {
      throw new IllegalStateException("OSGiClassLoaderFactory is expected to create the policy");
   }

   @Override
   protected void addDelegates(Module module, List<DelegateLoader> delegates, List<DelegateLoader> dynamic, Set<Module> visited, boolean reExport)
   {
      super.addDelegates(module, delegates, dynamic, visited, reExport);
      if (log.isTraceEnabled())
         log.trace("Add delegates for module: " + this + "\n  delegates: " + delegates + "\n  dynamic: " + dynamic);
   }
}
