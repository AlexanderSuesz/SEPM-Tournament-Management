-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE FROM horse WHERE id < 0;
DELETE  FROM breed WHERE id < 0;

INSERT INTO breed (id, name)
VALUES
    (-1, 'Andalusian'),
    (-2, 'Appaloosa'),
    (-3, 'Arabian'),
    (-4, 'Belgian Draft'),
    (-5, 'Connemara Pony'),
    (-6, 'Dartmoor Pony'),
    (-7, 'Friesian'),
    (-8, 'Haflinger'),
    (-9, 'Hanoverian'),
    (-10, 'Icelandic Horse'),
    (-11, 'Lipizzaner'),
    (-12, 'Oldenburg'),
    (-13, 'Paint Horse'),
    (-14, 'Quarter Horse'),
    (-15, 'Shetland Pony'),
    (-16, 'Tinker'),
    (-17, 'Trakehner'),
    (-18, 'Warmblood'),
    (-19, 'Welsh Cob'),
    (-20, 'Welsh Pony');

-- Füge Pferdedaten hinzu
INSERT INTO horse (id, name, sex, date_of_birth, height, weight, breed_id)
VALUES
    (-1, 'Wendy', 'FEMALE', '2019-08-05', 1.40, 380, -15),
    (-2, 'Hugo', 'MALE', '2020-02-20', 1.20, 320, -20),
    (-3, 'Bella', 'FEMALE', '2005-04-08', 1.45, 550, -1),
    (-4, 'Thunder', 'MALE', '2008-07-15', 1.60, 600, -2),
    (-5, 'Luna', 'FEMALE', '2012-11-22', 1.65, 650, -3),
    (-6, 'Apollo', 'MALE', '2003-09-03', 1.52, 500, -4),
    (-7, 'Sophie', 'FEMALE', '2010-06-18', 1.70, 700, -5),
    (-8, 'Max', 'MALE', '2006-03-27', 1.55, 580, -6),
    (-9, 'Bella', 'FEMALE', '2002-08-09', 1.48, 520, -7),
    (-10, 'Rocky', 'MALE', '2013-05-05', 1.55, 620, -8),
    (-11, 'Daisy', 'FEMALE', '2007-02-12', 1.30, 350, -9),
    (-12, 'Charlie', 'MALE', '2011-09-21', 1.68, 680, -10),
    (-13, 'Ruby', 'FEMALE', '2004-07-30', 1.10, 280, -11),
    (-14, 'Duke', 'MALE', '2009-03-14', 1.75, 800, -12),
    (-15, 'Rosie', 'FEMALE', '2001-12-08', 1.57, 590, -13),
    (-16, 'Jack', 'MALE', '2014-10-25', 1.52, 560, -14),
    (-17, 'Lilly', 'FEMALE', '2008-06-03', 1.40, 400, -15),
    (-18, 'Sam', 'MALE', '2010-01-17', 1.65, 650, -16),
    (-19, 'Misty', 'FEMALE', '2005-11-09', 1.25, 320, -17),
    (-20, 'Max', 'MALE', '2012-08-29', 1.72, 670, -18),
    (-21, 'Bella', 'FEMALE', '2003-07-06', 1.50, 580, -19),
    (-22, 'Rocky', 'MALE', '2007-04-12', 1.40, 450, -1),
    (-23, 'Misty', 'FEMALE', '2015-03-12', 1.32, 360, -7),
    (-24, 'Rocky', 'MALE', '2018-08-19', 1.42, 480, -6),
    (-25, 'Lucky', 'MALE', '2019-05-25', 1.58, 620, -5),
    (-26, 'Daisy', 'FEMALE', '2017-12-01', 1.28, 340, -9),
    (-27, 'Buddy', 'MALE', '2016-09-14', 1.68, 700, -10),
    (-28, 'Molly', 'FEMALE', '2014-04-03', 1.55, 580, -13),
    (-29, 'Cody', 'MALE', '2019-11-30', 1.45, 550, -2),
    (-30, 'Rosie', 'FEMALE', '2016-06-28', 1.52, 520, -14),
    (-31, 'Leo', 'MALE', '2017-03-05', 1.70, 720, -8),
    (-32, 'Luna', 'FEMALE', '2018-10-10', 1.62, 670, -19);

