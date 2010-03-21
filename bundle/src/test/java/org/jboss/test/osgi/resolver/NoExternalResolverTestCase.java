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
package org.jboss.test.osgi.resolver;

// $Id$

import org.jboss.osgi.framework.resolver.Resolver;
import org.junit.Test;

/**
 * Test resolver functionality without external resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Nov-2009
 */
public class NoExternalResolverTestCase extends AbstractImportExportTest
{
   @Override
   protected Resolver getTestResolver()
   {
      return null;
   }

   @Test
   public void testPreferredExporterHigherVersion() throws Exception
   {
      System.out.println("FIXME [JBCL-133] Implement resolver preferences");
   }
}