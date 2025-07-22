# Claude AI Guide Directory

This directory contains documentation and guides specifically for Claude AI to understand and work effectively with the Liquibase Test Harness.

## Contents

- **[SNOWFLAKE_TESTING_STRATEGY.md](SNOWFLAKE_TESTING_STRATEGY.md)** - Comprehensive testing strategy for Snowflake including the critical init.xml cleanup pattern
- **[TESTING_CHEAT_SHEET.md](TESTING_CHEAT_SHEET.md)** - Quick reference for Maven test commands and syntax

## Quick Start for Claude

When starting a new session, refer to:
1. `/CLAUDE.md` at the root - Your main knowledge base
2. This directory for specific implementation strategies

## Key Concepts to Remember

1. **Persistent Database Problem**: Cloud databases like Snowflake retain state between test runs
2. **Init Cleanup Pattern**: Always run init.xml with `runAlways="true"` before tests
3. **Self-Contained Tests**: Each test must create its own required objects
4. **Three-File Pattern**: changelog, expectedSql, and expectedSnapshot are all required