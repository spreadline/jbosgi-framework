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

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;


/**
 * A Module for the {@link XResolver}.
 *
 * This is the resolver representation of a {@link Bundle}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XModule 
{
   /**
    * Get the resover that this module has been added to 
    */
   XResolver getResolver();
   
   /**
    * Get the bundle associated with this module.
    */
   Bundle getBundle();

   /**
    * Get the module name
    */
   String getName();
   
   /**
    * Get the module version
    */
   Version getVersion();
   
   /**
    * Get this modules host capability
    */
   XHostCapability getHostCapability();
   
   /**
    * Get the package capabilities 
    */
   List<XPackageCapability> getPackageCapabilities();

   /**
    * Get all module capabilities 
    */
   List<XCapability> getCapabilities();

   /**
    * Get the bundle requirements
    */
   List<XBundleRequirement> getBundleRequirements();

   /**
    * Get the non-dynamic package requirements  
    */
   List<XPackageRequirement> getPackageRequirements();

   /**
    * Get the dynamic package requirements  
    */
   List<XPackageRequirement> getDynamicPackageRequirements();

   /**
    * Get all module requirements  
    */
   List<XRequirement> getRequirements();

   /**
    * Get the fragment host requirement if this module is a fragment
    * @return null if this module is not a fragment
    */
   XHostRequirement getHostRequirement();
   
   /**
    * True is this module represents a fragment
    */
   boolean isFragment();
}