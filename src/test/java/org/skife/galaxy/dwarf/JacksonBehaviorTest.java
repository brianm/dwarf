package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class JacksonBehaviorTest
{

    @Test
    public void testFoo() throws Exception
    {
        Path deployments = Paths.get(".dwarf").resolve("deployments");
        Reader sup = Files.newBufferedReader(deployments, Charsets.UTF_8);
        List<Deployment> rs = new ObjectMapper().readValue(sup, new TypeReference<List<Deployment>>()
        {
        });
        sup.close();
    }

    @Test
    public void testBar() throws Exception
    {
        DataFormatDetector det = new DataFormatDetector(new JsonFactory(),
                                                        new YAMLFactory());
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        C c = mapper.readValue("{\"foo\":{\n\"type\":\"bar\",\"say\":\"hello\"}}", C.class);

        assertThat(c.foo).isInstanceOf(FooBar.class);
        assertThat(c.foo.yarp()).isEqualTo("hello");
    }


    @Test
    public void testYaml() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        C c = mapper.readValue("foo: { type: bar, say: hello }", C.class);

        assertThat(c.foo).isInstanceOf(FooBar.class);
        assertThat(c.foo.yarp()).isEqualTo("hello");
    }

    @Test
    public void testBaz() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        C c = mapper.readValue("{\"foo\":{\n\"type\":\"baz\",\"nice\":\"hello\", \"willy\":\"you\"}}", C.class);

        assertThat(c.foo).isInstanceOf(FooBaz.class);
        assertThat(c.foo.yarp()).isEqualTo("hello you");
    }

    public static class C
    {
        public Foo foo;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = FooBar.class, name = "bar"),
                   @JsonSubTypes.Type(value = FooBaz.class, name = "baz")})
    public static interface Foo
    {
        public String yarp();
    }

    public static class FooBar implements Foo
    {

        private final String say;

        @JsonCreator
        public FooBar(@JsonProperty("say") String say)
        {
            this.say = say;
        }

        @Override
        public String yarp()
        {
            return say;
        }
    }

    public static class FooBaz implements Foo
    {

        private final String nice;
        private final String willy;

        @JsonCreator
        public FooBaz(@JsonProperty("nice") String nice, @JsonProperty("willy") String willy)
        {
            this.nice = nice;
            this.willy = willy;
        }

        @Override
        public String yarp()
        {
            return String.format("%s %s", nice, willy);
        }
    }


}
