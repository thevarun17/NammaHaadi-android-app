package com.nammahaadi.app.data.model

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class RoadReportTest {

    @Test
    fun `getFormattedCreatedAt returns correct string for Timestamp`() {
        val date = Date(1600000000000L) // Some fixed date
        val timestamp = Timestamp(date)
        val report = RoadReport(createdAt = timestamp)

        assertEquals(date.toString(), report.getFormattedCreatedAt())
    }

    @Test
    fun `getFormattedCreatedAt returns the string if createdAt is already a String`() {
        val dateString = "2023-10-27"
        val report = RoadReport(createdAt = dateString)

        assertEquals(dateString, report.getFormattedCreatedAt())
    }

    @Test
    fun `getFormattedCreatedAt returns empty string for null or unknown type`() {
        val reportWithNull = RoadReport(createdAt = null)
        val reportWithInt = RoadReport(createdAt = 123)

        assertEquals("", reportWithNull.getFormattedCreatedAt())
        assertEquals("", reportWithInt.getFormattedCreatedAt())
    }

    @Test
    fun `RoadReport default values are correct`() {
        val report = RoadReport()
        assertEquals("", report.id)
        assertEquals("ACTIVE", report.status)
        assertEquals(0.0, report.lat, 0.0)
        assertEquals(1, report.reportCount)
    }
}
