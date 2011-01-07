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
package org.jboss.osgi.framework.plugin.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;
import org.jboss.osgi.framework.bundle.ServiceReferenceComparator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory
{
   private static BundleContext systemBundleContext;

   final Logger log = Logger.getLogger(URLStreamHandlerFactory.class);
   private final ServiceTracker tracker;
   private ConcurrentMap<String, List<ServiceReference>> handlers = new ConcurrentHashMap<String, List<ServiceReference>>();

   static void setSystemBundleContext(BundleContext bc)
   {
      systemBundleContext = bc;
   }

   public URLStreamHandlerFactory()
   {
      if (systemBundleContext == null)
         throw new IllegalStateException("System Context not initialized");

      tracker = new ServiceTracker(systemBundleContext, URLStreamHandlerService.class.getName(), null)
      {

         @Override
         public Object addingService(ServiceReference reference)
         {
            Object svc = super.addingService(reference);
            String[] protocols = parseProtocol(reference.getProperty(URLConstants.URL_HANDLER_PROTOCOL));
            if (protocols != null && svc instanceof URLStreamHandlerService)
            {
               for (String protocol : protocols)
               {
                  handlers.putIfAbsent(protocol, new ArrayList<ServiceReference>());
                  List<ServiceReference> list = handlers.get(protocol);
                  synchronized (list)
                  {
                     list.add(reference);
                     Collections.sort(list, Collections.reverseOrder(ServiceReferenceComparator.getInstance()));
                  }
               }
            }
            else
            {
               log.error("A non-compliant instance of " + URLStreamHandlerService.class.getName()
                     + " has been registered for protocol: " + Arrays.toString(protocols) + " - " + svc);
            }
            return svc;
         }

         @Override
         public void modifiedService(ServiceReference reference, Object service)
         {
            // TODO Auto-generated method stub
            // David: vaguely remember that the spec says something about ignoring this case need to check
            super.modifiedService(reference, service);
         }

         @Override
         public void removedService(ServiceReference reference, Object service)
         {
            super.removedService(reference, service);

            for (List<ServiceReference> list : handlers.values())
            {
               synchronized (list)
               {
                  for (Iterator<ServiceReference> it = list.iterator(); it.hasNext();)
                  {
                     if (it.next().equals(reference))
                     {
                        it.remove();
                        break;
                     }
                  }
               }
            }
         }
      };
      tracker.open();
   }

   @Override
   protected void finalize() throws Throwable
   {
      tracker.close();
   }

   protected String[] parseProtocol(Object prop)
   {
      if (prop == null)
         return null;

      if (prop instanceof String)
         return new String[] { (String)prop };

      if (prop instanceof String[])
         return (String[])prop;

      return null;
   }

   @Override
   public URLStreamHandler createURLStreamHandler(String protocol)
   {
      List<ServiceReference> refList = handlers.get(protocol);
      if (refList == null)
         return null;
      
      return new URLStreamHandlerProxy(protocol, refList);
   }

   private static final class URLStreamHandlerProxy extends URLStreamHandler implements URLStreamHandlerSetter
   {
      // This list is maintained in the ServiceTracker that tracks the URLStreamHandlerService
      // This proxy should always use to top element (if it contains any elements).
      private final List<ServiceReference> serviceReferences;
      private final String protocol;

      public URLStreamHandlerProxy(String protocol, List<ServiceReference> refList)
      {
         this.protocol = protocol;
         this.serviceReferences = refList;
      }
      
      @Override
      public void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref)
      {
         // Made public to implement URLStreamHandlerSetter
         super.setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
      }

      @Override
      @SuppressWarnings("deprecation")
      public void setURL(URL u, String protocol, String host, int port, String file, String ref)
      {
         // Made public to implement URLStreamHandlerSetter
         super.setURL(u, protocol, host, port, file, ref);
      }

      @Override
      protected void parseURL(URL u, String spec, int start, int limit)
      {
         getHandlerService().parseURL(this, u, spec, start, limit);
      }

      @Override
      protected URLConnection openConnection(URL u) throws IOException
      {
         return getHandlerService().openConnection(u);
      }

      @Override
      protected String toExternalForm(URL u)
      {
         return getHandlerService().toExternalForm(u);
      }

      @Override
      protected URLConnection openConnection(URL u, Proxy p) throws IOException
      {
         // TODO via reflection: 
         // return getHandlerService().openConnection(u, p);
         return null;
      }

      @Override
      protected int getDefaultPort()
      {
         return getHandlerService().getDefaultPort();
      }

      @Override
      protected boolean equals(URL u1, URL u2)
      {
         return getHandlerService().equals(u1, u2);
      }

      @Override
      protected int hashCode(URL u)
      {
         return getHandlerService().hashCode(u);
      }

      @Override
      protected boolean sameFile(URL u1, URL u2)
      {
         return getHandlerService().sameFile(u1, u2);
      }

      @Override
      protected synchronized InetAddress getHostAddress(URL u)
      {
         return getHandlerService().getHostAddress(u);
      }

      @Override
      protected boolean hostsEqual(URL u1, URL u2)
      {
         return getHandlerService().hostsEqual(u1, u2);
      }

      private URLStreamHandlerService getHandlerService()
      {
         synchronized (serviceReferences)
         {
            if (serviceReferences.isEmpty())
               throw new IllegalStateException("No handlers in the OSGi Service registry for protocol: " + protocol);

            ServiceReference ref = serviceReferences.get(0);
            Object service = ref.getBundle().getBundleContext().getService(ref);
            if (service instanceof URLStreamHandlerService)
            {
               return (URLStreamHandlerService)service;
            }
            throw new IllegalStateException("Problem with OSGi URL handler service " + service + " for url:" + protocol);
         }
      }
   }
}
