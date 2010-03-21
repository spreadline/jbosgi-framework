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
package org.jboss.test.osgi.service;

import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.test.osgi.AbstractFrameworkTest;
import org.jboss.test.osgi.service.support.a.A;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * ServiceReferenceTest.
 *
 * todo more isAssignableTests
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 1.1 $
 */
public class ServiceReferenceTestCase extends AbstractFrameworkTest
{
   @Test
   public void testGetProperty() throws Exception
   {
      ServiceReference reference = null;
      String[] clazzes = new String[] { BundleContext.class.getName() };
      Object serviceID = null;

      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testA", "a");
         properties.put("testB", "b");
         properties.put("MiXeD", "Case");
         ServiceRegistration registration = bundleContext.registerService(clazzes, bundleContext, properties);
         assertNotNull(registration);

         reference = registration.getReference();
         assertNotNull(reference);

         serviceID = reference.getProperty(Constants.SERVICE_ID);
         assertNotNull(serviceID);
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID.toLowerCase()));
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID.toUpperCase()));
         assertArrayEquals(clazzes, (String[])reference.getProperty(Constants.OBJECTCLASS));
         assertArrayEquals(clazzes, (String[])reference.getProperty(Constants.OBJECTCLASS.toLowerCase()));
         assertArrayEquals(clazzes, (String[])reference.getProperty(Constants.OBJECTCLASS.toUpperCase()));
         assertEquals("a", reference.getProperty("testA"));
         assertEquals("b", reference.getProperty("testB"));
         assertEquals("Case", reference.getProperty("MiXeD"));
         assertEquals("Case", reference.getProperty("mixed"));
         assertEquals("Case", reference.getProperty("MIXED"));
         assertNull(reference.getProperty(null));
         assertNull(reference.getProperty("doesNotExist"));

         properties.put("testA", "notA");
         assertEquals("a", reference.getProperty("testA"));
         properties.put(Constants.SERVICE_ID, "rubbish");
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
         properties.put(Constants.OBJECTCLASS, "rubbish");
         assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));

         registration.setProperties(properties);
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", reference.getProperty("testA"));
         assertEquals("b", reference.getProperty("testB"));
         assertEquals("Case", reference.getProperty("MiXeD"));
         assertEquals("Case", reference.getProperty("mixed"));
         assertEquals("Case", reference.getProperty("MIXED"));

         registration.setProperties(null);
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));
         assertNull(reference.getProperty("testA"));
         assertNull(reference.getProperty("testB"));
         assertNull(reference.getProperty("MiXeD"));
         assertNull(reference.getProperty("mixed"));
         assertNull(reference.getProperty("MIXED"));
         assertNull(reference.getProperty(null));

         registration.setProperties(properties);
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", reference.getProperty("testA"));
         assertEquals("b", reference.getProperty("testB"));
         assertEquals("Case", reference.getProperty("MiXeD"));
         assertEquals("Case", reference.getProperty("mixed"));
         assertEquals("Case", reference.getProperty("MIXED"));
         assertNull(reference.getProperty(null));

         registration.unregister();
         assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", reference.getProperty("testA"));
         assertEquals("b", reference.getProperty("testB"));
         assertEquals("Case", reference.getProperty("MiXeD"));
         assertEquals("Case", reference.getProperty("mixed"));
         assertEquals("Case", reference.getProperty("MIXED"));
         assertNull(reference.getProperty(null));
      }
      finally
      {
         bundle.uninstall();
      }

      assertEquals(serviceID, reference.getProperty(Constants.SERVICE_ID));
      assertEquals(clazzes, reference.getProperty(Constants.OBJECTCLASS));
      assertEquals("notA", reference.getProperty("testA"));
      assertEquals("b", reference.getProperty("testB"));
      assertEquals("Case", reference.getProperty("MiXeD"));
      assertEquals("Case", reference.getProperty("mixed"));
      assertEquals("Case", reference.getProperty("MIXED"));
      assertNull(reference.getProperty(null));
   }

   @Test
   public void testGetPropertyKeys() throws Exception
   {
      ServiceReference reference = null;

      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testA", "a");
         properties.put("testB", "b");
         properties.put("MiXeD", "Case");
         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, properties);
         assertNotNull(registration);

         reference = registration.getReference();
         assertNotNull(reference);

         assertPropertyKeys(reference, "testA", "testB", "MiXeD");

         properties.put("testC", "c");
         assertPropertyKeys(reference, "testA", "testB", "MiXeD");

         registration.setProperties(properties);
         assertPropertyKeys(reference, "testA", "testB", "testC", "MiXeD");

         registration.setProperties(null);
         assertPropertyKeys(reference);

         registration.setProperties(properties);
         assertPropertyKeys(reference, "testA", "testB", "testC", "MiXeD");

         registration.unregister();
         assertPropertyKeys(reference, "testA", "testB", "testC", "MiXeD");
      }
      finally
      {
         bundle.uninstall();
      }
      assertPropertyKeys(reference, "testA", "testB", "testC", "MiXeD");
   }

   private void assertPropertyKeys(ServiceReference reference, String... expectedKeys)
   {
      Set<String> expected = new HashSet<String>();
      expected.add(Constants.SERVICE_ID);
      expected.add(Constants.OBJECTCLASS);
      for (String key : expectedKeys)
         expected.add(key);

      Set<String> actual = new HashSet<String>();
      for (String key : reference.getPropertyKeys())
         actual.add(key);

      assertEquals(expected, actual);
   }

   @Test
   public void testGetBundle() throws Exception
   {
      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         Bundle other = reference.getBundle();
         assertEquals(bundle, other);

         registration.unregister();

         other = reference.getBundle();
         assertNull("" + other, other);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testGetBundleAfterStop() throws Exception
   {
      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         Bundle other = reference.getBundle();
         assertEquals(bundle, other);

         bundle.stop();

         other = reference.getBundle();
         assertNull("" + other, other);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testUsingBundles() throws Exception
   {
      VirtualFile assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         assertUsingBundles(reference);

         VirtualFile assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2");
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            bundle2.start();
            BundleContext bundleContext2 = bundle2.getBundleContext();
            assertNotNull(bundleContext2);

            bundleContext2.getService(reference);
            assertUsingBundles(reference, bundle2);

            bundleContext2.ungetService(reference);
            assertUsingBundles(reference);

            bundleContext2.getService(reference);
            bundleContext2.getService(reference);
            assertUsingBundles(reference, bundle2);
            bundleContext2.ungetService(reference);
            assertUsingBundles(reference, bundle2);
            bundleContext2.ungetService(reference);
            assertUsingBundles(reference);

            bundleContext.getService(reference);
            bundleContext2.getService(reference);
            assertUsingBundles(reference, bundle1, bundle2);

            registration.unregister();
            assertUsingBundles(reference);
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testUsingBundlesAfterStop() throws Exception
   {
      VirtualFile assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         assertUsingBundles(reference);

         VirtualFile assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2");
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            bundle2.start();
            BundleContext bundleContext2 = bundle2.getBundleContext();
            assertNotNull(bundleContext2);

            bundleContext.getService(reference);
            bundleContext2.getService(reference);
            assertUsingBundles(reference, bundle1, bundle2);

            bundle1.stop();
            assertUsingBundles(reference);
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testIsAssignableToErrors() throws Exception
   {
      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1", A.class);
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         try
         {
            reference.isAssignableTo(null, A.class.getName());
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }

         try
         {
            reference.isAssignableTo(bundle, null);
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testNotAssignableTo() throws Exception
   {
      VirtualFile assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1", A.class);
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration);

         ServiceReference reference = registration.getReference();
         assertNotNull(reference);

         VirtualFile assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2", A.class);
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            assertFalse(reference.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(reference.isAssignableTo(bundle2, String.class.getName()));

            registration.unregister();
            assertFalse(reference.isAssignableTo(bundle2, A.class.getName()));
            assertFalse(reference.isAssignableTo(bundle2, String.class.getName())); // review ???
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testIsAssignableTo() throws Exception
   {
      //Bundle-Name: Service2
      //Bundle-SymbolicName: org.jboss.test.osgi.service2
      //Export-Package: org.jboss.test.osgi.service.support.a
      VirtualFile assembly2 = assembleArchive("service2", "/bundles/service/service-bundle2", A.class);
      Bundle bundle2 = installBundle(assembly2);

      try
      {
         bundle2.start();
         BundleContext bundleContext2 = bundle2.getBundleContext();
         assertNotNull(bundleContext2);

         //Bundle-Name: Service1
         //Bundle-SymbolicName: org.jboss.test.osgi.service1
         //Import-Package: org.jboss.test.osgi.service.support.a
         VirtualFile assembly1 = assembleArchive("service1", "/bundles/service/service-bundle1");
         Bundle bundle1 = installBundle(assembly1);

         try
         {

            ServiceRegistration registration = bundleContext2.registerService(BundleContext.class.getName(), bundleContext2, null);
            assertNotNull(registration);

            ServiceReference reference = registration.getReference();
            assertNotNull(reference);

            assertTrue(reference.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(reference.isAssignableTo(bundle2, String.class.getName()));
            assertTrue(reference.isAssignableTo(bundle1, A.class.getName()));
            assertTrue(reference.isAssignableTo(bundle1, String.class.getName()));

            registration.unregister();
            assertTrue(reference.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(reference.isAssignableTo(bundle2, String.class.getName()));
            assertFalse(reference.isAssignableTo(bundle1, A.class.getName()));
            assertFalse(reference.isAssignableTo(bundle1, String.class.getName())); // review ???
         }
         finally
         {
            bundle1.uninstall();
         }
      }
      finally
      {
         bundle2.uninstall();
      }
   }

   @Test
   public void testCompareTo() throws Exception
   {
      VirtualFile assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration registration1 = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration1);

         ServiceReference reference1 = registration1.getReference();
         assertNotNull(reference1);

         ServiceRegistration registration2 = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(registration2);

         ServiceReference reference2 = registration2.getReference();
         assertNotNull(reference2);

         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put(Constants.SERVICE_RANKING, 10);
         ServiceRegistration registration3 = bundleContext.registerService(BundleContext.class.getName(), bundleContext, properties);
         assertNotNull(registration3);

         ServiceReference reference3 = registration3.getReference();
         assertNotNull(reference3);

         properties = new Hashtable<String, Object>();
         properties.put(Constants.SERVICE_RANKING, -10);
         ServiceRegistration registration4 = bundleContext.registerService(BundleContext.class.getName(), bundleContext, properties);
         assertNotNull(registration4);

         ServiceReference reference4 = registration4.getReference();
         assertNotNull(reference4);

         assertGreaterRanking(reference1, reference2);
         assertGreaterRanking(reference3, reference1);
         assertGreaterRanking(reference3, reference2);
         assertGreaterRanking(reference1, reference4);
         assertGreaterRanking(reference2, reference4);
         assertGreaterRanking(reference3, reference4);

         try
         {
            reference1.compareTo(null);
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }

         try
         {
            reference1.compareTo(new Object());
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }

         properties = new Hashtable<String, Object>();
         properties.put(Constants.SERVICE_RANKING, "NotANumber");
         ServiceRegistration registration5 = bundleContext.registerService(BundleContext.class.getName(), bundleContext, properties);
         assertNotNull(registration5);

         ServiceReference reference5 = registration5.getReference();
         assertNotNull(reference5);

         assertGreaterRanking(reference1, reference5); // review ???

         Set<ServiceReference> ordering = new TreeSet<ServiceReference>();
         ordering.add(reference1);
         ordering.add(reference2);
         ordering.add(reference3);
         ordering.add(reference4);
         ordering.add(reference5);
         Iterator<ServiceReference> iterator = ordering.iterator();
         assertEquals(reference4, iterator.next());
         assertEquals(reference5, iterator.next());
         assertEquals(reference2, iterator.next());
         assertEquals(reference1, iterator.next());
         assertEquals(reference3, iterator.next());

         ordering = new TreeSet<ServiceReference>();
         ordering.add(reference5);
         ordering.add(reference4);
         ordering.add(reference3);
         ordering.add(reference2);
         ordering.add(reference1);
         iterator = ordering.iterator();
         assertEquals(reference4, iterator.next());
         assertEquals(reference5, iterator.next());
         assertEquals(reference2, iterator.next());
         assertEquals(reference1, iterator.next());
         assertEquals(reference3, iterator.next());
      }
      finally
      {
         bundle.uninstall();
      }
   }

   protected void assertGreaterRanking(ServiceReference reference1, ServiceReference reference2) throws Exception
   {
      assertTrue(reference1 + " > " + reference2, reference1.compareTo(reference2) > 0);
      assertTrue(reference2 + " < " + reference1, reference2.compareTo(reference1) < 0);
   }
}