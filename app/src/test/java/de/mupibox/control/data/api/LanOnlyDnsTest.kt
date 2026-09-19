package de.mupibox.control.data.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class LanOnlyDnsTest {
    @Test fun acceptsPrivateAndLocalAddresses() {
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("192.168.2.114")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("10.1.2.3")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("172.31.2.3")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("127.0.0.1")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("169.254.1.2")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("fd00::1234")))
        assertTrue(LanOnlyDns.isLanAddress(InetAddress.getByName("fe80::1")))
    }

    @Test fun rejectsPublicAddresses() {
        assertFalse(LanOnlyDns.isLanAddress(InetAddress.getByName("8.8.8.8")))
        assertFalse(LanOnlyDns.isLanAddress(InetAddress.getByName("2001:4860:4860::8888")))
    }
}
