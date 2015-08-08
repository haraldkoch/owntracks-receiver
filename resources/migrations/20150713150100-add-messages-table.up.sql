CREATE TABLE IF NOT EXISTS `messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `topic` varchar(255) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`id`),
  KEY `time` (`time`),
  KEY `topic` (`topic`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
