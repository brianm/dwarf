package org.skife.galaxy.dwarf.cli;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.skife.galaxy.dwarf.cli.Search.minuuid;

public class SearchTest
{
    @Test
    public void testFoo() throws Exception
    {
        Map<UUID, String> rs = minuuid(ImmutableSet.of(UUID.fromString("c33c4c2f-e6b3-4830-9beb-b35ac312ae73"),
                                                       UUID.fromString("c33c4c3f-e6b3-4830-9beb-b35ac312ae73"),
                                                       UUID.fromString("c33c4c4f-e6b3-4830-9beb-b35ac312ae73")));
        assertThat(ImmutableSet.copyOf(rs.values())).isEqualTo(ImmutableSet.of("c33c4c2", "c33c4c3", "c33c4c4"));
    }
}
