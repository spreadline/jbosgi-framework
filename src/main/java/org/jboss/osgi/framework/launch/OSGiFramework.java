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
package org.jboss.osgi.framework.launch;

// $Id$

import java.io.IOException;
import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jboss.osgi.framework.bundle.OSGiBundleManager;
import org.jboss.osgi.framework.bundle.OSGiBundleWrapper;
import org.jboss.virtual.VirtualFile;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;

/**
 * An impementation of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 21-Aug-2009
 */
public class OSGiFramework extends OSGiBundleWrapper implements Framework
{
   // Provide logging
   final Logger log = Logger.getLogger(OSGiFramework.class);
   
   private OSGiBundleManager bundleManager;
   
   public OSGiFramework(OSGiBundleManager bundleManager)
   {
      super(bundleManager.getBundleById(0));
      this.bundleManager = bundleManager;
   }

   public OSGiBundleManager getBundleManager()
   {
      return bundleManager;
   }

   public void init() throws BundleException
   {
      bundleManager.initFramework();
   }

   @Override
   public void start() throws BundleException
   {
      bundleManager.startFramework();
   }

   @Override
   public void start(int options) throws BundleException
   {
      bundleManager.startFramework();
   }

   @Override
   public void stop() throws BundleException
   {
      bundleManager.stopFramework();
   }

   @Override
   public void stop(int options) throws BundleException
   {
      bundleManager.stopFramework();
   }

   @Override
   public void update() throws BundleException
   {
      bundleManager.restartFramework();
   }

   /**
    * Calling this method is the same as calling {@link #update()} except that any provided InputStream is immediately closed.
    */
   @Override
   public void update(InputStream in) throws BundleException
   {
      if (in != null)
      {
         try
         {
            in.close();
         }
         catch (IOException ex)
         {
            // ignore
         }
      }

      // [TODO] The method returns immediately to the caller after initiating the following steps

      bundleManager.restartFramework();
   }

   /**
    * Install a bundle from a virtual file
    * 
    * @param root the root file
    * @return the bundle
    * @throws BundleException for any error
    */
   public Bundle installBundle(VirtualFile root) throws BundleException
   {
      return bundleManager.installBundle(root).getBundleInternal();
   }
   
   /**
    * The Framework cannot be uninstalled.
    * <p>
    * This method always throws a BundleException.
    */
   @Override
   public void uninstall() throws BundleException
   {
      throw new BundleException("The system bundle cannot be uninstalled");
   }

   /**
    * Wait until this Framework has completely stopped. 
    */
   public FrameworkEvent waitForStop(long timeout) throws InterruptedException
   {
      return bundleManager.waitForStop(timeout);
   }
}