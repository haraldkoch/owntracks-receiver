CREATE TABLE IF NOT EXISTS `waypoint` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `descr` varchar(64) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `alt` int(11) NOT NULL,
  `rad` int(11) NOT NULL,
  `tst` int(10) unsigned NOT NULL,
  `tid` char(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `waypoint` (`descr`,`tst`),
  KEY `descr` (`descr`),
  KEY `tst` (`tst`),
  KEY `tid` (`tid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
