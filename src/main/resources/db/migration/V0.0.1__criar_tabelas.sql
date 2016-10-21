CREATE TABLE main.hibernate_sequences
(
  sequence_name varchar(255) NOT NULL,
  sequence_next_hi_value bigint NOT NULL
);

CREATE TABLE main.uf
(
  id bigint NOT NULL,
  sigla character(2),
  nome varchar,
  datacriacao timestamp,
  dataatualizacao timestamp,
  CONSTRAINT pk_id PRIMARY KEY (id)
);

CREATE TABLE main.cidade
(
  id bigint NOT NULL,
  nome varchar,
  uf_id bigint,
  datacriacao timestamp,
  dataatualizacao timestamp,
  ibge double precision,
  CONSTRAINT pk_cidade_id PRIMARY KEY (id),
  CONSTRAINT pk_cidade_uf FOREIGN KEY (uf_id) REFERENCES main.uf (id)
);

CREATE TABLE main.bairro
(
  id bigint NOT NULL,
  cidade_id bigint,
  nome varchar,
  datacriacao timestamp,
  dataatualizacao timestamp,
  CONSTRAINT pk_bairro_id PRIMARY KEY (id),
  CONSTRAINT fk_bairro_cidade_id FOREIGN KEY (cidade_id) REFERENCES main.cidade (id)
);

CREATE TABLE main.logradouro
(
  id bigint NOT NULL,
  bairro_id bigint,
  nome varchar,
  complemento varchar,
  datacriacao timestamp,
  dataatualizacao timestamp,
  cidade_id bigint,
  cep integer,
  CONSTRAINT pk_logradouro_id PRIMARY KEY (id),
  CONSTRAINT fk_logradouro_bairro FOREIGN KEY (bairro_id) REFERENCES main.bairro (id),
  CONSTRAINT fk_logradouro_cidade FOREIGN KEY (cidade_id) REFERENCES main.cidade (id)
);

CREATE INDEX bairro_nome_idx
  ON main.bairro
  USING btree
  (nome COLLATE pg_catalog."default");

CREATE INDEX cidade_nome_idx
  ON main.cidade
  USING btree
  (nome COLLATE pg_catalog."default");

CREATE INDEX uf_nome_idx
  ON main.uf
  USING btree
  (nome COLLATE pg_catalog."default");

