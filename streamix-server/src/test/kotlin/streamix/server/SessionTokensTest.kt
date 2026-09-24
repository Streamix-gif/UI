package streamix.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SessionTokensTest {
    @Test
    fun issuesAndVerifiesSession() {
        val tokens = SessionTokens("test-secret")
        val token = tokens.issue("uid-1", "user@example.com", "Shin")
        assertEquals("uid-1", tokens.verify(token).uid)
    }

    @Test
    fun rejectsTamperedSession() {
        val tokens = SessionTokens("test-secret")
        val token = tokens.issue("uid-1", null, "Shin")
        val tampered = token.dropLast(1) + if (token.last() == 'a') 'b' else 'a'
        assertFails { tokens.verify(tampered) }
    }
}
