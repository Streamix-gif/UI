package streamix.social

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SessionTokensTest {
    @Test fun roundTrip() {
        val tokens=SessionTokens("test-secret")
        val token=tokens.issue("uid-1","user@example.com","AniLab User")
        assertEquals("uid-1",tokens.verify(token).uid)
    }
    @Test fun tamperRejected() {
        val tokens=SessionTokens("test-secret")
        val token=tokens.issue("uid-1",null,"AniLab User")
        assertFails { tokens.verify(token.dropLast(1)+"x") }
    }
}
