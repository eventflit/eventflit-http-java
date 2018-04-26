package com.eventflit.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;

import com.eventflit.rest.data.PresenceUser;

public class EventflitChannelAuthTest {

    private final Eventflit p = new Eventflit("00001", "278d425bdf160c739803", "7ad3773142a6692b25b8");

    @Test
    public void privateChannelAuth() {
        assertThat(p.authenticate("1234.1234", "private-foobar"),
                is("{\"auth\":\"278d425bdf160c739803:58df8b0c36d6982b82c3ecf6b4662e34fe8c25bba48f5369f135bf843651c3a4\"}"));
    }

    @Test
    public void complexPrivateChannelAuth() {
        assertThat(p.authenticate("1234.1234", "private-azAZ9_=@,.;"),
                is("{\"auth\":\"278d425bdf160c739803:208cbbce2a22fd7d7c3509046b17a97b99d345cf4c195bc0d54af9004a022b0b\"}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void privateChannelWrongPrefix() {
        p.authenticate("1234.1234", "presence-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void privateChannelNoPrefix() {
        p.authenticate("1234.1234", "foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingColonSocketId() {
        p.authenticate("1.1:", "private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingNLSocketId() {
        p.authenticate("1.1\n", "private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void leadingColonSocketId() {
        p.authenticate(":1.1", "private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void leadingColonNLSocketId() {
        p.authenticate(":\n1.1", "private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingColonNLSocketId() {
        p.authenticate("1.1\n:", "private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingColonChannel() {
        p.authenticate("1.1", "private-foobar:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void leadingColonChannel() {
        p.authenticate("1.1", ":private-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void leadingColonNLChannel() {
        p.authenticate("1.1", ":\nprivate-foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingColonNLChannel() {
        p.authenticate("1.1", "private-foobar\n:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void trailingNLChannel() {
        p.authenticate("1.1", "private-foobar\n");
    }

    @Test
    public void presenceChannelAuth() {
        assertThat(p.authenticate("1234.1234", "presence-foobar", new PresenceUser(Integer.valueOf(10), Collections.singletonMap("name", "Mr. Eventflit"))),
                is("{\"auth\":\"278d425bdf160c739803:2be06ed82a4f555af6d0669e6fba1bb9e0d93a1af0492c4e59a8e5f29200daa5\",\"channel_data\":\"{\\\"user_id\\\":10,\\\"user_info\\\":{\\\"name\\\":\\\"Mr. Eventflit\\\"}}\"}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void presenceChannelWrongPrefix() {
        p.authenticate("1234.1234", "private-foobar", new PresenceUser("dave"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void presenceChannelNoPrefix() {
        p.authenticate("1234.1234", "foobar", new PresenceUser("dave"));
    }
}
