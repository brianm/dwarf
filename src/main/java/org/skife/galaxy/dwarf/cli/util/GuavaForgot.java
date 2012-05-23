package org.skife.galaxy.dwarf.cli.util;

import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class GuavaForgot
{
    public static InputSupplier<InputStreamReader> fromUri(final URI uri)
    {
        return new InputSupplier<InputStreamReader>()
        {
            @Override
            public InputStreamReader getInput() throws IOException
            {
                return new InputStreamReader(uri.toURL().openStream());
            }
        };
    }
}
