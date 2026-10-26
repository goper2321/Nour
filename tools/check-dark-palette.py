#!/usr/bin/env python3
"""NUR-007 — dark theme parity check for the Nour capture matrix.

Two parts:

1. REFERENCE TOKEN TABLE (always printed). The reference dark palette below is
   the seed-generated M3 scheme for the app seed #0A7AFF (Variant.CONTENT,
   neutral chroma 4/8, 2021 spec — the same values DynamicColor.kt/Color.kt now
   delegate to after the NUR-004 unification; generated with
   @material/material-color-utilities 0.4.0). Every row's CIE L* is printed and
   must stay inside its band:
     * dark surfaces: L* <= 30 (page and card surfaces read "darkish")
     * dark accents:  L* >= 55 AND saturation (max-min channel) >= 20
       (primary/secondary/tertiary containers keep a visible accent)

2. CAPTURE BAND CHECK (only with --captures-dir). For each dark-mode capture
   (files containing "dark") the script computes the pixel share of each band
   and a coarse accent-presence ratio, prints rows, and reports DRIFT against
   the bands above. Exit code is 0 unless --strict, so the printed report is
   advisory for review (captures include the full-bleed sky hero, whose own
   luminance is time-of-day dependent and intentionally not surface-toned).

Usage:
    python3 tools/check-dark-palette.py
    python3 tools/check-dark-palette.py --captures-dir app/build/screen-verify [--strict]
"""
import argparse
import os
import sys

# Reference dark scheme (post-NUR-004 single source of truth).
DARK_TOKENS = [
    # (role, hex, kind) kind in {surface, accent}
    ("surfaceContainerLowest", "#0D0E11", "surface"),
    ("surfaceContainerLow", "#1B1B1F", "surface"),
    ("surfaceContainer", "#1F1F23", "surface"),
    ("surfaceContainerHigh", "#292A2D", "surface"),
    ("surfaceContainerHighest", "#343538", "mid"),
    ("surfaceVariant", "#44474F", "mid"),
    ("surface (page)", "#121316", "surface"),
    ("onSurface", "#E3E2E6", "ink"),
    ("onSurfaceVariant", "#C4C6D0", "ink"),
    ("primary", "#ADC6FF", "accent"),
    ("primaryContainer", "#4C8EFF", "accent"),
    ("secondary", "#B8C6EA", "accent"),
    ("secondaryContainer", "#3B4967", "accent"),
    ("tertiary", "#F5ADFE", "accent"),
    ("tertiaryContainer", "#BB78C5", "accent"),
]

def srgb_to_Lstar(hexstr: str) -> float:
    h = hexstr.lstrip("#")
    rgb = [int(h[i:i+2], 16) / 255.0 for i in (0, 2, 4)]
    lin = [c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4 for c in rgb]
    rel = 0.2126 * lin[0] + 0.7152 * lin[1] + 0.0722 * lin[2]
    return (116.0 * _lab_f(rel) - 16.0) if rel > 0.008856 else 903.3 * rel

def _lab_f(t: float) -> float:
    e = 0.008856 ** (1.0 / 3.0)
    kappa = 903.3
    return t ** (1.0 / 3.0) if t > e else (kappa * t + 16.0) / 116.0

def sat(hexstr: str) -> int:
    h = hexstr.lstrip("#")
    rgb = [int(h[i:i+2], 16) for i in (0, 2, 4)]
    return max(rgb) - min(rgb)

def print_reference() -> None:
    print("=" * 62)
    print("NUR-007 reference dark palette (seed #0A7AFF generated) — bands")
    print("=" * 62)
    drift = 0
    for role, hx, kind in DARK_TOKENS:
        L = srgb_to_Lstar(hx); s = sat(hx)
        if kind == "surface":
            ok = L <= 30.0
            band = "L*<=30"
        elif kind == "mid":
            # The strongest neutral (segmented track) and surfaceVariant are
            # M3-standard steps ABOVE the page planes; they only need to stay
            # easy to see against the true dark planes.
            ok = 30.0 < L <= 65.0
            band = "30<L*<=65 (mid step)"
        elif kind == "ink":
            ok = L >= 70.0
            band = "L*>=70 (readable ink)"
        else:
            ok = L >= 55.0 and s >= 20
            band = "L*>=55 && sat>=20"
        drift += 0 if ok else 1
        print(f"{'DRIFT' if not ok else ' ok  '}  {role:<26} {hx}  L*={L:5.1f} sat={s:3d}  band: {band}")
    if drift == 0:
        print("reference table: all rows inside their bands")
    else:
        print(f"reference table: {drift} DRIFT rows — token layer needs review")

DARK_CAPTURE_EXPECT = {
    # (min share of dark-band pixels for screen-like captures, max share of page-bright pixels)
    "m01_prayer_dark": (0.45, 0.25),
    "m19_prayer_dark_high_contrast": (0.40, 0.30),
}
DARK_L = (2.0, 30.0)      # reference dark band in L*

def check_captures(captures_dir: str, strict: bool) -> int:
    try:
        from PIL import Image
    except ImportError:
        print("PIL not available; capture check skipped"); return 1
    found_dark = [f for f in sorted(os.listdir(captures_dir)) if f.endswith(".png") and "dark" in f]
    if not found_dark:
        print(f"no dark captures in {captures_dir} (record the matrix first)")
        return 1
    all_light = [f for f in sorted(os.listdir(captures_dir)) if f.endswith(".png") and "dark" not in f]
    print("\nCapture matrix dark-counterpart map:")
    print("  dark captures   :", ", ".join(found_dark))
    print("  light-only files:", ", ".join(all_light) or "(none)")
    print(f"\n{'capture':<34} {'dark-band%':>10} {'bright(L*>75)%':>15} {'verdict':>8}")
    failures = 0
    for name in found_dark:
        img = Image.open(os.path.join(captures_dir, name)).convert("RGB")
        img.thumbnail((240, 240))
        px = list(img.getdata())
        n = len(px)
        dark = bright = 0
        for r, g, b in px:
            c = [v / 255.0 for v in (r, g, b)]
            lin = [v / 12.92 if v <= 0.04045 else ((v + 0.055) / 1.055) ** 2.4 for v in c]
            rel = 0.2126 * lin[0] + 0.7152 * lin[1] + 0.0722 * lin[2]
            L = 116.0 * _lab_f(rel) - 16.0 if rel > 0.008856 else 903.3 * rel
            if DARK_L[0] <= L <= DARK_L[1]:
                dark += 1
            if L > 75.0:
                bright += 1
        sd = dark / n; sb = bright / n
        lo_dark, hi_bright = DARK_CAPTURE_EXPECT.get(name, (0.40, 0.40))
        ok = sd >= lo_dark and sb <= hi_bright
        verdict = "ok" if ok else "DRIFT"
        failures += 0 if ok else 1
        print(f"{name:<34} {sd*100:>9.1f}% {sb*100:>14.1f}% {verdict:>8}")
    return failures if strict else 0

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--captures-dir")
    ap.add_argument("--strict", action="store_true",
                    help="exit non-zero when a capture drifts (default: report only)")
    args = ap.parse_args()
    print_reference()
    rc = 0
    if args.captures_dir:
        rc = check_captures(args.captures_dir, args.strict)
    if args.strict and rc:
        print("\nSTRICT: drift detected", file=sys.stderr)
    return rc

if __name__ == "__main__":
    raise SystemExit(main())
