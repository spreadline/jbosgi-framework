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
package org.jboss.test.osgi.framework.urlhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.Test;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLHandlerTestCase extends OSGiFrameworkTest
{
   @Test
   public void testURLHandling() throws Exception
   {
      URLStreamHandlerService protocol1Svc = new TestURLStreamHandlerService("test_protocol1");
      Dictionary<String, Object> props1 = new Hashtable<String, Object>();
      props1.put(URLConstants.URL_HANDLER_PROTOCOL, "protocol1");
      getSystemContext().registerService(URLStreamHandlerService.class.getName(), protocol1Svc, props1);
      
      URLStreamHandlerService protocol2Svc = new TestURLStreamHandlerService("test_protocol2");
      Dictionary<String, Object> props2 = new Hashtable<String, Object>();
      props2.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { "protocol2", "altprot2" });
      getSystemContext().registerService(URLStreamHandlerService.class.getName(), protocol2Svc, props2);

      URL url = new URL("protocol1://blah");
      assertEquals("test_protocol1blah", new String(suckStream(url.openStream())));

      URL url2 = new URL("protocol2://foo");
      assertEquals("test_protocol2foo", new String(suckStream(url2.openStream())));

      URL url3 = new URL("altprot2://bar");
      assertEquals("test_protocol2bar", new String(suckStream(url3.openStream())));

      try
      {
         new URL("protocol3://blahdiblah");
         fail("protocol3 is not registered so a URL containing this protocol should throw a MalformedURLException.");
      }
      catch (MalformedURLException mue)
      {
         // good
      }
   }


   public static void pumpStream(InputStream is, OutputStream os) throws IOException
   {
      byte[] bytes = new byte[8192];

      int length = 0;
      int offset = 0;

      while ((length = is.read(bytes, offset, bytes.length - offset)) != -1)
      {
         offset += length;

         if (offset == bytes.length)
         {
            os.write(bytes, 0, bytes.length);
            offset = 0;
         }
      }
      if (offset != 0)
      {
         os.write(bytes, 0, offset);
      }
   }

   public static byte[] suckStream(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         pumpStream(is, baos);
         return baos.toByteArray();
      }
      finally
      {
         is.close();
      }
   }

   private static class TestURLStreamHandlerService extends AbstractURLStreamHandlerService
   {
      private final String data;

      public TestURLStreamHandlerService(String data)
      {
         this.data = data;
      }

      @Override
      public URLConnection openConnection(final URL u) throws IOException
      {
         return new URLConnection(u)
         {
            @Override
            public void connect() throws IOException
            {
            }

            @Override
            public InputStream getInputStream() throws IOException
            {
               String content = data + u.getHost();
               return new ByteArrayInputStream(content.getBytes());
            }
         };
      }
   }
}
