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

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.Test;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class URLHandlerTestCase extends OSGiFrameworkTest
{
   @Test
   public void testURLHandling() throws Exception
   {
      URL url = new URL("protocol1://blahdiblah");
      System.out.println("Result: " + new String(Streams.suck(url.openStream())));
   }

   @Test
   public void testURLHandling2() throws Exception
   {
      try {
         new URL("protocol2://blahdiblah");
         fail("The protocol is not registered so a URL containing this protocol should throw a MalformedURLException.");
      } catch (MalformedURLException mue) {
         // good
      }
   }
}