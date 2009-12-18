/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.osgi.framework.metadata.internal;

import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.ACTIVATION_POLICY_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.INTEGER_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.PACKAGE_LIST_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.PARAM_ATTRIB_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.PATH_ATTRIB_LIST_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.QNAME_ATTRIB_LIST_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.STRING_LIST_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.STRING_VC;
import static org.jboss.osgi.framework.metadata.internal.ValueCreatorUtil.URL_VC;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATOR;
import static org.osgi.framework.Constants.BUNDLE_CATEGORY;
import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import static org.osgi.framework.Constants.BUNDLE_DESCRIPTION;
import static org.osgi.framework.Constants.BUNDLE_LOCALIZATION;
import static org.osgi.framework.Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_NATIVECODE;
import static org.osgi.framework.Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_UPDATELOCATION;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.FRAGMENT_HOST;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.REQUIRE_BUNDLE;

import java.net.URL;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import org.jboss.classloading.spi.version.VersionComparatorRegistry;
import org.jboss.deployers.vfs.spi.deployer.helpers.AbstractManifestMetaData;
import org.jboss.osgi.framework.metadata.ActivationPolicyMetaData;
import org.jboss.osgi.framework.metadata.OSGiMetaData;
import org.jboss.osgi.framework.metadata.PackageAttribute;
import org.jboss.osgi.framework.metadata.ParameterizedAttribute;
import org.jboss.osgi.framework.metadata.internal.AbstractVersionRange.OSGiVersionToOSGiVersionComparator;
import org.jboss.osgi.framework.metadata.internal.AbstractVersionRange.OSGiVersionToStringComparator;
import org.jboss.osgi.framework.metadata.internal.AbstractVersionRange.OSGiVersionToVersionComparator;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Abstract OSGi meta data.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractOSGiMetaData extends AbstractManifestMetaData implements OSGiMetaData
{
   private static final long serialVersionUID = 1L;

   static
   {
      VersionComparatorRegistry registry = VersionComparatorRegistry.getInstance();
      registry.registerVersionComparator(Version.class, Version.class, new OSGiVersionToOSGiVersionComparator());
      registry.registerVersionComparator(Version.class, org.jboss.classloading.spi.version.Version.class, new OSGiVersionToVersionComparator());
      registry.registerVersionComparator(Version.class, String.class, new OSGiVersionToStringComparator());
   }

   protected transient Map<String, Object> cachedAttributes = new ConcurrentHashMap<String, Object>();

   protected transient ParameterizedAttribute parameters;
   
   public AbstractOSGiMetaData()
   {
   }

   public AbstractOSGiMetaData(Manifest manifest)
   {
      super(manifest);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Dictionary<String, String> getHeaders(String locale)
   {
      // TODO localisation
      Map<Name, String> attributes = (Map) getManifest().getMainAttributes();
      Hashtable<String, String> result = new Hashtable<String, String>();
      for (Entry<Name, String> entry : attributes.entrySet())
         result.put(entry.getKey().toString(), entry.getValue());
      return result;
   }

   public String getHeader(String key)
   {
      return get(key, STRING_VC);
   }

   public ActivationPolicyMetaData getBundleActivationPolicy()
   {
      return get("Bundle-ActivationPolicy", ACTIVATION_POLICY_VC);
   }

   public String getBundleActivator()
   {
      return get(BUNDLE_ACTIVATOR, STRING_VC);
   }

   public List<String> getBundleCategory()
   {
      return get(BUNDLE_CATEGORY, STRING_LIST_VC);
   }

   public List<String> getBundleClassPath()
   {
      return get(BUNDLE_CLASSPATH, STRING_LIST_VC, Arrays.asList("."));
   }

   public String getBundleDescription()
   {
      return get(BUNDLE_DESCRIPTION, STRING_VC);
   }

   public String getBundleLocalization()
   {
      return get(BUNDLE_LOCALIZATION, STRING_VC, BUNDLE_LOCALIZATION_DEFAULT_BASENAME);
   }

   public int getBundleManifestVersion()
   {
      return get(BUNDLE_MANIFESTVERSION, INTEGER_VC, 2);
   }

   public String getBundleName()
   {
      return get(BUNDLE_NAME, STRING_VC);
   }

   public List<ParameterizedAttribute> getBundleNativeCode()
   {
      return get(BUNDLE_NATIVECODE, PATH_ATTRIB_LIST_VC);
   }

   public List<String> getRequiredExecutionEnvironment()
   {
      return get(BUNDLE_REQUIREDEXECUTIONENVIRONMENT, STRING_LIST_VC);
   }

   public String getBundleSymbolicName()
   {
      String symbolicName = null;
      
      ParameterizedAttribute parameters = parseSymbolicName();
      if (parameters != null)
         symbolicName = parameters.getAttribute();
      
      return symbolicName;
   }

   public ParameterizedAttribute getBundleParameters()
   {
      return parseSymbolicName();
   }
   
   public URL getBundleUpdateLocation()
   {
      return get(BUNDLE_UPDATELOCATION, URL_VC);
   }

   public String getBundleVersion()
   {
      return get(BUNDLE_VERSION, STRING_VC , "0.0.0");
   }

   public List<PackageAttribute> getDynamicImports()
   {
      return get(DYNAMICIMPORT_PACKAGE, PACKAGE_LIST_VC);
   }

   public List<PackageAttribute> getExportPackages()
   {
      return get(EXPORT_PACKAGE, PACKAGE_LIST_VC);
   }

   public ParameterizedAttribute getFragmentHost()
   {
      return get(FRAGMENT_HOST, PARAM_ATTRIB_VC);
   }

   public List<PackageAttribute> getImportPackages()
   {
      return get(IMPORT_PACKAGE, PACKAGE_LIST_VC);
   }

   public List<ParameterizedAttribute> getRequireBundles()
   {
      return get(REQUIRE_BUNDLE, QNAME_ATTRIB_LIST_VC);
   }

   public boolean isSingleton()
   {
      parseSymbolicName();
      if (parameters == null)
         return false;
      return "true".equals(parameters.getDirectiveValue(Constants.SINGLETON_DIRECTIVE, String.class));
   }

   public String getFragmentAttachment()
   {
      parseSymbolicName();
      if (parameters == null)
         return null;
      return parameters.getDirectiveValue(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, String.class);
   }

   protected ParameterizedAttribute parseSymbolicName()
   {
      if (parameters == null)
      {
         List<ParameterizedAttribute> parsed = get(BUNDLE_SYMBOLICNAME, QNAME_ATTRIB_LIST_VC);
         if (parsed == null || parsed.size() != 1)
            return null;
         parameters = parsed.get(0);
      }
      return parameters;
   }

   protected <T> T get(String key, ValueCreator<T> creator)
   {
      return get(key, creator, null);
   }

   @SuppressWarnings("unchecked")
   protected <T> T get(String key, ValueCreator<T> creator, T defaultValue)
   {
      T value = (T)cachedAttributes.get(key);
      if (value == null)
      {
         String attribute = getMainAttribute(key);
         if (attribute != null)
         {
            value = creator.createValue(attribute);
         }
         else if (defaultValue != null)
         {
            value = defaultValue;
         }
         if (value != null)
            cachedAttributes.put(key, value);
      }
      return value;
   }

}
