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
package org.jboss.osgi.framework.deployers;

import java.util.jar.Manifest;

import org.jboss.deployers.vfs.spi.deployer.ManifestDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.osgi.framework.metadata.OSGiMetaData;
import org.jboss.osgi.framework.metadata.internal.AbstractOSGiMetaData;
import org.jboss.osgi.spi.OSGiConstants;
import org.jboss.virtual.VirtualFile;

/**
 * OSGiManifestParsingDeployer.<p>
 * 
 * This deployer attaches OSGiMetaData to the deployment.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 1.1 $
 */
public class OSGiManifestParsingDeployer extends ManifestDeployer<OSGiMetaData>
{
   public OSGiManifestParsingDeployer()
   {
      super(OSGiMetaData.class);
      setTopLevelOnly(true);
   }

   @Override
   protected OSGiMetaData createMetaData(Manifest manifest) throws Exception
   {
      AbstractOSGiMetaData metaData = new AbstractOSGiMetaData(manifest);
      
      // At least one of these manifest headers must be there
      // Note, in R3 and R4 there is no common mandatory header
      String bundleName = metaData.getBundleName();
      String bundleVersion = metaData.getBundleVersion();
      String bundleSymbolicName = metaData.getBundleSymbolicName();
      if (bundleName == null && bundleVersion == null && bundleSymbolicName == null)
         metaData = null;
      
      return metaData;
   }

   @Override
   protected void init(VFSDeploymentUnit unit, OSGiMetaData metaData, VirtualFile file) throws Exception
   {
      super.init(unit, metaData, file);

      String symbolicName = metaData.getBundleSymbolicName();
      if (symbolicName != null)
      {
         // Add a marker that this is an R4 OSGi deployment
         log.debug("Bundle-SymbolicName: " + symbolicName);
         unit.addAttachment(OSGiConstants.KEY_BUNDLE_SYMBOLIC_NAME, symbolicName);
      }
   }
}
