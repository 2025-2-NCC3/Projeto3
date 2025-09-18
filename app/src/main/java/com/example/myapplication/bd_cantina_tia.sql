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


CREATE TABLE pedidos(
id int primary key auto_increment,
id_usuario int,
preco float,
id_produto int,
status varchar(20),
constraint fk_id_usuario foreign key(id_usuario) references users(id),
constraint fk_preco foreign key(preco) references produtos(preco),
constraint fk_id_produto foreign key(id_produto) references produtos(id)
)

