# Security Policy

Report vulnerabilities privately through GitHub Security Advisories.

Do not open public issues for:

- command injection
- path traversal in reports
- malicious SARIF or HTML report generation
- sensitive data leakage in diagnostics
- unsafe source traversal outside the project root

Security-sensitive implementation rules:

- never include environment variables in reports by default
- redact tokens and secrets from command lines
- sanitize HTML report output
- avoid following symlinks outside the project root unless enabled
- keep source rewriting opt-in through explicit `--apply`
