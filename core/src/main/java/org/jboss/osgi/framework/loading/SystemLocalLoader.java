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
package org.jboss.osgi.framework.loading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.Resource;
import org.jboss.osgi.vfs.VFSUtils;

/**
 * A {@link LocalLoader} that delegates to the system class loader.
 *
 * @author thomas.diesler@jboss.com
 * @since 15-Sep-2010
 */
public class SystemLocalLoader implements LocalLoader
{
   // Provide logging
   private static final Logger log = Logger.getLogger(SystemLocalLoader.class);

   private final ClassLoader systemClassLoader;
   private final Set<String> exportedPaths;

   public SystemLocalLoader(Set<String> exportedPaths)
   {
      if (exportedPaths == null)
         throw new IllegalArgumentException("Null loaderPaths");

      this.exportedPaths = exportedPaths;
      this.systemClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return ClassLoader.getSystemClassLoader();
         }
      });
   }

   public Set<String> getExportedPaths()
   {
      return Collections.unmodifiableSet(exportedPaths);
   }

   @Override
   public Class<?> loadClassLocal(String className, boolean exportOnly)
   {
      log.tracef("Attempt to find system class [%s] ...", className);

      String path = VFSUtils.getPathFromClassName(className);
      if (exportedPaths.contains(path))
      {
         Class<?> result = null;
         try
         {
            result = loadSystemClass(className);
            log.tracef("Found system class [%s]", className);
            return result;
         }
         catch (ClassNotFoundException ex)
         {
            log.tracef("Cannot find system class [%s]", className);
         }
      }
      else
      {
         log.tracef("Cannot find filtered class [%s]", className);
      }

      return null;
   }

   public Class<?> loadSystemClass(String className) throws ClassNotFoundException
   {
      return systemClassLoader.loadClass(className);
   }

   @Override
   public List<Resource> loadResourceLocal(String name)
   {
      // TODO, what do we do with this one?
      // We need to provide the META-INF/service/java.net.URLStreamHandlerFactory
      // resource to hook it in with the ModularURLStreamHandlerFactory
      // but we cannot simply serve every single META-INF/service resource
      // on the classpath because that's may too much! It will actually cause
      // Arquillian to fail because it finds two test runners in that case...
      if (!"META-INF/services/java.net.URLStreamHandlerFactory".equals(name))
         return Collections.emptyList();

      Enumeration<URL> urls;
      try
      {
         urls = systemClassLoader.getResources(name);
      }
      catch (IOException e)
      {
         return Collections.emptyList();
      }

      List<Resource> list = new ArrayList<Resource>();
      while (urls.hasMoreElements())
         list.add(new URLResource(urls.nextElement()));

      return list;
   }

   @Override
   public Resource loadResourceLocal(String root, String name)
   {
      // TODO see loadResourceLocal(String name)
      if (!"META-INF/services/java.net.URLStreamHandlerFactory".equals(name))
         return null;

      if (!"".equals(root))
         return null;

      final URL url = systemClassLoader.getResource(name);
      return url == null ? null : new URLResource(url);
   }

   // TODO this class exists in org.jboss.modules but is package-protected
   // Should it not be made public?
   private static final class URLResource implements Resource
   {
      private final URL url;

      public URLResource(final URL url)
      {
         this.url = url;
      }

      public String getName()
      {
         return url.getPath();
      }

      public URL getURL()
      {
         return url;
      }

      public InputStream openStream() throws IOException
      {
         return url.openStream();
      }

      public long getSize()
      {
         return 0L;
      }
   }
}
