# Vehicle Management API - Tinnova Test

Esta é uma API RESTful desenvolvida para o desafio técnico da Tinnova. O objetivo principal é gerenciar veículos, permitindo operações de CRUD, consultas com filtros dinâmicos e integração com APIs externas de cotação de moedas, tudo protegido por autenticação baseada em perfis de acesso (JWT).

---

## 📺 Demonstração e Explicação

Assista aos vídeos abaixo para uma explicação detalhada da arquitetura, funcionalidades e execução do projeto:

| Vídeo 1 | Vídeo 2 | Vídeo 3 |
| :---: | :---: | :---: |
| [![Explicação 1](https://img.youtube.com/vi/HWCAXpDU3EE/0.jpg)](https://youtu.be/HWCAXpDU3EE) | [![Explicação 2](https://img.youtube.com/vi/Lzuob_fKSxk/0.jpg)](https://youtu.be/Lzuob_fKSxk) | [![Explicação 3](https://img.youtube.com/vi/DHKVqqbvHSQ/0.jpg)](https://youtu.be/DHKVqqbvHSQ) |
| [Assistir Parte 1](https://youtu.be/HWCAXpDU3EE) | [Assistir Parte 2](https://youtu.be/Lzuob_fKSxk) | [Assistir Parte 3](https://youtu.be/DHKVqqbvHSQ) |

---

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Java 21  
- **Framework:** Spring Boot  
- **Banco de Dados:** PostgreSQL
- **Cache:** Redis  
- **Segurança:** Spring Security + JWT
- **Integração Externa:** Spring Cloud OpenFeign  
- **Documentação:** OpenAPI / Swagger  
- **Infraestrutura:** Docker & Docker Compose  
- **Outros:** Lombok, Hibernate/JPA (com Specifications para filtros dinâmicos)  

---

## 📋 Pré-requisitos

Para rodar este projeto na sua máquina, você precisará ter instalado:

- Docker e Docker Compose (para subir o banco de dados e o cache)  
- Java 21 (JDK)  
- Maven (Opcional, pois o projeto conta com o mvnw wrapper)  

---

## 🛠️ Como executar a aplicação

Siga os passos abaixo para subir a infraestrutura e rodar a API localmente.

### 1. Subir a Infraestrutura (PostgreSQL e Redis)

Na raiz do projeto (onde o arquivo docker-compose.yml está localizado), execute o comando abaixo no terminal para iniciar os containers em segundo plano:

```bash
docker-compose up -d
````

Isso iniciará o PostgreSQL na porta **5432** e o Redis na porta **6379**.

---

### 2. Iniciar a Aplicação Spring Boot

Você pode rodar a aplicação diretamente pela sua IDE (executando a classe `VehicleapiApplication`) ou utilizar o Maven Wrapper pelo terminal:

**No Linux/macOS:**

```bash
./mvnw spring-boot:run
```

**No Windows:**

```bash
mvnw.cmd spring-boot:run
```

A aplicação estará disponível na porta **8080**.

---

## 🔐 Autenticação e Usuários Padrão

A API utiliza Spring Security com JWT. Para facilitar os testes, a aplicação conta com um Data Seeder que cria automaticamente dois usuários no banco de dados assim que a aplicação sobe:

| Perfil | Username | Password | Permissões                                   |
| ------ | -------- | -------- | -------------------------------------------- |
| ADMIN  | admin    | admin123 | Acesso total (GET, POST, PUT, PATCH, DELETE) |
| USER   | user     | user123  | Acesso restrito apenas a consultas (GET)     |

---

## 🧪 Como testar a API (Swagger)

A API está totalmente documentada com o Swagger/OpenAPI. É a forma mais fácil e interativa de testar todos os endpoints.

Com a aplicação rodando, acesse no seu navegador:

👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Passos:

1. Vá até o endpoint **POST /auth/login**
2. Clique em **Try it out** e envie o corpo da requisição com as credenciais de um dos usuários (ex: admin / admin123).

Exemplo de payload:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

3. Copie o token retornado na resposta
4. Role a página até o topo, clique no botão **Authorize** (ícone de cadeado) e cole o seu token no formato:

```
Bearer SEU_TOKEN_AQUI
```

Agora você pode testar todos os endpoints de **/veiculos** com as devidas permissões!

---

## ⚙️ Testes Automatizados (Unitários e de Integração)

O projeto possui uma suíte robusta de testes automatizados para garantir a qualidade, segurança e a resiliência da aplicação, cobrindo 100% das regras de negócio mapeadas:

- **Testes Unitários**: Focados nas regras de negócio (camada de Service), utilizando JUnit 5 e Mockito para conversões de moeda e validações de placa.
- **Testes de Integração (Banco de Dados e E2E):** Utiliza Testcontainers para subir um container efêmero do PostgreSQL real, homologando as Specifications, contagens customizadas e contratos da Controller via MockMvc sem sujar o banco de dados local.
- **Testes de Resiliência Externa:** Utiliza WireMock para simular as respostas da API externa de cotação de moedas, testando tanto o "Caminho Feliz" (Status 200) quanto o mecanismo de Fallback em caso de falha (Status 500).

**Como executar a suíte de testes**

**No Linux/macOS:**

```bash
./mvnw verify
```

**No Windows:**

```bash
mvnw.cmd verify
```
O comando verify garante a execução tanto da fase test para unitários quanto da fase integration-test do Failsafe Plugin.

---
## ✅ Requisitos Implementados

* [x] CRUD Completo: Cadastro, atualização (PUT/PATCH), deleção lógica (Soft Delete) e consulta de veículos
* [x] Filtros e Paginação: Consultas dinâmicas utilizando JpaSpecificationExecutor (marca, ano, cor, range de preço)
* [x] Regra de Negócio (Placa Única): Validação para impedir o cadastro de veículos com placas duplicadas
* [x] Integração de Moedas: Preço recebido em BRL (Real), mas convertido e armazenado no banco em USD (Dólar)
* [x] Resiliência e Cache: Consumo da API pública de dólar via OpenFeign, com fallback automático para uma segunda API em caso de falha na primária, e cache no Redis para evitar sobrecarga de requisições externas
* [x] Relatórios: Endpoint de agrupamento e contagem de veículos por marca
* [x] Segurança: Autenticação JWT validando perfis (ADMIN vs USER)
* [x] Infraestrutura Automática: docker-compose e Data Seeder prontos para facilitar a avaliação
