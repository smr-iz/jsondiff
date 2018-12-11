# --- !Ups

CREATE TABLE `JsonData` (
  `id` varchar(50) NOT NULL,
  `left` text DEFAULT NULL,
  `right` text DEFAULT NULL,
  `cDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `uDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs
