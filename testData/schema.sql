CREATE
USER 'matching'@'%' IDENTIFIED BY 'matching';
GRANT ALL
ON *.* TO 'matching'@'%';
FLUSH
PRIVILEGES;

DROP TABLE IF EXISTS Matches;
DROP TABLE IF EXISTS MatchingGroup;
DROP TABLE IF EXISTS MatchingInstances;

CREATE TABLE Matches
(
    id                    bigint UNIQUE NOT NULL AUTO_INCREMENT,
    UserId                bigint UNIQUE NOT NULL,
    OriginalMatchingGroup varchar(10)   NOT NULL,
    CurrentMatchingGroup  varchar(10) DEFAULT NULL,
    IsPremium             boolean     DEFAULT FALSE,
    PremiumBehaviour      ENUM('WORLDWIDE', 'NONPREMIUM', 'DROP') DEFAULT NULL,
    SendTo                bigint,
    ReceiveFrom           bigint,
    CONSTRAINT PK_Matches PRIMARY KEY (id),
    CHECK (SendTo <> UserId),
    CHECK (ReceiveFrom <> UserId)
);

CREATE TABLE MatchingGroup
(
    id     varchar(10) UNIQUE NOT NULL,
    Parent varchar(10),
    CONSTRAINT PK_MatchingGroup PRIMARY KEY (id)
);

CREATE TABLE MatchingInstances
(
    id             bigint UNIQUE NOT NULL AUTO_INCREMENT,
    done           BOOLEAN default FALSE,
    matchingGroups text    default NULL,
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

INSERT INTO MatchingGroup (id, Parent)
VALUES ('ES', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('USA', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Europe', 'Worldwide');
INSERT INTO MatchingGroup (id, Parent)
VALUES ('Worldwide', null);
INSERT INTO MatchingGroup (id, Parent)
VALUES ('UK', 'Europe');

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
VALUES (14, 'Europe', NULL, NULL);
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
