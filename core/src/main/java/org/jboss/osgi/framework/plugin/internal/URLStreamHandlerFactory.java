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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory
{
   private static BundleContext systemBundleContext;

   final Logger log = Logger.getLogger(URLStreamHandlerFactory.class);
   private final ServiceTracker tracker;
   private Map<String, URLStreamHandlerService> handlers = new ConcurrentHashMap<String, URLStreamHandlerService>();

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
                  handlers.put(protocol, (URLStreamHandlerService)svc);
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

            for (Iterator<URLStreamHandlerService> it = handlers.values().iterator(); it.hasNext();)
            {
               URLStreamHandlerService svc = it.next();
               if (service.equals(svc))
               {
                  it.remove();
                  break;
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
      final URLStreamHandlerService handlerService = handlers.get(protocol);
      if (handlerService == null) 
         return null;
      
      return new URLStreamHandler()
      {
         @Override
         protected URLConnection openConnection(URL u) throws IOException
         {
            return handlerService.openConnection(u);
         }
      };
   }
}
