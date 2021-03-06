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
package org.jboss.test.osgi.framework.simple;

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.framework.simple.bundleB.SimpleLogServiceActivator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A test that deployes a bundle and verifies its state
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Aug-2009
 */
public class SimpleLogServiceTestCase extends OSGiFrameworkTest
{
   static JavaArchive archive;

   @BeforeClass
   public static void beforeClass()
   {
      // Bundle-SymbolicName: simple-logservice-bundle
      // Bundle-Activator:
      // org.jboss.test.osgi.framework.simple.bundleB.SimpleLogServiceActivator
      archive = ShrinkWrap.create(JavaArchive.class, "simple-logservice-bundle");
      archive.addClasses(SimpleLogServiceActivator.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleActivator(SimpleLogServiceActivator.class);
            builder.addImportPackages(BundleActivator.class, LogService.class, ServiceTracker.class);
            return builder.openStream();
         }
      });
   }

   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      createFramework().start();
   }

   @After
   public void tearDown() throws Exception
   {
      shutdownFramework();
      super.tearDown();
   }

   @Test
   public void testNoLogService() throws Exception
   {
      Bundle bundle = installBundle(archive);
      try
      {
         bundle.start();
         fail("Unresolved package contstraint on [org.osgi.service.log] expected");
      }
      catch (BundleException ex)
      {
         // expected
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testLogServiceFromThirdParty() throws Exception
   {
      Bundle logBundle = installBundle(getTestArchivePath("bundles/org.apache.felix.log.jar"));
      try
      {
         logBundle.start();
         assertBundleState(Bundle.ACTIVE, logBundle.getState());

         Bundle bundle = installBundle(archive);
         try
         {
            bundle.start();
            assertBundleState(Bundle.ACTIVE, bundle.getState());
         }
         finally
         {
            bundle.uninstall();
         }
      }
      finally
      {
         logBundle.uninstall();
      }
   }
}