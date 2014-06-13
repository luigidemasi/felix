/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.framework.util.manifestparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.VersionRange;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

public class R4LibraryClause
{
    private static final String OS_AIX = "aix";
    private static final String OS_DIGITALUNIX = "digitalunix";
    private static final String OS_HPUX = "hpux";
    private static final String OS_IRIX = "irix";
    private static final String OS_LINUX = "linux";
    private static final String OS_MACOS = "macos";
    private static final String OS_NETBSD = "netbsd";
    private static final String OS_NETWARE = "netware";
    private static final String OS_OPENBSD = "openbsd";
    private static final String OS_OS2 = "os2";
    private static final String OS_QNX = "qnx";
    private static final String OS_SOLARIS = "solaris";
    private static final String OS_SUNOS = "sunos";
    private static final String OS_VXWORKS = "vxworks";
    private static final String OS_WINDOWS_2000 = "windows2000";
    private static final String OS_WINDOWS_2003 = "windows2003";
    private static final String OS_WINDOWS_7 = "windows7";
    private static final String OS_WINDOWS_8 = "windows8";
    private static final String OS_WINDOWS_9 = "windows9";
    private static final String OS_WINDOWS_95 = "windows95";
    private static final String OS_WINDOWS_98 = "windows98";
    private static final String OS_WINDOWS_CE = "windowsce";
    private static final String OS_WINDOWS_NT = "windowsnt";
    private static final String OS_WINDOWS_SERVER_2008 = "windowsserver2008";
    private static final String OS_WINDOWS_SERVER_2012 = "windowsserver2012";
    private static final String OS_WINDOWS_VISTA = "windowsvista";
    private static final String OS_WINDOWS_XP = "windowsxp";
    private static final String OS_WIN_32 = "win32";

    /* Storing the OS names in a map as this is quicker to look up.
     */
    private static final Map<String, String> NORMALIZED_OS_NAMES;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put(OS_AIX, "");
        m.put(OS_DIGITALUNIX, "");
        m.put(OS_HPUX, "");
        m.put(OS_IRIX, "");
        m.put(OS_LINUX, "");
        m.put(OS_MACOS, "");
        m.put(OS_NETBSD, "");
        m.put(OS_NETWARE, "");
        m.put(OS_OPENBSD, "");
        m.put(OS_OS2, "");
        m.put(OS_QNX, "");
        m.put(OS_SOLARIS, "");
        m.put(OS_SUNOS, "");
        m.put(OS_VXWORKS, "");
        m.put(OS_WINDOWS_2000, "");
        m.put(OS_WINDOWS_2003, "");
        m.put(OS_WINDOWS_7, "");
        m.put(OS_WINDOWS_8, "");
        m.put(OS_WINDOWS_9, "");
        m.put(OS_WINDOWS_95, "");
        m.put(OS_WINDOWS_98, "");
        m.put(OS_WINDOWS_CE, "");
        m.put(OS_WINDOWS_NT, "");
        m.put(OS_WINDOWS_SERVER_2008, "");
        m.put(OS_WINDOWS_SERVER_2012, "");
        m.put(OS_WINDOWS_VISTA, "");
        m.put(OS_WINDOWS_XP, "");
        m.put(OS_WIN_32, "");

        NORMALIZED_OS_NAMES = Collections.unmodifiableMap(m);
    }

    private final String[] m_libraryEntries;
    private final String[] m_osnames;
    private final String[] m_processors;
    private final String[] m_osversions;
    private final String[] m_languages;
    private final String m_selectionFilter;

    public R4LibraryClause(String[] libraryEntries, String[] osnames,
        String[] processors, String[] osversions, String[] languages,
        String selectionFilter)
    {
        m_libraryEntries = libraryEntries;
        m_osnames = osnames;
        m_processors = processors;
        m_osversions = osversions;
        m_languages = languages;
        m_selectionFilter = selectionFilter;
    }

    public R4LibraryClause(R4LibraryClause library)
    {
        m_libraryEntries = library.m_libraryEntries;
        m_osnames = library.m_osnames;
        m_osversions = library.m_osversions;
        m_processors = library.m_processors;
        m_languages = library.m_languages;
        m_selectionFilter = library.m_selectionFilter;
    }

    public String[] getLibraryEntries()
    {
        return m_libraryEntries;
    }

    public String[] getOSNames()
    {
        return m_osnames;
    }

    public String[] getProcessors()
    {
        return m_processors;
    }

    public String[] getOSVersions()
    {
        return m_osversions;
    }

    public String[] getLanguages()
    {
        return m_languages;
    }

    public String getSelectionFilter()
    {
        return m_selectionFilter;
    }

    public boolean match(Map configMap) throws BundleException
    {
        String normal_osname = normalizeOSName((String) configMap.get(Constants.FRAMEWORK_OS_NAME));
        String normal_processor = normalizeProcessor((String) configMap.get(Constants.FRAMEWORK_PROCESSOR));
        String normal_osversion = normalizeOSVersion((String) configMap.get(Constants.FRAMEWORK_OS_VERSION));
        String normal_language = (String) configMap.get(Constants.FRAMEWORK_LANGUAGE);

        // Check library's osname.
        if (!checkOSNames(normal_osname, getOSNames()))
        {
            return false;
        }

        // Check library's processor.
        if (!checkProcessors(normal_processor, getProcessors()))
        {
            return false;
        }

        // Check library's osversion if specified.
        if ((getOSVersions() != null) &&
            (getOSVersions().length > 0) &&
            !checkOSVersions(normal_osversion, getOSVersions()))
        {
            return false;
        }

        // Check library's language if specified.
        if ((getLanguages() != null) &&
            (getLanguages().length > 0) &&
            !checkLanguages(normal_language, getLanguages()))
        {
            return false;
        }

        // Check library's selection-filter if specified.
        if ((getSelectionFilter() != null) &&
            (getSelectionFilter().length() >= 0) &&
            !checkSelectionFilter(configMap, getSelectionFilter()))
        {
            return false;
        }

        return true;
    }

    private boolean checkOSNames(String currentOSName, String[] osnames)
    {
        boolean win32 = currentOSName.startsWith("win") && !currentOSName.equals(OS_WINDOWS_CE);

        for (int i = 0; (osnames != null) && (i < osnames.length); i++)
        {
            if (osnames[i].equals(currentOSName) ||
                (OS_WIN_32.equals(osnames[i]) && win32))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkProcessors(String currentProcessor, String[] processors)
    {
        for (int i = 0; (processors != null) && (i < processors.length); i++)
        {
            if (processors[i].equals(currentProcessor))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkOSVersions(String currentOSVersion, String[] osversions)
        throws BundleException
    {
        for (int i = 0; (osversions != null) && (i < osversions.length); i++)
        {
            try
            {
                VersionRange range = VersionRange.parse(osversions[i]);
                if (range.isInRange(new Version(currentOSVersion)))
                {
                    return true;
                }
            }
            catch (Exception ex)
            {
                throw new BundleException(
                    "Error evaluating osversion: " + osversions[i], ex);
            }
        }
        return false;
    }

    private boolean checkLanguages(String currentLanguage, String[] languages)
    {
        for (int i = 0; (languages != null) && (i < languages.length); i++)
        {
            if (languages[i].equals(currentLanguage))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkSelectionFilter(Map configMap, String expr)
        throws BundleException
    {
        // Get all framework properties
        Dictionary dict = new Hashtable();
        for (Iterator i = configMap.keySet().iterator(); i.hasNext(); )
        {
            Object key = i.next();
            dict.put(key, configMap.get(key));
        }
        // Compute expression
        try
        {
            Filter filter = FrameworkUtil.createFilter(expr);
            return filter.match(dict);
        }
        catch (Exception ex)
        {
            throw new BundleException(
                "Error evaluating filter expression: " + expr, ex);
        }
    }

    public static R4LibraryClause parse(Logger logger, String s)
    {
        try
        {
            if ((s == null) || (s.length() == 0))
            {
                return null;
            }

            if (s.equals(FelixConstants.BUNDLE_NATIVECODE_OPTIONAL))
            {
                return new R4LibraryClause(null, null, null, null, null, null);
            }

            // The tokens are separated by semicolons and may include
            // any number of libraries along with one set of associated
            // properties.
            StringTokenizer st = new StringTokenizer(s, ";");
            String[] libEntries = new String[st.countTokens()];
            List osNameList = new ArrayList();
            List osVersionList = new ArrayList();
            List processorList = new ArrayList();
            List languageList = new ArrayList();
            String selectionFilter = null;
            int libCount = 0;
            while (st.hasMoreTokens())
            {
                String token = st.nextToken().trim();
                if (token.indexOf('=') < 0)
                {
                    // Remove the slash, if necessary.
                    libEntries[libCount] = (token.charAt(0) == '/')
                        ? token.substring(1)
                        : token;
                    libCount++;
                }
                else
                {
                    // Check for valid native library properties; defined as
                    // a property name, an equal sign, and a value.
                    // NOTE: StringTokenizer can not be used here because
                    // a value can contain one or more "=" too, e.g.,
                    // selection-filter="(org.osgi.framework.windowing.system=gtk)"
                    String property = null;
                    String value = null;
                    if (!(token.indexOf("=") > 1))
                    {
                        throw new IllegalArgumentException(
                            "Bundle manifest native library entry malformed: " + token);
                    }
                    else
                    {
                        property = (token.substring(0, token.indexOf("=")))
                            .trim().toLowerCase();
                        value = (token.substring(token.indexOf("=") + 1, token
                            .length())).trim();
                    }

                    // Values may be quoted, so remove quotes if present.
                    if (value.charAt(0) == '"')
                    {
                        // This should always be true, otherwise the
                        // value wouldn't be properly quoted, but we
                        // will check for safety.
                        if (value.charAt(value.length() - 1) == '"')
                        {
                            value = value.substring(1, value.length() - 1);
                        }
                        else
                        {
                            value = value.substring(1);
                        }
                    }
                    // Add the value to its corresponding property list.
                    if (property.equals(Constants.BUNDLE_NATIVECODE_OSNAME))
                    {
                        osNameList.add(normalizeOSName(value));
                    }
                    else if (property.equals(Constants.BUNDLE_NATIVECODE_OSVERSION))
                    {
                        osVersionList.add(normalizeOSVersion(value));
                    }
                    else if (property.equals(Constants.BUNDLE_NATIVECODE_PROCESSOR))
                    {
                        processorList.add(normalizeProcessor(value));
                    }
                    else if (property.equals(Constants.BUNDLE_NATIVECODE_LANGUAGE))
                    {
                        languageList.add(value);
                    }
                    else if (property.equals(Constants.SELECTION_FILTER_ATTRIBUTE))
                    {
// TODO: NATIVE - I believe we can have multiple selection filters too.
                        selectionFilter = value;
                    }
                }
            }

            if (libCount == 0)
            {
                return null;
            }

            // Shrink lib file array.
            String[] actualLibEntries = new String[libCount];
            System.arraycopy(libEntries, 0, actualLibEntries, 0, libCount);
            return new R4LibraryClause(
                actualLibEntries,
                (String[]) osNameList.toArray(new String[osNameList.size()]),
                (String[]) processorList.toArray(new String[processorList.size()]),
                (String[]) osVersionList.toArray(new String[osVersionList.size()]),
                (String[]) languageList.toArray(new String[languageList.size()]),
                selectionFilter);
        }
        catch (RuntimeException ex)
        {
            logger.log(Logger.LOG_ERROR,
                "Error parsing native library header.", ex);
            throw ex;
        }
    }

    public static String normalizeOSName(String value)
    {
        if (NORMALIZED_OS_NAMES.containsKey(value))
        {
            // Already normalized
            return value;
        }

        value = value.toLowerCase();

        if (value.startsWith("win"))
        {
            String os = "win";
            if (value.indexOf("32") >= 0 || value.indexOf("*") >= 0)
            {
                os = OS_WIN_32;
            }
            else if (value.indexOf("95") >= 0)
            {
                os = OS_WINDOWS_95;
            }
            else if (value.indexOf("98") >= 0)
            {
                os = OS_WINDOWS_98;
            }
            else if (value.indexOf("nt") >= 0)
            {
                os = OS_WINDOWS_NT;
            }
            else if (value.indexOf("2000") >= 0)
            {
                os = OS_WINDOWS_2000;
            }
            else if (value.indexOf("2003") >= 0)
            {
                os = OS_WINDOWS_2003;
            }
            else if (value.indexOf("2008") >= 0)
            {
                os = OS_WINDOWS_SERVER_2008;
            }
            else if (value.indexOf("2012") >= 0)
            {
                os = OS_WINDOWS_SERVER_2012;
            }
            else if (value.indexOf("xp") >= 0)
            {
                os = OS_WINDOWS_XP;
            }
            else if (value.indexOf("ce") >= 0)
            {
                os = OS_WINDOWS_CE;
            }
            else if (value.indexOf("vista") >= 0)
            {
                os = OS_WINDOWS_VISTA;
            }
            else if ((value.indexOf(" 7") >= 0) || value.equals("win7"))
            {
                os = OS_WINDOWS_7;
            }
            else if ((value.indexOf(" 8") >= 0) || value.equals("win8"))
            {
                os = OS_WINDOWS_8;
            }
            else if ((value.indexOf(" 9") >= 0) || value.equals("win9"))
            {
                os = OS_WINDOWS_9;
            }
            return os;
        }
        else if (value.startsWith(OS_LINUX))
        {
            return OS_LINUX;
        }
        else if (value.startsWith(OS_AIX))
        {
            return OS_AIX;
        }
        else if (value.startsWith(OS_DIGITALUNIX))
        {
            return OS_DIGITALUNIX;
        }
        else if (value.startsWith(OS_HPUX))
        {
            return OS_HPUX;
        }
        else if (value.startsWith(OS_IRIX))
        {
            return OS_IRIX;
        }
        else if (value.startsWith(OS_MACOS) || value.startsWith("mac os"))
        {
            return OS_MACOS;
        }
        else if (value.startsWith(OS_NETWARE))
        {
            return OS_NETWARE;
        }
        else if (value.startsWith(OS_OPENBSD))
        {
            return OS_OPENBSD;
        }
        else if (value.startsWith(OS_NETBSD))
        {
            return OS_NETBSD;
        }
        else if (value.startsWith(OS_OS2) || value.startsWith("os/2"))
        {
            return OS_OS2;
        }
        else if (value.startsWith(OS_QNX) || value.startsWith("procnto"))
        {
            return OS_QNX;
        }
        else if (value.startsWith(OS_SOLARIS))
        {
            return OS_SOLARIS;
        }
        else if (value.startsWith(OS_SUNOS))
        {
            return OS_SUNOS;
        }
        else if (value.startsWith(OS_VXWORKS))
        {
            return OS_VXWORKS;
        }
        return value;
    }

    public static String normalizeProcessor(String value)
    {
        value = value.toLowerCase();

        if (value.startsWith("x86-64") || value.startsWith("amd64") ||
            value.startsWith("em64") || value.startsWith("x86_64"))
        {
            return "x86-64";
        }
        else if (value.startsWith("x86") || value.startsWith("pentium")
            || value.startsWith("i386") || value.startsWith("i486")
            || value.startsWith("i586") || value.startsWith("i686"))
        {
            return "x86";
        }
        else if (value.startsWith("68k"))
        {
            return "68k";
        }
        else if (value.startsWith("arm"))
        {
            return "arm";
        }
        else if (value.startsWith("alpha"))
        {
            return "alpha";
        }
        else if (value.startsWith("ignite") || value.startsWith("psc1k"))
        {
            return "ignite";
        }
        else if (value.startsWith("mips"))
        {
            return "mips";
        }
        else if (value.startsWith("parisc"))
        {
            return "parisc";
        }
        else if (value.startsWith("powerpc") || value.startsWith("power")
            || value.startsWith("ppc"))
        {
            return "powerpc";
        }
        else if (value.startsWith("sparc"))
        {
            return "sparc";
        }
        return value;
    }

    public static String normalizeOSVersion(String value)
    {
        // Header: 'Bundle-NativeCode', Parameter: 'osversion'
        // Standardized 'osversion': major.minor.micro, only digits
        try
        {
            return VersionRange.parse(value).toString();
        }
        catch (Exception ex)
        {
            return Version.emptyVersion.toString();
        }
    }
}
