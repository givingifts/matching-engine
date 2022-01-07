DROP TABLE IF EXISTS Matches;
DROP TABLE IF EXISTS DoNotMatch;
DROP TABLE IF EXISTS MatchingGroup;
DROP TABLE IF EXISTS MatchingInstances;

CREATE TABLE Matches
(
    id                      bigint UNIQUE NOT NULL AUTO_INCREMENT,
    UserId                  bigint UNIQUE NOT NULL,
    OriginalMatchingGroup   varchar(10)   NOT NULL,
    CurrentMatchingGroup    varchar(10)                            DEFAULT NULL,
    Level                   bigint                                 DEFAULT 1,
    IsPremium               boolean                                DEFAULT FALSE,
    IsDropped               boolean                                DEFAULT FALSE,
    IsUpgradedToWorldwide   boolean                                DEFAULT FALSE,
    NoMatchBehaviour        ENUM ('DROP', 'INTERNATIONAL_WORLDWIDE', 'WORLDWIDE', 'INTERNATIONAL_DROP'),
    PremiumNoMatchBehaviour ENUM ('DROP', 'STANDARD', 'WORLDWIDE') DEFAULT NULL,
    SendTo                  bigint,
    ReceiveFrom             bigint,
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

CREATE TABLE MatchingGroup
(
    id     varchar(10) UNIQUE NOT NULL,
    Parent varchar(10),
    CONSTRAINT PK_MatchingGroup PRIMARY KEY (id)
);

CREATE TABLE MatchingInstances
(
    id            bigint UNIQUE NOT NULL AUTO_INCREMENT,
    done          BOOLEAN     default FALSE,
    matchingGroup varchar(10) default NULL,
    CONSTRAINT PK_MatchingInstances PRIMARY KEY (id)
);

CREATE INDEX idx_original_matching_group
    ON Matches (OriginalMatchingGroup);

CREATE INDEX idx_current_matching_group
    ON Matches (CurrentMatchingGroup);

CREATE INDEX idx_user_id
    ON Matches (UserId);

CREATE INDEX idx_send_to
    ON Matches (SendTo);

CREATE INDEX idx_first_user_id
    ON DoNotMatch (FirstUserId);

CREATE INDEX idx_second_user_id
    ON DoNotMatch (SecondUserId);

INSERT INTO MatchingGroup (id, Parent)
VALUES ('ES', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('USA', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Group 1', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Worldwide', null);
INSERT INTO MatchingGroup (id, Parent)
VALUES ('UK', 'Group 1');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Test', 'Group 1');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Group 2', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Group 3', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Test2', 'Group 2');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Test3', 'Group 3');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Test4', 'Group 3');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Test5', 'Worldwide');

INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (1, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (2, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (3, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (4, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (5, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (6, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (7, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (8, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (9, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (10, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (11, 'ES', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (12, 'USA', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (13, 'USA', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (14, 'Group 1', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (15, 'Worldwide', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (16, 'Worldwide', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (17, 'UK', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (18, 'UK', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (19, 'UK', NULL, NULL, 'INTERNATIONAL_WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (20, 'UK', NULL, NULL, true, 'INTERNATIONAL_DROP', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (21, 'Worldwide', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (22, 'Group 1', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (23, 'Test', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (24, 'ES', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'STANDARD');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (25, 'USA', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, IsPremium, NoMatchBehaviour,
                     PremiumNoMatchBehaviour)
VALUES (26, 'USA', NULL, NULL, true, 'INTERNATIONAL_WORLDWIDE', 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (27, 'Test2', NULL, NULL, 'INTERNATIONAL_DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (28, 'Group 2', NULL, NULL, 'INTERNATIONAL_DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (29, 'Test3', NULL, NULL, 'INTERNATIONAL_DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (30, 'Test4', NULL, NULL, 'WORLDWIDE');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (31, 'Test5', NULL, NULL, 'DROP');
INSERT INTO Matches (UserId, OriginalMatchingGroup, SendTo, ReceiveFrom, NoMatchBehaviour)
VALUES (32, 'Test5', NULL, NULL, 'DROP');
INSERT INTO DoNotMatch (FirstUserId, SecondUserId)
VALUES (31, 32)