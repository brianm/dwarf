package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Host implements Comparable<Host>
{
    private final String hostname;

    @JsonCreator
    public Host(@JsonProperty("hostname") String hostname)
    {
        this.hostname = hostname;
    }

    public String getHostname()
    {
        return hostname;
    }

    @Override
    public int compareTo(Host o)
    {
        return this.getHostname().compareTo(o.getHostname());
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
