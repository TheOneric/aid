/*
    This file is part of AiD.

    AiD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AiD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AiD.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020  Oneric
*/
package de.oneric.aid;

import java.util.regex.Pattern;

public class Util {

    //Why is [^/]* neccessary ? Does it always need to match _the whole_ string ?
    private static final Pattern aodDomainsRegex = Pattern.compile("[^/]*\\.?anime-on-demand\\.de$");

    /**
     * The AoD base domain
     */
    public static final String DOMAIN_AOD  = "anime-on-demand.de";

    /**
     * Strip an HTTP(S) URI down to only the domain name
     */
    public static String domainOfUri(String uri) {
        return uri.replaceFirst("^https?://", "")
                  .replaceAll("(/.*)+$",      "");
    }

    /**
     * True for *any* AoD (sub)domain, but not for URIs.
     */
    public static boolean isAoDDomain(String domain) {
        return aodDomainsRegex.matcher(domain).matches();
    }

    /**
     * True if URI belongs to any AoD (sub)domain
     */
    public static boolean isAoDUri(String uri) {
        return isAoDDomain(domainOfUri(uri));
    }

}
