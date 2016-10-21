# Microservico Correios

Feito com:
+ Java Spark API para requisições web.
+ Utilizamos lambda do Java 8.
+ JPA e Spring Data para persistência.
+ Migration com FlyWay para versionar o banco.
+ URL Rest amigável com retorno JSON.
+ Versionamento por pacotes.

# Executando o projeto

1. Criar no Postgres a base de dados chamada 'bd_correios_desenv' e um schema chamado 'main'. Pode-se alterar configurações de base por ambiente nos "arquivos envs".
2. Para subir o projeto, basta rodar a classe "Main" como uma aplicação java (normal ou debug). Internamente, o Spark API iniciará um Jetty standalone.

# Resposta da API

O retorno será um JSON onde o conteúdo é um único objeto "Resposta" com os dados para cada ação. Assim, o Angular vai esperar sempre o mesmo padrão de retorno. Para saber o que uma resposta quer dizer, deve-se levar em consideração o status HTTP respondido:

+ 200: sucesso.
+ 500: erro interno não esperado.
+ 403: validações e restrições de regras.
+ 401: sem permissão.

O JSON de retorno conterá dois campos:

+ lista: array com a listagem de dados quaisquer. Pode ser um único objeto interno, em caso de retorno de cadastros.
+ mensagens: array de mensagens. Será sempre objetos com dois campos: chave e valor. Uma ação poderá desencadear várias mensagens ao mesmo tempo.


Exemplo de retorno de validação de dados:

status http: 403 
```
{
	"lista":null,
	"mensagens":[
		{
			"chave":"mensagem",
			"valor":"Dados insuficientes. Verifique o campo login"
		}
	]
}
```

Exemplo de retorno de pemissão negada:

status http: 401
```
{
	"lista":null,
	"mensagens":[
		{
			"chave":"mensagem",
			"valor":"Permissão negada! Faça o login e tente novamente."
		}
	]
}
```

Exemplo de retorno de cadastro de pessoa com sucesso:

status http: 200
```
{
	"lista":[
		{"dataCriacao":1455032318503,"id":1,"chave":"f123","nome":"Pessoa 1"}
	],
	"mensagens":[
		{
			"chave":"mensagem",
			"valor":"sucesso"
		}
	]
}
```

Exemplo de retorno de listagem com 2 pessoas:

status http: 200
```
{
	"lista":[
		{"dataCriacao":null,"id":1,"chave":"f123","nome":"Pessoa 1"},
		{"dataCriacao":null,"id":2,"chave":"f456","nome":"Pessoa 2"}
	],
	"mensagens":[
		{
			"chave":"mensagem",
			"valor":"sucesso"
		}
	]
}
```

# Arquitetura

Existem quatro pacotes básicos centrais:
+ endpoint: são as configurações de acessos web REST.
+ beans: as classes POJOs com mapeamentos JPA.
+ dominio: classes de domínio. Contem algumas validações e validações.
+ infra: classes utilitárias gerais.

Usamos somente dois verbos HTTP:
+ GET: para buscar dados, seja unico por id ou listagens.
+ POST: qualquer alterações de dados.
