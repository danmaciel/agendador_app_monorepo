# Agendador de Serviços

Projeto demonstrativo de backend para gerenciamento de **usuários, serviços e agendamentos**, com autenticação e regras de negócio organizadas por funcionalidades.

## Visão técnica

- **Stack:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA e Flyway.
- **Persistência:** H2 no perfil de desenvolvimento e PostgreSQL no perfil `prod`.
- **Segurança:** autenticação JWT e refresh token, com configurações de produção exigidas via variáveis de ambiente.
- **Organização:** `feature/*/application/usecase`, `domain` e `infrastructure/controller`.
- **Qualidade:** testes automatizados Maven via GitHub Actions.

### Principais casos de uso

- Cadastro e administração de usuários e serviços.
- Criação, aprovação, atualização e cancelamento de agendamentos.
- Sugestões de datas, histórico e consultas de agendamentos.
- Login, renovação de token e controle de acesso.

## Estrutura

```text
backend/
├── src/main/java/.../
│   ├── feature/
│   │   ├── agendamento/
│   │   ├── auth/
│   │   ├── servico/
│   │   └── usuario/
│   └── config/
├── src/main/resources/
│   └── db/migration/      # Migrações Flyway
└── src/test/               # Testes automatizados
```

## Executar testes

Requer **JDK 21** e Maven.

```bash
cd backend
mvn verify
```

A pipeline [Backend CI](.github/workflows/ci.yml) executa `mvn -B verify` em pull requests e pushes na `main`.

## Execução local

O perfil padrão configura H2 em memória para desenvolvimento:

```bash
cd backend
mvn spring-boot:run
```

Para PostgreSQL, configure as variáveis de ambiente exigidas em `backend/src/main/resources/application-prod.properties` antes de iniciar com `SPRING_PROFILES_ACTIVE=prod`. O `docker-compose.yml` inicia apenas o banco; a API está comentada no Compose original.

Leia [SECURITY_SETUP.md](SECURITY_SETUP.md) e use [`.env.example`](.env.example) como referência. **Não publique credenciais reais.**

## Decisões e limitações

- Separação por funcionalidade e casos de uso ajuda a manter regras de negócio testáveis.
- Flyway mantém o esquema versionado; ambientes devem ser configurados explicitamente.
- O Docker Compose disponível não constitui uma implantação completa da aplicação.
- Os testes automatizados validam os cenários implementados, mas não substituem testes ponta a ponta e uma revisão de segurança de produção.

## Escopo

Projeto de estudo/demonstração de engenharia de software. Não há declaração de disponibilidade ou implantação em produção.
