# Passive local-only variant

This branch keeps SeedcrackerX's client-side structure discovery and seed-cracking logic while removing active outbound behavior implemented by SeedcrackerX itself.

The goal is compatibility with servers where extra client-mod metadata or active probing may trigger expensive server-side processing. This variant therefore avoids SeedcrackerX-specific outbound communication while preserving the normal Minecraft/Fabric connection needed to play.

## Removed or disabled

- Startup fetch of the public seed database.
- Seed submission to the external database.
- Authentication request used by the database submission path.
- Database command and database-related configuration UI.
- Anti-xray block-update probing that sent `ServerboundPlayerActionPacket` packets.

## Retained

- Passive processing of data already delivered to the Minecraft client.
- Structure finders and loaded-chunk rescanning.
- Local seed-cracking logic and local configuration/state.
- Normal Minecraft/Fabric networking required to connect to and play on a server.

## Enforcement

`scripts/audit_passive_only.py` scans the mod's Java source for outbound-I/O primitives that are not allowed in this variant. GitHub Actions runs the audit before compiling the mod.

Every branch push and pull request runs the passive-only audit before the Java 25 Gradle build, and non-default branches upload the resulting JAR files as a workflow artifact.

The audit covers SeedcrackerX's own source tree. It does not and cannot remove the ordinary networking performed by Minecraft, Fabric Loader, or unrelated installed mods.
