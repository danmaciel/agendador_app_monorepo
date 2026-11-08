# Agendador de Serviços

Projeto demonstrativo de backend para gerenciamento de **usuários, serviços e agendamentos**, com autenticação e regras de negócio organizadas por funcionalidades.

## Visão técnica

- **Stack:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA e Flyway.
- **Persistência:** H2 no perfil de desenvolvimento e PostgreSQL no perfil `prod`.
- **Segurança:** autenticação JWT e refresh token, com configurações de produção exigidas via variáveis de ambiente. O cadastro é público; consulta/listagem de usuários exigem autenticação. A consulta de perfil por ID permite apenas o **próprio usuário ou administradores**; os demais endpoints continuam sujeitos às políticas descritas no código.
- **Organização:** `feature/*/application/usecase`, `domain` e `infrastructure/controller`.
- **Qualidade:** testes JUnit/MockMvc e integração com H2 e PostgreSQL 15 via Testcontainers; GitHub Actions executa Maven em jobs separados.

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

A pipeline [Backend CI](.github/workflows/ci.yml) executa `mvn -B verify` e, em outro job com Docker, `mvn -B -Ppostgres-it verify`, incluindo testes de concorrência reais com PostgreSQL 15. Em ambiente local com Docker disponível, você também pode executar `mvn -Ppostgres-it verify`.

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
- A consulta de usuário por ID exige autenticação e usa autorização por proprietário ou administrador, verificada antes da busca do perfil. O restante dos contratos de autorização exige revisão separada antes de qualquer exposição pública.
- Novos refresh tokens são segredos opacos de 256 bits, persistidos apenas como **fingerprint SHA-256**. Tokens legados em UUIDv4 ainda são aceitos temporariamente até sua rotação. A renovação usa bloqueio pessimista por usuário e token, com cenários de concorrência testados em PostgreSQL. **Ainda não há** família de tokens, detecção robusta de replay, política de múltiplos dispositivos ou garantia para todas as interleavings e falhas. Ver [plano de transição](docs/REFRESH_TOKEN_TRANSITION.md).
- O perfil padrão utiliza credenciais de administrador e JWT de desenvolvimento: execute apenas em ambiente descartável e isolado. Não publique a porta HTTP como serviço externo.
- Os endpoints de listagem, promoção a administrador e desativação de usuários são restritos a `ROLE_ADMIN`, com regressões MockMvc para usuários comuns e administradores. O cadastro continua público no laboratório.

## Escopo

Projeto de estudo/demonstração de engenharia de software. Não há declaração de disponibilidade ou implantação em produção.
