#!/usr/bin/env python3
import argparse
import re
from typing import List

def format_code(code: str) -> str:
    lines = code.splitlines()
    out_lines: List[str] = []
    block_stack: List[int] = []   # each entry is the "base" indent for a block (where BEGIN/RETURN/END sit)

    def current_body_indent() -> int:
        return (block_stack[-1] + 1) if block_stack else 0

    prefix_begin_re = re.compile(r'^(.*\S)\s+BEGIN\s*$')

    for raw in lines:
        stripped = raw.strip()
        # preserve empty lines
        if stripped == '':
            out_lines.append('')
            continue

        # "prefix BEGIN" on the same line, e.g. "MOVE TEMP 2 BEGIN"
        m = prefix_begin_re.match(stripped)
        if m:
            prefix = m.group(1)
            body_indent = current_body_indent()
            out_lines.append('\t' * body_indent + prefix)
            base = body_indent + 1
            out_lines.append('\t' * base + 'BEGIN')
            block_stack.append(base)
            continue

        # standalone BEGIN -> place it one tab deeper than current body, open block
        if stripped == 'BEGIN':
            body_indent = current_body_indent()
            base = body_indent + 1
            out_lines.append('\t' * base + 'BEGIN')
            block_stack.append(base)
            continue

        # RETURN -> aligned with the current block's BEGIN (top of stack)
        if stripped == 'RETURN':
            base = block_stack[-1] if block_stack else current_body_indent()
            out_lines.append('\t' * base + 'RETURN')
            # the following return-expression lines will naturally be printed at base+1
            continue

        # END -> close current block and print END aligned with its BEGIN
        if stripped == 'END':
            base = block_stack.pop() if block_stack else current_body_indent()
            out_lines.append('\t' * base + 'END')
            continue

        # normal line -> print at current body indent
        body_indent = current_body_indent()
        out_lines.append('\t' * body_indent + stripped)

    return '\n'.join(out_lines)


def main():
    parser = argparse.ArgumentParser(description='Format code that uses BEGIN / RETURN / END blocks.')
    parser.add_argument('infile', nargs='?', default='out.txt', help='input file (default: out.txt)')
    parser.add_argument('outfile', nargs='?', default='out_formatted.txt', help='output file (default: out_formatted.txt)')
    parser.add_argument('-p', '--print', action='store_true', help='also print formatted output to stdout')
    args = parser.parse_args()

    with open(args.infile, 'r', encoding='utf-8') as f:
        raw = f.read()

    formatted = format_code(raw)

    with open(args.outfile, 'w', encoding='utf-8') as f:
        f.write(formatted)

    if args.print:
        print(formatted)


if __name__ == '__main__':
    main()
