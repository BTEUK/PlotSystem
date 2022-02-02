
CREATE TABLE IF NOT EXISTS player_data
(
	uuid		CHAR(36)		NOT NULL, 
	name		VARCHAR(20)		NOT NULL,
	b_points	INT				NOT NULL,
	last_join	BIGINT			NOT NULL,
	role		VARCHAR(20)		NOT NULL,
	last_submit	BIGINT			NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS tutorial_data
(
	uuid 		CHAR(36)		NOT NULL,
	type		INT				NOT NULL,
	stage		INT				NOT NULL,
	first_time	TINYINT(1)		NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS plot_data
(
	id			INT				AUTO_INCREMENT,
	uuid		CHAR(36)		NOT NULL,
	status		VARCHAR(20)		NOT NULL,
	last_enter	BIGINT			NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS deny_data
(
	id			INT				AUTO_INCREMENT,
	plot		INT				NOT NULL,
	attempt		INT				NOT NULL,
	uuid		CHAR(36)		NOT NULL,
	reviewer	CHAR(36)		NOT NULL,
	feedback	INT				NOT NULL,
	type		VARCHAR(20)		NOT NULL,
	time		BIGINT			NOT NULL,
	PRIMARY KEY (id)
);	

CREATE TABLE IF NOT EXISTS accept_data
(
	plot		INT				NOT NULL,
	uuid		CHAR(36)		NOT NULL,
	reviewer	CHAR(36)		NOT NULL,
	feedback	INT				NOT NULL,
	size		INT				NOT NULL,
	accuracy	INT				NOT NULL,
	quality		INT				NOT NULL,
	points		INT				NOT NULL,
	time		BIGINT			NOT NULL,
	PRIMARY KEY (plot)
);
	

CREATE TABLE IF NOT EXISTS area_data
(
	id			INT				AUTO_INCREMENT,
	name		VARCHAR(64)		NOT NULL,
	type		VARCHAR(32)		NOT NULL,
	status		VARCHAR(32)		NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS submit_data
(
	id			INT				NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS book_data
(
	id			INT				AUTO_INCREMENT,
	book		INT				NOT NULL,
	page		INT				NOT NULL,
	text		TEXT			NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS message_data
(
	id			INT				AUTO_INCREMENT,
	uuid		CHAR(36)		NOT NULL,
	plot		INT				NOT NULL,
	type		VARCHAR(20)		NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS points_data
(
	id			INT				AUTO_INCREMENT,
	plot		INT				NOT NULL,
	point		INT				NOT NULL,
	x			INT				NOT NULL,
	z			INT				NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS hologram_data
(
	name			VARCHAR(16)			NOT NULL,
	world			VARCHAR(32)			NOT NULL,
	x			DOUBLE				NOT NULL,
	y			DOUBLE				NOT NULL,
	z			DOUBLE				NOT NULL,
	visible			TINYINT(1)			NOT NULL,
	PRIMARY KEY(name)
);

CREATE TABLE IF NOT EXISTS hologram_text
(
	id			INT				AUTO_INCREMENT,
	hologram_name		VARCHAR(16)			NOT NULL,
	line			INT				NOT NULL,
	text			TEXT				NOT NULL,
	PRIMARY KEY(id)
);
