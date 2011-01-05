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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory
{
   @Override
   public URLStreamHandler createURLStreamHandler(String protocol)
   {
      System.out.println("******* Request for: " + protocol);
      if ("protocol1".equals(protocol))
      {
         return new Protocol1Handler();
      }
      return null;
   }

   private static final class Protocol1Handler extends URLStreamHandler
   {

      @Override
      protected URLConnection openConnection(URL u) throws IOException
      {
         return new Protocol1URLConnection(u);
      }

   }

   private static final class Protocol1URLConnection extends URLConnection
   {

      public Protocol1URLConnection(URL u)
      {
         super(u);
      }

      @Override
      public void connect() throws IOException
      {
      }

      @Override
      public InputStream getInputStream() throws IOException
      {
         return new ByteArrayInputStream("XYZ".getBytes());
      }
   }
}
