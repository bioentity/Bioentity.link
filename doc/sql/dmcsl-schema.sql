# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.17)
# Database: dmcsl
# Generation Time: 2017-04-05 18:32:42 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table lexica
# ------------------------------------------------------------

DROP TABLE IF EXISTS `lexica`;

CREATE TABLE `lexica` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `lexicon_id` int(11) unsigned NOT NULL,
  `entity` varchar(20) NOT NULL DEFAULT '',
  `synonym` varchar(20) DEFAULT NULL,
  `database_id` varchar(20) DEFAULT NULL,
  `curator_notes` text,
  `ignore_during_markup` tinyint(1) DEFAULT NULL,
  `possibly_ambiguous` tinyint(1) DEFAULT NULL,
  `species` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lexicon_id` (`lexicon_id`),
  CONSTRAINT `lexica_ibfk_1` FOREIGN KEY (`lexicon_id`) REFERENCES `lexicon_sources` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table lexicon_sources
# ------------------------------------------------------------

DROP TABLE IF EXISTS `lexicon_sources`;

CREATE TABLE `lexicon_sources` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `source` varchar(30) NOT NULL DEFAULT '',
  `class` varchar(20) NOT NULL DEFAULT '',
  `date_last_updated` date NOT NULL,
  `version` varchar(10) DEFAULT '',
  `url_constructor` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table markup_tracker
# ------------------------------------------------------------

DROP TABLE IF EXISTS `markup_tracker`;

CREATE TABLE `markup_tracker` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `entity_id` int(11) unsigned DEFAULT NULL,
  `publication_id` int(11) unsigned DEFAULT NULL,
  `location_start` int(11) DEFAULT NULL,
  `location_stop` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `entity_id` (`entity_id`),
  KEY `publication_id` (`publication_id`),
  CONSTRAINT `markup_tracker_ibfk_1` FOREIGN KEY (`entity_id`) REFERENCES `lexica` (`id`),
  CONSTRAINT `markup_tracker_ibfk_2` FOREIGN KEY (`publication_id`) REFERENCES `publication` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table publication
# ------------------------------------------------------------

DROP TABLE IF EXISTS `publication`;

CREATE TABLE `publication` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(30) DEFAULT NULL,
  `journal` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
