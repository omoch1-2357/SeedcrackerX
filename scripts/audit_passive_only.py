#!/usr/bin/env python3
"""Fail closed if SeedcrackerX source reintroduces outbound or discovery surface.

This intentionally audits the mod's own Java source. Normal Minecraft/Fabric
networking performed by the game and loader is outside this source tree and is
not affected.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "src" / "main" / "java"

FORBIDDEN: tuple[tuple[str, re.Pattern[str]], ...] = (
    ("java.net import", re.compile(r"^\s*import\s+java\.net(?:\.|;)", re.MULTILINE)),
    ("HTTP client", re.compile(r"\b(?:HttpClient|HttpRequest|HttpResponse|HttpURLConnection)\b")),
    ("serverbound packet", re.compile(r"^\s*import\s+net\.minecraft\.network\.protocol\..*Serverbound", re.MULTILINE)),
    ("Fabric networking API", re.compile(r"\b(?:ClientPlayNetworking|ServerPlayNetworking|PayloadTypeRegistry)\b")),
    ("custom payload type", re.compile(r"\b(?:CustomPacketPayload|ServerboundCustomPayloadPacket|BrandPayload)\b")),
    ("Minecraft connection send", re.compile(r"\bgetConnection\s*\(\s*\)\s*\.\s*send\s*\(")),
    ("direct Connection.send", re.compile(r"\bConnection\s*\.\s*send\s*\(")),
    ("session joinServer", re.compile(r"\.\s*joinServer\s*\(")),
    ("external URI opener", re.compile(r"\.\s*openUri\s*\(")),
    ("socket construction", re.compile(r"\bnew\s+(?:Socket|DatagramSocket)\s*\(")),
    ("process launch", re.compile(r"\b(?:ProcessBuilder|Runtime\.getRuntime\s*\(\s*\)\s*\.\s*exec)\b")),
    ("Fabric mod enumeration", re.compile(r"\.\s*(?:getAllMods|getEntrypointContainers)\s*\(")),
)


def main() -> int:
    violations: list[str] = []

    for path in sorted(SOURCE_ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        rel = path.relative_to(ROOT)
        for label, pattern in FORBIDDEN:
            for match in pattern.finditer(text):
                line = text.count("\n", 0, match.start()) + 1
                snippet = text.splitlines()[line - 1].strip()
                violations.append(f"{rel}:{line}: {label}: {snippet}")

    if violations:
        print("Passive-only audit failed:", file=sys.stderr)
        for violation in violations:
            print(f"  {violation}", file=sys.stderr)
        return 1

    print("Passive-only audit passed: no forbidden outbound or mod-discovery primitives found in src/main/java.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
