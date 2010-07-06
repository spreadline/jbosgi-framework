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

import java.lang.reflect.Method;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.classloading.DeploymentMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.osgi.framework.metadata.OSGiMetaData;

/**
 * A deployer that attaches {@link DeploymentMetaData}, which is used to mark
 * the deployment as "lazy-resolve".
 * 
 * This automatically resolves the classloaders if needed to resolve other bundles.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-Mar-2010
 */
public class OSGiDeploymentMetaDataDeployer extends AbstractRealDeployer
{
   private static Class<?> dmdClass;
   
   static
   {
      ClassLoader cl = OSGiDeploymentMetaDataDeployer.class.getClassLoader();
      try
      {
         dmdClass = cl.loadClass("org.jboss.deployers.plugins.classloading.DeploymentMetaData");
      }
      catch (ClassNotFoundException e)
      {
         try
         {
            dmdClass = cl.loadClass("org.jboss.deployers.spi.classloading.DeploymentMetaData");
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException("Cannot load DeploymentMetaData class", cnfe);
         }
      }
   }
   
   public OSGiDeploymentMetaDataDeployer()
   {
      setInput(OSGiMetaData.class);
      addInput(dmdClass);
      addOutput(dmdClass);
      setStage(DeploymentStages.POST_PARSE);
      setTopLevelOnly(true);
   }

   @Override
   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit.isAttachmentPresent(dmdClass))
         return;

      // [TODO] Restore the code below when JBoss-6.0.0.M3 stops being supported
      // http://community.jboss.org/thread/153008
      
      //DeploymentMetaData deploymentMetaData = new DeploymentMetaData();
      //unit.addAttachment(DeploymentMetaData.class, deploymentMetaData);
      //deploymentMetaData.setLazyResolve(true);

      // Incompatible change in jboss-deployers
      // http://community.jboss.org/thread/153008
      try
      {
         Object deploymentMetaData = dmdClass.newInstance();
         unit.addAttachment(dmdClass.getName(), deploymentMetaData);
         Method setLazyResolve = dmdClass.getMethod("setLazyResolve", boolean.class);
         setLazyResolve.invoke(deploymentMetaData, true);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error handling depoyment metadata", e);
      }
   }
}