package de.mupibox.control.data.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalEndpointValidatorTest {
    @Test fun acceptsPrivateLanTargets() {
        assertTrue(LocalEndpointValidator.isLanHost("192.168.2.114"))
        assertTrue(LocalEndpointValidator.isLanHost("10.0.0.5"))
        assertTrue(LocalEndpointValidator.isLanHost("172.16.1.5"))
        assertTrue(LocalEndpointValidator.isLanHost("mupibox.local"))
        assertTrue(LocalEndpointValidator.isLanHost("mupibox"))
        assertTrue(LocalEndpointValidator.isLanHost("box.home.arpa"))
    }

    @Test fun rejectsPublicTargets() {
        assertFalse(LocalEndpointValidator.isLanHost("8.8.8.8"))
        assertFalse(LocalEndpointValidator.isLanHost("example.com"))
        assertFalse(LocalEndpointValidator.isLanHost("fc.example.com"))
        assertFalse(LocalEndpointValidator.isLanHost("fd.example.com"))
    }
}
