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
package org.jboss.osgi.framework.resolver;

/**
 * An OSGi resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XResolver 
{
   /**
    * Add a module to the resolver.
    */
   void addModule(XModule module);

   /**
    * Remove a module from the resolver.
    */
   void removeModule(XModule module);

   /**
    * Find the host module for a given fragment module.
    */
   XModule findHost(XModule fragModule);
   
   /**
    * Resolve the given root module
    * @throws XResolverException if the module cannot be resolved
    */
   void resolve(XModule rootModule) throws XResolverException;

   /**
    * The the optional callback handler on the resolver
    */
   void setCallbackHandler(XResolverCallback callback);
}