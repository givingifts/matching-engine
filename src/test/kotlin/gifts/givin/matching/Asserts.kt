package gifts.givin.matching

import gifts.givin.matching.common.domain.Match
import org.junit.Assert

fun assertDifferentMatch(vararg matches: Match) {
    matches.forEach {
        Assert.assertNotEquals(it.id, getMatch(it.sendTo!!).sendTo)
    }
}

fun assertValidMatch(match: Match) {
    if (match.isDropped) {
        Assert.assertNull(match.sendTo)
        Assert.assertNull(match.receiveFrom)
        return
    }
    Assert.assertNotNull(match.sendTo)
    Assert.assertNotNull(match.receiveFrom)
    Assert.assertNotEquals(match.id, match.receiveFrom)
    Assert.assertNotEquals(match.id, match.sendTo)
}
