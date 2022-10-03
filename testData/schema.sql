CREATE
USER 'matching'@'%' IDENTIFIED WITH mysql_native_password by 'matching';
GRANT ALL
ON *.* TO 'matching'@'%';
FLUSH
PRIVILEGES;

DROP TABLE IF EXISTS Matches;
DROP TABLE IF EXISTS DoNotMatch;
DROP TABLE IF EXISTS MatchingInstances;

CREATE TABLE Matches
(
    id            bigint UNIQUE NOT NULL AUTO_INCREMENT,
    UserId        bigint UNIQUE NOT NULL,
    MatchingGroup varchar(40)   NOT NULL,
    IsMatched     boolean DEFAULT FALSE,
    SendTo        bigint,
    ReceiveFrom   bigint,
    CONSTRAINT PK_Matches PRIMARY KEY (id),
    CHECK (SendTo <> UserId),
    CHECK (ReceiveFrom <> UserId)
);

CREATE TABLE DoNotMatch
(
    id           bigint UNIQUE NOT NULL AUTO_INCREMENT,
    FirstUserId  bigint        NOT NULL,
    SecondUserId bigint        NOT NULL,
    CHECK (FirstUserId <> SecondUserId)
);

CREATE TABLE MatchingInstances
(
    id            bigint UNIQUE NOT NULL AUTO_INCREMENT,
    done          BOOLEAN     default FALSE,
    matchingGroup varchar(10) default NULL,
    CONSTRAINT PK_MatchingInstances PRIMARY KEY (id)
);

CREATE INDEX idx_matching_group
    ON Matches (MatchingGroup);

CREATE INDEX idx_user_id
    ON Matches (UserId);

CREATE INDEX idx_send_to
    ON Matches (SendTo);

CREATE INDEX idx_first_user_id
    ON DoNotMatch (FirstUserId);

CREATE INDEX idx_second_user_id
    ON DoNotMatch (SecondUserId);

-- Expected:
-- ES: 12 users all matched
-- USA: 4 users matched without warning
-- Group 1: Users matched to each other
-- Group 2: 1 User not matched
-- Worldwide: 2 users matched, 1 dropped
-- UK: 3 users matched, 1 dropped
-- Test: 1 User not matched
-- Test2: 1 User not matched
-- Test3: 1 User not matched
-- Test4: 1 User not matched
-- Test5: 2 users dropped


INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (1, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (2, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (3, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (4, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (5, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (6, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (7, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (8, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (9, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (10, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (11, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (12, 'USA', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (13, 'USA', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (14, 'Group 1', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (15, 'Worldwide', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (16, 'Worldwide', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (17, 'UK', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (18, 'UK', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (19, 'UK', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (20, 'UK', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (21, 'Worldwide', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (22, 'Group 1', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (23, 'Test', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (24, 'ES', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (25, 'USA', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (26, 'USA', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (27, 'Test2', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (28, 'Group 2', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (29, 'Test3', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (30, 'Test4', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (31, 'Test5', NULL, NULL);
INSERT INTO Matches (UserId, MatchingGroup, SendTo, ReceiveFrom)
VALUES (32, 'Test5', NULL, NULL);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (31, 32);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (15, 16);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (17, 18);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (17, 19);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (17, 20);
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (12, 13);