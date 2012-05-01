package org.skife.galaxy.dwarf;

import org.junit.Test;
import org.skife.galaxy.dwarf.state.Host;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.fest.assertions.Assertions.assertThat;

public class TestMethodHandleStuff
{
    @Test
    public void testFoo() throws Throwable
    {
        MethodHandle h = MethodHandles.lookup().findVirtual(Host.class,
                                                            "getHostname",
                                                            MethodType.methodType(String.class));
        String out = (String) h.bindTo(new Host("woof")).invoke();
        assertThat(out).isEqualTo("woof");
    }

    @Test
    public void testBaz() throws Exception
    {
    }


    @Test
    public void testBar() throws Throwable
    {

        MethodHandle to_upper = MethodHandles.lookup().findVirtual(String.class,
                                                                   "toUpperCase",
                                                                   MethodType.methodType(String.class));

        MethodHandle h = MethodHandles.lookup().findVirtual(Meow.class,
                                                            "say",
                                                            MethodType.methodType(String.class, String.class));

        assertThat(h.bindTo(new Meow()).bindTo("hello").invoke()).isEqualTo("meow hello");
        assertThat(h.invoke(new Meow(), "hello")).isEqualTo("meow hello");

        assertThat(MethodHandles.filterArguments(h, 1, to_upper).invoke(new Meow(), "hello")).isEqualTo("meow HELLO");
    }

    public static class Meow
    {
        public String say(String msg)
        {
            return String.format("meow %s", msg);
        }
    }
}
