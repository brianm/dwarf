package org.skife.galaxy.dwarf;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class UriBox
{
    private static final Set<Path> TO_DELETE = Sets.newCopyOnWriteArraySet();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (Path path : TO_DELETE) {
                    try {
                        if (Files.exists(path)) {
                            Files.delete(path);
                        }
                    }
                    catch (IOException e) {
                        // nothing to do, we are shutting down :-(
                    }
                }
            }
        }));
    }


    public static Path copyLocally(URI uri) throws IOException
    {

        Path bundle_tmp = Files.createTempFile("uribox", ".tmp");
        TO_DELETE.add(bundle_tmp);
        try (InputStream in = uri.toURL().openStream()) {
            Files.copy(in, bundle_tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        return bundle_tmp;
    }
}
