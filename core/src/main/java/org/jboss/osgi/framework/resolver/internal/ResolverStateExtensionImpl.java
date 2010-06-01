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
package org.jboss.osgi.framework.resolver.internal;

import java.util.Set;

import org.apache.felix.framework.FelixResolverState;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.capabilityset.Capability;
import org.apache.felix.framework.capabilityset.Requirement;
import org.apache.felix.framework.resolver.Module;
import org.apache.felix.framework.resolver.ResolveException;

/**
 * An extension to the Apache Felix ResolverState.
 *  
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
class ResolverStateExtensionImpl implements ResolverStateExtension
{
   private FelixResolverState delegate;

   public ResolverStateExtensionImpl(Logger logger)
   {
      delegate = new FelixResolverState(logger, null);
   }

   @Override
   public void addModule(Module module)
   {
      delegate.addModule(module);
   }

   @Override
   public void removeModule(Module module)
   {
      delegate.removeModule(module);
   }

   @Override
   public void detachFragment(Module host, Module fragment)
   {
      delegate.detachFragment(host, fragment);
   }

   @Override
   public Set<Capability> getCandidates(Module module, Requirement req, boolean obeyMandatory)
   {
      return delegate.getCandidates(module, req, obeyMandatory);
   }

   @Override
   public void checkExecutionEnvironment(Module module) throws ResolveException
   {
      delegate.checkExecutionEnvironment(module);
   }

   @Override
   public void checkNativeLibraries(Module module) throws ResolveException
   {
      delegate.checkExecutionEnvironment(module);
   }

   @Override
   public void checkSingleton(Module module)
   {
      delegate.checkSingleton(module);
   }

   @Override
   public Module findHost(Module rootModule) throws ResolveException
   {
      return delegate.findHost(rootModule);
   }

   @Override
   public void moduleResolved(Module module)
   {
      delegate.moduleResolved(module);
   }
}