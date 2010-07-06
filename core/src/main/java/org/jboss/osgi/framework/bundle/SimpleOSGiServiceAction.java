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
package org.jboss.osgi.framework.bundle;

/**
 * Handle OSGi service context.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class SimpleOSGiServiceAction extends OSGiServiceAction
{
   /**
    * Handle osgi context.
    *
    * @param context the context
    * @param install are we in install phase
    * @throws Throwable for any error
    */
   protected abstract void handleContext(OSGiServiceState context, boolean install) throws Throwable;

   protected void installAction(OSGiServiceState context) throws Throwable
   {
      try
      {
         handleContext(context, true);
      }
      catch (Throwable t)
      {
         uninstall(context);
         throw t;
      }
   }

   protected void uninstallAction(OSGiServiceState context)
   {
      try
      {
         handleContext(context, false);
      }
      catch (Throwable t)
      {
         log.warn("Ignoring exception at un-installing context: " + t);
      }
   }
}