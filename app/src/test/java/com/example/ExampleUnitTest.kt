package com.example

import com.example.ui.components.appendKeyWithAutoDot
import com.example.ui.components.deleteLastKeyFromIp
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testIpAutoDotFirstAndSecondOctets() {
        var ip = ""

        // Type 192 -> auto appends dot
        ip = appendKeyWithAutoDot(ip, "1")
        assertEquals("1", ip)
        ip = appendKeyWithAutoDot(ip, "9")
        assertEquals("19", ip)
        ip = appendKeyWithAutoDot(ip, "2")
        assertEquals("192.", ip)

        // Type 168 -> auto appends dot
        ip = appendKeyWithAutoDot(ip, "1")
        assertEquals("192.1", ip)
        ip = appendKeyWithAutoDot(ip, "6")
        assertEquals("192.16", ip)
        ip = appendKeyWithAutoDot(ip, "8")
        assertEquals("192.168.", ip)

        // Third octet does NOT auto-append dot
        ip = appendKeyWithAutoDot(ip, "1")
        assertEquals("192.168.1", ip)

        // User manually presses dot
        ip = appendKeyWithAutoDot(ip, ".")
        assertEquals("192.168.1.", ip)

        // Fourth octet
        ip = appendKeyWithAutoDot(ip, "5")
        assertEquals("192.168.1.5", ip)
        ip = appendKeyWithAutoDot(ip, "0")
        assertEquals("192.168.1.50", ip)
    }

    @Test
    fun testIpDeletion() {
        var ip = "192.168."
        ip = deleteLastKeyFromIp(ip)
        assertEquals("192.168", ip)

        ip = deleteLastKeyFromIp(ip)
        assertEquals("192.16", ip)

        ip = deleteLastKeyFromIp("192.")
        assertEquals("192", ip)
    }
}
