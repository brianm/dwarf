/* From JODD --  http://jodd.org/
   Original Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.
     - Licensed under Apache License, 2.0
   Local modifications by Brian McCallister
     - Trim whitespace from names in templates
*/

package org.skife.galaxy.dwarf;

import java.util.Map;

/**
 * Parser for string macro templates. On parsing, macro values
 * in provided string are resolved and replaced with real values.
 * Once set, one string template parser can be reused for parsing,
 * even using different macro resolvers.
 */
public class TemplateParser
{

    public static final String DEFAULT_MACRO_START = "${";
    public static final String DEFAULT_MACRO_END   = "}";

    // ---------------------------------------------------------------- properties

    protected boolean replaceMissingKey = true;
    protected String missingKeyReplacement;
    protected boolean resolveEscapes = true;
    protected String  macroStart     = DEFAULT_MACRO_START;
    protected String  macroEnd       = DEFAULT_MACRO_END;
    protected char    escapeChar     = '\\';
    protected boolean parseValues;

    public boolean isReplaceMissingKey()
    {
        return replaceMissingKey;
    }

    /**
     * Specifies if missing keys should be resolved at all,
     * <code>true</code> by default.
     * If <code>false</code> missing keys will be left as it were, i.e.
     * they will not be replaced.
     */
    public void setReplaceMissingKey(boolean replaceMissingKey)
    {
        this.replaceMissingKey = replaceMissingKey;
    }

    public String getMissingKeyReplacement()
    {
        return missingKeyReplacement;
    }

    /**
     * Specifies replacement for missing keys. If <code>null</code>
     * exception will be thrown.
     */
    public void setMissingKeyReplacement(String missingKeyReplacement)
    {
        this.missingKeyReplacement = missingKeyReplacement;
    }

    public boolean isResolveEscapes()
    {
        return resolveEscapes;
    }

    /**
     * Specifies if escaped values should be resolved. In special usecases,
     * when the same string has to be processed more then once,
     * this may be set to <code>false</code> so escaped values
     * remains.
     */
    public void setResolveEscapes(boolean resolveEscapes)
    {
        this.resolveEscapes = resolveEscapes;
    }

    public String getMacroStart()
    {
        return macroStart;
    }

    /**
     * Defines macro start string.
     */
    public void setMacroStart(String macroStart)
    {
        this.macroStart = macroStart;
    }

    public String getMacroEnd()
    {
        return macroEnd;
    }

    /**
     * Defines macro end string.
     */
    public void setMacroEnd(String macroEnd)
    {
        this.macroEnd = macroEnd;
    }

    public char getEscapeChar()
    {
        return escapeChar;
    }

    /**
     * Defines escape character.
     */
    public void setEscapeChar(char escapeChar)
    {
        this.escapeChar = escapeChar;
    }

    public boolean isParseValues()
    {
        return parseValues;
    }

    /**
     * Defines if macro values has to be parsed, too.
     * By default, macro values are returned as they are.
     */
    public void setParseValues(boolean parseValues)
    {
        this.parseValues = parseValues;
    }


    // ---------------------------------------------------------------- parse

    /**
     * Parses string template and replaces macros with resolved values.
     */
    public String parse(String template, MacroResolver macroResolver)
    {
        StringBuilder result = new StringBuilder(template.length());

        int i = 0;
        int len = template.length();

        int startLen = macroStart.length();
        int endLen = macroEnd.length();

        while (i < len) {
            int ndx = template.indexOf(macroStart, i);
            if (ndx == -1) {
                result.append(i == 0 ? template : template.substring(i));
                break;
            }

            // check escaped
            int j = ndx - 1;
            boolean escape = false;
            int count = 0;

            while ((j >= 0) && (template.charAt(j) == escapeChar)) {
                escape = !escape;
                if (escape) {
                    count++;
                }
                j--;
            }
            if (resolveEscapes) {
                result.append(template.substring(i, ndx - count));
            }
            else {
                result.append(template.substring(i, ndx));
            }
            if (escape == true) {
                result.append(macroStart);
                i = ndx + startLen;
                continue;
            }

            // find macros end
            ndx += startLen;
            int ndx2 = template.indexOf(macroEnd, ndx);
            if (ndx2 == -1) {
                throw new IllegalArgumentException("Invalid string template, unclosed macro at: " + (ndx - startLen));
            }

            // detect inner macros, there is no escaping
            int ndx1 = ndx;
            while (ndx1 < ndx2) {
                int n = indexOf(template, macroStart, ndx1, ndx2);
                if (n == -1) {
                    break;
                }
                ndx1 = n + startLen;
            }

            String name = template.substring(ndx1, ndx2).trim();

            // find value and append
            Object value;
            if (missingKeyReplacement != null || replaceMissingKey == false) {
                try {
                    value = macroResolver.resolve(name);
                }
                catch (Exception ignore) {
                    value = null;
                }

                if (value == null) {
                    if (replaceMissingKey == true) {
                        value = missingKeyReplacement;
                    }
                    else {
                        value = template.substring(ndx1 - startLen, ndx2 + 1);
                    }
                }
            }
            else {
                value = macroResolver.resolve(name);
                if (value == null) {
                    value = "";
                }
            }

            if (ndx == ndx1) {
                String stringValue = value.toString();
                if (parseValues == true) {
                    if (stringValue.contains(macroStart)) {
                        stringValue = parse(stringValue, macroResolver);
                    }
                }
                result.append(stringValue);
                i = ndx2 + endLen;
            }
            else {
                // inner macro
                template = template.substring(0, ndx1 - startLen) + value.toString() + template.substring(ndx2 + endLen);
                len = template.length();
                i = ndx - startLen;
            }
        }
        return result.toString();
    }


    /**
     * Finds first occurrence of a substring in the given source but within limited range [start, end).
     * It is fastest possible code, but still original <code>String.indexOf(String, int)</code>
     * is much faster (since it uses char[] value directly) and should be used when no range is needed.
     *
     * @param src        source string for examination
     * @param sub        substring to find
     * @param startIndex starting index
     * @param endIndex   ending index
     *
     * @return index of founded substring or -1 if substring not found
     */
    private static int indexOf(String src, String sub, int startIndex, int endIndex)
    {
        if (startIndex < 0) {
            startIndex = 0;
        }
        int srclen = src.length();
        if (endIndex > srclen) {
            endIndex = srclen;
        }
        int sublen = sub.length();
        if (sublen == 0) {
            return startIndex > srclen ? srclen : startIndex;
        }

        int total = endIndex - sublen + 1;
        char c = sub.charAt(0);
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (src.charAt(i) != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub.charAt(j) != src.charAt(k)) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }


    // ---------------------------------------------------------------- resolver

    /**
     * Macro value resolver.
     */
    public interface MacroResolver
    {
        /**
         * Resolves macro value for macro name founded in
         * string template. <code>null</code> values will
         * be replaced with empty strings.
         */
        String resolve(String macroName);

    }

    /**
     * Creates commonly used {@link MacroResolver} that resolved
     * macros in the provided map.
     */
    public static MacroResolver createResolver(final String source, final Map map)
    {
        return new MacroResolver()
        {
            public String resolve(String macroName)
            {
                if (macroName.contains("|")) {
                    String[] parts = macroName.split("\\|");
                    if (map.containsKey(parts[0].trim())) {
                        return map.get(parts[0].trim()).toString();
                    }
                    else {
                        return parts[1].trim();
                    }
                }
                else if (map.containsKey(macroName.trim())) {
                    return map.get(macroName.trim()).toString();

                }
                else {
                    throw new IllegalStateException("Unable to evaluate template " + source +
                                                    "\nNo value defined for '" + macroName.trim() + "'");
                }
            }
        };
    }

    /**
     * Creates commonly used {@link MacroResolver} that resolved
     * macros in the provided map.
     */
    public static MacroResolver createResolver(final Map map)
    {
        return createResolver("<WHA?>", map);
    }
}