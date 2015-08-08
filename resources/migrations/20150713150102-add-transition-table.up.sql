CREATE TABLE IF NOT EXISTS `transition` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `descr` varchar(64) NOT NULL,
  `lon` double NOT NULL,
  `lat` double NOT NULL,
  `acc` int(11) NOT NULL,
  `tst` int(10) unsigned NOT NULL,
  `wtst` int(10) unsigned NOT NULL,
  `event` char(5) NOT NULL,
  `tid` char(5) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `tst` (`tst`),
  KEY `event` (`event`),
  KEY `tid` (`tid`),
  KEY `waypoint` (`descr`,`wtst`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
