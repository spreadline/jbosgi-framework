/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General protected License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General protected License for more details.
 *
 * You should have received a copy of the GNU Lesser General protected
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.osgi.framework.testing;

// $Id: $

import java.util.ArrayList;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.osgi.framework.bundle.OSGiBundleManager;
import org.jboss.osgi.framework.deployers.AbstractDeployment;
import org.jboss.osgi.framework.launch.OSGiFramework;
import org.jboss.osgi.framework.metadata.OSGiMetaData;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.Archive;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * Parent for native framework tests.  
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 10-Mar-2010
 */
public abstract class AbstractFrameworkTest extends OSGiFrameworkTest
{
   protected OSGiBundleManager getBundleManager()
   {
      Framework framework;
      try
      {
         framework = getFramework();
      }
      catch (BundleException ex)
      {
         throw new IllegalStateException("Cannot get the framework", ex);
      }
      return ((OSGiFramework)framework).getBundleManager();
   }

   protected Kernel getKernel()
   {
      return getBundleManager().getKernel();
   }

   protected Object getBean(Object beanName)
   {
      return getBean(beanName, ControllerState.INSTALLED);
   }

   protected Object getBean(Object name, ControllerState state)
   {
      KernelControllerContext context = getControllerContext(name, state);
      return context.getTarget();
   }

   protected KernelControllerContext getControllerContext(Object name)
   {
      return getControllerContext(name, ControllerState.INSTALLED);
   }

   protected KernelControllerContext getControllerContext(Object name, ControllerState state)
   {
      KernelController controller = getKernel().getController();
      KernelControllerContext context = (KernelControllerContext)controller.getContext(name, state);
      if (context == null)
         throw new IllegalStateException("Bean not found " + name + " at state " + state);
      return context;
   }

   protected ControllerState change(KernelControllerContext context, ControllerState required) throws Throwable
   {
      Controller controller = getKernel().getController();
      controller.change(context, required);
      return context.getState();
   }

   protected void checkComplete() throws Exception
   {
      getDeployerClient().checkComplete();
   }

   protected DeploymentUnit getDeploymentUnit(Deployment deployment) throws Exception
   {
      DeployerClient deployerClient = getDeployerClient();
      MainDeployerStructure deployerStructure = (MainDeployerStructure)deployerClient;
      return deployerStructure.getDeploymentUnit(deployment.getName());
   }

   protected DeployerClient getDeployerClient()
   {
      return getBundleManager().getDeployerClient();
   }

   protected Deployment createDeployment(String name, String[] resources, Class<?>... packages) throws Exception
   {
      Archive<?> assembly = assembleArchive(name, resources, packages);
      Deployment deployment = AbstractDeployment.createDeployment(toVirtualFile(assembly));
      return deployment;
   }

   protected Deployment addBeanMetaData(Deployment deployment, Class<?> beanClass)
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanClass.getSimpleName(), beanClass.getName());
      return addBeanMetaData(deployment, builder.getBeanMetaData());
   }

   protected Deployment addBeanMetaData(Deployment deployment, BeanMetaData bmd)
   {
      MutableAttachments att = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      KernelDeployment kdep = att.getAttachment(KernelDeployment.class);
      if (kdep == null)
      {
         kdep = new AbstractKernelDeployment();
         ((AbstractKernelDeployment)kdep).setBeanFactories(new ArrayList<BeanMetaDataFactory>());
         att.addAttachment(KernelDeployment.class, kdep);
      }
      kdep.getBeanFactories().add((BeanMetaDataFactory)bmd);
      return deployment;
   }

   protected Deployment addOSGiMetaData(Deployment deployment, OSGiMetaData metadata)
   {
      MutableAttachments att = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      att.addAttachment(OSGiMetaData.class, metadata);
      return deployment;
   }

   protected Deployment deploy(Deployment deployment) throws Exception
   {
      getDeployerClient().addDeployment(deployment);
      getDeployerClient().process();
      getDeployerClient().checkComplete();
      return deployment;
   }

   protected void undeploy(Deployment deployment) throws Exception
   {
      DeployerClient deployerClient = getDeployerClient();
      deployerClient.undeploy(deployment);
   }

   protected Deployment addDeployment(Deployment deployment) throws DeploymentException
   {
      getDeployerClient().addDeployment(deployment);
      getDeployerClient().process();
      return deployment;
   }

   protected ClassLoaderSystem getClassLoaderSystem()
   {
      return (ClassLoaderSystem)getBean("OSGiClassLoaderSystem");
   }

   protected ClassLoaderDomain getClassLoaderDomain()
   {
      return (ClassLoaderDomain)getBean("OSGiClassLoaderDomain");
   }
}