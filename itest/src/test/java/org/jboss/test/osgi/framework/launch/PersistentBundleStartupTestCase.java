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
package org.jboss.test.osgi.framework.launch;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.osgi.spi.util.ServiceLoader;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.framework.simple.bundleC.SimpleActivator;
import org.jboss.test.osgi.framework.simple.bundleC.SimpleService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Test persistent bundle startup
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Oct-2010
 */
public class PersistentBundleStartupTestCase extends OSGiFrameworkTest
{
   File storageDir = new File("target/test-osgi-store").getAbsoluteFile();
   
   @BeforeClass
   public static void beforeClass()
   {
      // prevent framework creation
   }
   
   @Test
   public void testInstalledBundleOnFirstInit() throws Exception
   {
      Map<String,String> props = new HashMap<String, String>();
      props.put("org.osgi.framework.storage", storageDir.getAbsolutePath());
      props.put("org.osgi.framework.storage.clean", "onFirstInit");
      
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      Framework framework = factory.newFramework(props);
      
      framework.start();
      assertBundleState(Bundle.ACTIVE, framework.getState());
      
      File systemStorageDir = new File(storageDir + "/bundle-0");
      assertTrue("File exists: " + systemStorageDir, systemStorageDir.exists());
      
      JavaArchive archive = getBundleArchive();
      BundleContext context = framework.getBundleContext();
      Bundle bundle = context.installBundle(archive.getName(), toInputStream(archive));
      assertBundleState(Bundle.INSTALLED, bundle.getState());
      
      File bundleStorageDir = new File(storageDir + "/bundle-1");
      assertTrue("File exists: " + bundleStorageDir, bundleStorageDir.exists());
      
      framework.stop();
      framework.waitForStop(2000);
      assertBundleState(Bundle.RESOLVED, framework.getState());
      assertBundleState(Bundle.INSTALLED, bundle.getState());
      
      // Check that the storage dirs are still there 
      assertTrue("File exists: " + systemStorageDir, systemStorageDir.exists());
      assertTrue("File exists: " + bundleStorageDir, bundleStorageDir.exists());
      
      // Restart the Framework
      framework.start();
      assertBundleState(Bundle.ACTIVE, framework.getState());
      
      // Check that the storage dirs are still there 
      assertTrue("File exists: " + systemStorageDir, systemStorageDir.exists());
      assertTrue("File exists: " + bundleStorageDir, bundleStorageDir.exists());
      
      context = framework.getBundleContext();
      bundle = context.getBundle(bundle.getBundleId());
      assertNotNull("Bundle available", bundle);

      framework.stop();
      framework.waitForStop(2000);
      assertBundleState(Bundle.RESOLVED, framework.getState());
   }

   @Test
   public void testActiveBundle() throws Exception
   {
      Map<String,String> props = new HashMap<String, String>();
      props.put("org.osgi.framework.storage", storageDir.getAbsolutePath());
      props.put("org.osgi.framework.storage.clean", "onFirstInit");
      
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      Framework framework = factory.newFramework(props);
      
      framework.start();
      assertBundleState(Bundle.ACTIVE, framework.getState());
      
      JavaArchive archive = getBundleArchive();
      BundleContext context = framework.getBundleContext();
      Bundle bundle = context.installBundle(archive.getName(), toInputStream(archive));
      assertBundleState(Bundle.INSTALLED, bundle.getState());
      
      bundle.start();
      assertBundleState(Bundle.ACTIVE, framework.getState());
      
      framework.stop();
      framework.waitForStop(2000);
      assertBundleState(Bundle.RESOLVED, framework.getState());
      assertBundleState(Bundle.RESOLVED, bundle.getState());
      
      // Restart the Framework
      framework.start();
      assertBundleState(Bundle.ACTIVE, framework.getState());
      
      context = framework.getBundleContext();
      bundle = context.getBundle(bundle.getBundleId());
      assertBundleState(Bundle.ACTIVE, bundle.getState());

      framework.stop();
      framework.waitForStop(2000);
      assertBundleState(Bundle.RESOLVED, framework.getState());
   }

   private JavaArchive getBundleArchive()
   {
      // Bundle-Version: 1.0.0
      // Bundle-SymbolicName: simple-bundle
      // Bundle-Activator: org.jboss.osgi.msc.framework.simple.bundle.SimpleActivator
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundle");
      archive.addClasses(SimpleService.class, SimpleActivator.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion("1.0.0");
            builder.addBundleActivator(SimpleActivator.class);
            builder.addImportPackages(BundleActivator.class);
            return builder.openStream();
         }
      });
      return archive;
   }
}