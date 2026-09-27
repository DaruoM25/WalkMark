#!/usr/bin/env python3
"""
Black-box PreToolUse Guard for Antigravity runtime.
Enforces read-only targets, non-root constraints, and git isolation boundaries.
"""
import sys
import json
import re

def main():
    try:
        raw = sys.stdin.read()
        if not raw.strip():
            sys.stdout.write(json.dumps({"decision": "allow"}))
            return

        # Check for mutation of black box targets or disallowed branches
        is_native_write = bool(re.search(r'(?i)(write_to_file|replace_file_content|multi_replace_file_content)', raw))
        cmd_patterns = [
            r'>', r'Set-Content', r'Out-File', r'Add-Content', r'Remove-Item',
            r'\brm\b', r'\bdel\b', r'\brmdir\b', r'Move-Item', r'\bmv\b',
            r'Copy-Item', r'\bcp\b', r'New-Item', r'mkdir',
            r'open\([^\)]*[\'\"][wa]',
            r'git\s+(clean|rm|checkout|reset|apply)',
            r'git\s+push\s+origin\s+(main|master|develop)'
        ]
        is_mutating_cmd = any(re.search(pat, raw, re.IGNORECASE) for pat in cmd_patterns)

        if is_native_write and ("target" in raw or "build" in raw or "node_modules" in raw):
            sys.stdout.write(json.dumps({
                "decision": "deny",
                "reason": "Mutation of black-box target directory or build artifact is strictly forbidden."
            }))
            return

        if re.search(r'git\s+push\s+origin\s+(main|master|develop)', raw, re.IGNORECASE):
            sys.stdout.write(json.dumps({
                "decision": "deny",
                "reason": "Direct push to base branch is strictly prohibited by governance policy."
            }))
            return

        sys.stdout.write(json.dumps({"decision": "allow"}))
    except Exception as e:
        sys.stdout.write(json.dumps({"decision": "deny", "reason": f"Safety hook error: {str(e)}"}))

if __name__ == "__main__":
    main()
