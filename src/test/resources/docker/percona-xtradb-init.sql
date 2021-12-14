GRANT SUPER ON *.* TO 'lbuser'@'%';
GRANT ALL PRIVILEGES ON lbcat.* TO 'lbuser'@'%' WITH GRANT OPTION;
GRANT PROCESS ON *.* TO 'lbuser'@'%';
SET GLOBAL pxc_strict_mode=DISABLED;
SET GLOBAL log_bin_trust_function_creators = 1;
FLUSH PRIVILEGES;

DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `first_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
                           `last_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
                           `email` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
                           `birthdate` date NOT NULL,
                           `added` timestamp NOT NULL DEFAULT current_timestamp(),
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `authors` VALUES ('1','Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
                             ('2','Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
                             ('3','Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
                             ('4','Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
                             ('5','Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00');

DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
                         `id` int(11) NOT NULL AUTO_INCREMENT,
                         `author_id` int(11) NOT NULL,
                         `title` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
                         `description` varchar(500) COLLATE utf8_unicode_ci NOT NULL,
                         `content` text COLLATE utf8_unicode_ci NOT NULL,
                         `inserted_date` date,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `posts` VALUES ('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
                           ('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
                           ('3','3','illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
                           ('4','4','itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
                           ('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');