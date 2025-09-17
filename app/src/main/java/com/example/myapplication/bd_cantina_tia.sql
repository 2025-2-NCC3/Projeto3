CREATE DATABASE cantina_tia;
USE cantina_tia;
CREATE TABLE users(
id int primary key auto_increment,
nome varchar(150) NOT NULL,
email varchar(150) NOT NULL,
senha varchar(20) NOT NULL
);

CREATE TABLE produtos(
id int primary key auto_increment,
nome varchar(30) NOT NULL,
descricao varchar(500) NOT NULL,
preco float,
estoque int,
categoria int,
caminhoImagem varchar(300)
);
