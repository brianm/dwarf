package org.skife.galaxy.dwarf;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class UriBox
{
    private static final Set<Path>               TO_DELETE = Sets.newCopyOnWriteArraySet();
    private static final LoadingCache<URI, Path> cache     = CacheBuilder.newBuilder()
                                                                         .maximumSize(Long.MAX_VALUE)
                                                                         .build(new Loader());

    static final URI BASE = Paths.get(".").toUri();

    private static class Loader extends CacheLoader<URI, Path>
    {

        @Override
        public Path load(URI uri) throws Exception
        {
            Path bundle_tmp = Files.createTempFile("uribox", ".tmp");
            TO_DELETE.add(bundle_tmp);
            try (InputStream in = BASE.resolve(uri).toURL().openStream()) {
                Files.copy(in, bundle_tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            return bundle_tmp;
        }
    }

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
        try {
            return cache.get(uri);
        }
        catch (ExecutionException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
            throw Throwables.propagate(e); // should not be reached
        }
    }
}
