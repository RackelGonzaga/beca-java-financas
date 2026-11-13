# Beca Java Finanças

API para gerenciamento de finanças pessoais, desenvolvida como parte do desafio Beca Java 2026.

O projeto foi organizado em dois microsserviços: um responsável pelo gerenciamento de usuários e outro pelas movimentações financeiras.

## Tecnologias utilizadas

- Java 25
- Spring Boot 4
- Spring Data JPA
- Spring Security
- PostgreSQL 17
- Maven
- Docker Compose
- Swagger / OpenAPI
- JUnit e Mockito
- MockAPI (integração de saldo)

## Organização do projeto

```text
beca-java-financas/
├── ms1-users/
│   └── Serviço de usuários
├── ms2-transactions/
│   └── Serviço de transações
├── docker-compose.yml
├── .env.example
└── README.md
```

### MS1 — Usuários

Responsável pelo cadastro, autenticação, consulta, atualização e exclusão de usuários, além da importação de usuários por arquivo CSV.

### MS2 — Transações

Responsável pelo registro e consulta individual de transações, validação das operações, comunicação com o MS1 e integração com a MockAPI para consultar e atualizar saldos.

## Funcionalidades implementadas

### Usuários (MS1)

- Cadastro de usuários.
- Consulta de todos os usuários e consulta por identificador.
- Atualização e exclusão de usuários.
- Autenticação de usuários.
- Proteção de senhas antes do armazenamento.
- Verificação de e-mails duplicados.
- Importação de usuários por CSV, com validação dos registros e identificação de erros por linha.

### Transações (MS2)

- Registro de depósitos, saques, transferências e compras.
- Validação dos dados enviados.
- Consulta de uma transação pelo identificador.
- Verificação do usuário por meio da comunicação com o MS1.
- Consulta e atualização de saldo pela MockAPI.
- Verificação de saldo e limites antes de operações de saída.
- Registro de transações aprovadas ou rejeitadas.

## Documentação da API

Com os serviços em execução, a documentação Swagger fica disponível em:

- MS1: http://localhost:8080/swagger-ui/index.html
- MS2: http://localhost:8081/swagger-ui/index.html

## Principais endpoints

### MS1 — Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/users` | Cadastrar usuário |
| GET | `/users` | Listar usuários |
| GET | `/users/{id}` | Consultar usuário por ID |
| PUT | `/users/{id}` | Atualizar usuário |
| DELETE | `/users/{id}` | Excluir usuário |
| POST | `/users/authenticate` | Autenticar usuário |
| POST | `/users/import` | Importar usuários por CSV |

### MS2 — Transações

As operações de transações exigem autenticação HTTP Basic.

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/transactions` | Registrar transação |
| GET | `/api/transactions/{id}` | Consultar transação por ID |
| GET | `/api/transactions/balance` | Consultar saldo do usuário autenticado |

## Requisitos para execução

- Java 25.
- Maven.
- Docker com Docker Compose.
- Uma URL válida da MockAPI configurada para o projeto.
- Credenciais de um usuário cadastrado no MS1 para a comunicação entre os serviços.

## Configuração do ambiente

1. Na raiz do projeto, crie seu arquivo de ambiente a partir do exemplo:

   ```bash
   cp .env.example .env
   ```

   No Windows PowerShell, também é possível usar:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Abra o arquivo `.env` e preencha os valores necessários:

   ```env
   POSTGRES_DB=financas
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=defina_uma_senha
   POSTGRES_PORT=5433

   MOCKAPI_BASE_URL=URL_DA_SUA_MOCKAPI
   MS1_SERVICE_EMAIL=EMAIL_DE_UM_USUARIO_CADASTRADO_NO_MS1
   MS1_SERVICE_PASSWORD=SENHA_DESSE_USUARIO
   ```

   A URL da MockAPI e as credenciais devem corresponder aos recursos configurados para o ambiente. Não compartilhe o arquivo `.env` nem envie credenciais para o repositório.

## Executando com Docker Compose

Na raiz do projeto, execute:

```bash
docker compose up --build
```

Para verificar os containers:

```bash
docker compose ps
```

Para encerrar os serviços:

```bash
docker compose down
```

O Docker Compose configura o PostgreSQL e inicia os serviços MS1 e MS2. O MS1 fica disponível na porta 8080 e o MS2 na porta 8081. O PostgreSQL utiliza a porta 5433 no computador local.

## Executando os testes

Para executar os testes do MS1:

```bash
cd ms1-users
mvn test
```

Para executar os testes do MS2:

```bash
cd ms2-transactions
mvn test
```

## Funcionalidades pendentes

As funcionalidades abaixo ainda precisam ser desenvolvidas ou concluídas para atender integralmente à proposta do desafio:

- Integração com a BrasilAPI para consulta de cotações e conversão de moedas.
- Exibição das informações de câmbio previstas no desafio.
- Análise de despesas por categoria, dia e mês.
- Exportação de dados para arquivo CSV.
- Disponibilização pela API da listagem e dos filtros de transações.
- Validação completa, em execução, da integração com a MockAPI e do funcionamento conjunto dos serviços pelo Docker Compose.

## Status dos testes

O repositório contém testes automatizados para partes dos dois serviços. A existência dos testes não significa, por si só, que todos foram executados com sucesso no ambiente atual. A execução deve ser realizada conforme as instruções acima.

## Organização do desenvolvimento

O desenvolvimento foi acompanhado pelo GitHub, com branches separadas para organizar as etapas e as funcionalidades do projeto.
