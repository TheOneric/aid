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
